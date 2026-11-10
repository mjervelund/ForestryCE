package forestry.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

import forestry.agriculture.multifarm.multiblock.FarmController;
import forestry.api.IForestryApi;
import forestry.api.agriculture.ForestryFarmTypes;
import forestry.api.agriculture.IFarmLogic;

/**
 * Port of upstream's FarmPlatformRowTest (Forestry 89875420f, "Fix some erroneous position checks in farm logic"):
 * soil placement along a farm row must stop at the first platform block in the row, checking the scanned position
 * rather than the row's start position.
 */
public class FarmPlatformRowTest {
	// relative to the isolated site's corner at floor height
	private static final BlockPos FARM_BASE = new BlockPos(6, 1, 6);
	// a north side row, which runs west from the farm's east edge to the platform edge
	private static final BlockPos ROW_START = new BlockPos(8, 1, 3);
	private static final int ROW_EXTENT = 9;
	private static final BlockPos BRICK = new BlockPos(4, 1, 3);
	// the site needs real seconds to load, not ticks (see ChunkLifecycle.Steps#await)
	private static final int TIMEOUT = 2_000_000;

	/**
	 * Runs on an isolated chunk, not in the test grid: the grid spaces tests 6 blocks apart in x and 8 in z, so the
	 * 16x16 floor and the farm at (6..8, 1..4, 6..8) reached into neighbouring tests' cells, and a neighbour that
	 * placed a block there (FlowerTypeTest.tagFlowerTypeAcceptsTaggedBlock's dandelion at its (0, 1, 0), once the
	 * suite grew to put it there) replaced a farm block, so the farm never assembled.
	 */
	@ForestryGameTest(timeoutTicks = TIMEOUT)
	public static void soilStopsAtPlatformBlock(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ChunkPos site = ChunkLifecycle.isolatedSite(helper);
		BlockPos corner = ChunkLifecycle.siteBlock(helper, site, 0, 0).below();
		BlockPos brick = corner.offset(BRICK);
		BlockPos rowStart = corner.offset(ROW_START);
		List<BlockPos> members = new ArrayList<>();

		ChunkLifecycle.steps(helper)
			.run(() -> ChunkLifecycle.hold(level, site, 1))
			.await(() -> ChunkLifecycle.isTicking(level, site), "site did not start ticking")
			.run(() -> {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						helper.setBlock(corner.offset(x, 0, z), Blocks.STONE_BRICKS);
					}
				}
				helper.setBlock(brick, Blocks.STONE_BRICKS);
				members.addAll(MultiblockTestSupport.buildFarm(helper, corner.offset(FARM_BASE)));
			})
			.await(() -> MultiblockTestSupport.isAssembled(level, members.get(0)), "Expected an assembled multifarm at " + FARM_BASE)
			.run(() -> {
				FarmController farm = (FarmController) MultiblockTestSupport.controllerAt(level, members.get(0));
				MultiblockTestSupport.insertItem(level, members, MultiblockTestSupport.FARM_INV_SIZE, 0, new ItemStack(Blocks.MYCELIUM, 64));

				IFarmLogic shroom = IForestryApi.INSTANCE.getFarmingManager().getFarmType(ForestryFarmTypes.SHROOM).getLogic(false);
				BlockPos rowStartAbs = helper.absolutePos(rowStart);
				for (int i = 0; i < ROW_EXTENT; i++) {
					shroom.cultivate(level, farm, rowStartAbs, Direction.WEST, ROW_EXTENT);
				}

				for (int x = brick.getX() + 1; x <= rowStart.getX(); x++) {
					helper.assertBlockPresent(Blocks.MYCELIUM, new BlockPos(x, rowStart.getY(), rowStart.getZ()));
				}
				helper.assertBlockPresent(Blocks.STONE_BRICKS, brick);
				helper.assertBlockPresent(Blocks.AIR, brick.west());
				ChunkLifecycle.release(level, site, 1);
			})
			.succeed();
	}
}
