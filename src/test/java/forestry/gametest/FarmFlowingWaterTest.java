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
import net.minecraft.world.level.block.LiquidBlock;

import forestry.agriculture.multifarm.multiblock.FarmController;
import forestry.api.IForestryApi;
import forestry.api.agriculture.ForestryFarmTypes;
import forestry.api.agriculture.IFarmLogic;

/**
 * ForestryCE#200 "Farms cannot replace blocks that are blocked by water": a managed farm lays its soil over whatever is
 * in the way, but skipped any position holding water, flowing water included (FarmLogic.isWaterSourceBlock matched
 * every water fluid), so a stream running across the field left a permanent gap. Only real water sources (and
 * waterlogged blocks) are to be kept.
 */
public class FarmFlowingWaterTest {
	// relative to the isolated site's corner at floor height
	private static final BlockPos FARM_BASE = new BlockPos(6, 1, 6);
	// a north side row, which runs west from the farm's east edge
	private static final BlockPos ROW_START = new BlockPos(8, 1, 3);
	private static final int ROW_EXTENT = 9;
	private static final BlockPos FLOWING = new BlockPos(5, 1, 3);
	// the site needs real seconds to load, not ticks (see ChunkLifecycle.Steps#await)
	private static final int TIMEOUT = 2_000_000;

	@ForestryGameTest(timeoutTicks = TIMEOUT)
	public static void soilReplacesFlowingWater(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ChunkPos site = ChunkLifecycle.isolatedSite(helper);
		BlockPos corner = ChunkLifecycle.siteBlock(helper, site, 0, 0).below();
		BlockPos flowing = corner.offset(FLOWING);
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
				members.addAll(MultiblockTestSupport.buildFarm(helper, corner.offset(FARM_BASE)));
			})
			.await(() -> MultiblockTestSupport.isAssembled(level, members.get(0)), "Expected an assembled multifarm at " + FARM_BASE)
			.run(() -> {
				FarmController farm = (FarmController) MultiblockTestSupport.controllerAt(level, members.get(0));
				MultiblockTestSupport.insertItem(level, members, MultiblockTestSupport.FARM_INV_SIZE, 0, new ItemStack(Blocks.DIRT, 64));

				// flowing water (no source) in the row; placed in the same tick, so it cannot drain away first
				helper.setBlock(flowing, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 3));
				helper.assertTrue(!level.getFluidState(helper.absolutePos(flowing)).isSource(), "test water is a source");

				IFarmLogic crops = IForestryApi.INSTANCE.getFarmingManager().getFarmType(ForestryFarmTypes.CROPS).getLogic(false);
				BlockPos rowStartAbs = helper.absolutePos(rowStart);
				for (int i = 0; i < ROW_EXTENT; i++) {
					crops.cultivate(level, farm, rowStartAbs, Direction.WEST, ROW_EXTENT);
				}

				helper.assertBlockPresent(Blocks.FARMLAND, flowing);
				ChunkLifecycle.release(level, site, 1);
			})
			.succeed();
	}
}
