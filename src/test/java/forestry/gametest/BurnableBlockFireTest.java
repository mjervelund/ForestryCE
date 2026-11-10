package forestry.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.core.features.CoreBlocks;

/**
 * Plywood sheets and cork extend {@code BlockBurnable} and the plywood block copies its fire code, which is meant to "catch on fire like planks".
 * The port used 45 for both the spread speed and the flammability, made the blocks a permanent fire source (fire on top
 * never went out, like netherrack) and replaced the block with fire as soon as it caught. Upstream fixed this in
 * e75bf6875 (and the plywood block in 32abba0c5): plank odds, no fire source, vanilla burn-away behaviour.
 */
public class BurnableBlockFireTest {
	private static final BlockPos POS = new BlockPos(1, 2, 1);

	@ForestryGameTest
	public static void burnableBlocksBurnLikePlanks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos abs = helper.absolutePos(POS);
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		helper.setBlock(POS, planks);
		int plankFlammability = planks.getFlammability(level, abs, Direction.UP);
		int plankSpread = planks.getFireSpreadSpeed(level, abs, Direction.UP);

		List<Block> blocks = List.of(CoreBlocks.PLYWOOD_SHEET.block(), CoreBlocks.PLYWOOD_BLOCK.block(), CoreBlocks.CORK.block());
		for (Block block : blocks) {
			BlockState state = block.defaultBlockState();
			helper.setBlock(POS, state);
			for (Direction dir : Direction.values()) {
				int flammability = state.getFlammability(level, abs, dir);
				int spread = state.getFireSpreadSpeed(level, abs, dir);
				helper.assertTrue(flammability == plankFlammability,
					block + " flammability " + flammability + " != planks " + plankFlammability + " (" + dir + ")");
				helper.assertTrue(spread == plankSpread,
					block + " fire spread speed " + spread + " != planks " + plankSpread + " (" + dir + ")");
			}
			// a sheet only burns on the face it fills, so only the full blocks must catch on every face
			if (block != CoreBlocks.PLYWOOD_SHEET.block()) {
				for (Direction dir : Direction.values()) {
					helper.assertTrue(state.isFlammable(level, abs, dir), block + " is not flammable (" + dir + ")");
				}
			}
			helper.assertFalse(state.isFireSource(level, abs, Direction.UP),
				block + " is a permanent fire source; fire on top of it never goes out");
		}
		helper.succeed();
	}
}
