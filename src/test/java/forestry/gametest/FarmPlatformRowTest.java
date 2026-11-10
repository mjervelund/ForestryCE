package forestry.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
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
	private static final BlockPos FARM_BASE = new BlockPos(6, 1, 6);
	// a north side row, which runs west from the farm's east edge to the platform edge
	private static final BlockPos ROW_START = new BlockPos(8, 1, 3);
	private static final int ROW_EXTENT = 9;
	private static final BlockPos BRICK = new BlockPos(4, 1, 3);

	@ForestryGameTest(timeoutTicks = 200)
	public static void soilStopsAtPlatformBlock(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE_BRICKS);
			}
		}
		helper.setBlock(BRICK, Blocks.STONE_BRICKS);
		List<BlockPos> members = MultiblockTestSupport.buildFarm(helper, FARM_BASE);

		// wait for the multiblock to assemble
		helper.runAfterDelay(40, () -> {
			if (!(MultiblockTestSupport.controllerAt(level, members.get(0)) instanceof FarmController farm) || !farm.isAssembled()) {
				helper.fail("Expected an assembled multifarm at " + FARM_BASE);
				return;
			}
			MultiblockTestSupport.insertItem(level, members, MultiblockTestSupport.FARM_INV_SIZE, 0, new ItemStack(Blocks.MYCELIUM, 64));

			IFarmLogic shroom = IForestryApi.INSTANCE.getFarmingManager().getFarmType(ForestryFarmTypes.SHROOM).getLogic(false);
			BlockPos rowStart = helper.absolutePos(ROW_START);
			for (int i = 0; i < ROW_EXTENT; i++) {
				shroom.cultivate(level, farm, rowStart, Direction.WEST, ROW_EXTENT);
			}

			for (int x = BRICK.getX() + 1; x <= ROW_START.getX(); x++) {
				helper.assertBlockPresent(Blocks.MYCELIUM, new BlockPos(x, 1, ROW_START.getZ()));
			}
			helper.assertBlockPresent(Blocks.STONE_BRICKS, BRICK);
			helper.assertBlockPresent(Blocks.AIR, BRICK.west());
			helper.succeed();
		});
	}
}
