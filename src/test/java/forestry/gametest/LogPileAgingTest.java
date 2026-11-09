package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;

import forestry.arboriculture.charcoal.LogPileBlock;
import forestry.arboriculture.features.CharcoalBlocks;

/**
 * An ignited log pile ages (and finally turns into ash) only through its scheduled block tick. A scheduled tick is
 * bound to a position, so when a piston moves the pile the tick stays behind and the pile at its new position never
 * ages again: https://github.com/thedarkcolour/ForestryCE/issues/278. The same holds for any other way an active pile
 * is placed without going through the ignition path (a pile placed next to a burning one starts active, too).
 */
public class LogPileAgingTest {
	private static final BlockPos PISTON = new BlockPos(1, 2, 1);
	private static final BlockPos PILE = new BlockPos(2, 2, 1);
	private static final BlockPos PUSHED = new BlockPos(3, 2, 1);
	private static final BlockPos POWER = new BlockPos(0, 2, 1);

	private static BlockState activePile() {
		return CharcoalBlocks.LOG_PILE.defaultState().setValue(LogPileBlock.IS_ACTIVE, true);
	}

	private static boolean hasTick(GameTestHelper helper, BlockPos relative) {
		return helper.getLevel().getBlockTicks().hasScheduledTick(helper.absolutePos(relative), CharcoalBlocks.LOG_PILE.block());
	}

	@ForestryGameTest
	public static void activeLogPileKeepsAgingAfterBeingReplaced(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 2, 1);
		helper.setBlock(pos, activePile());
		ServerLevel level = helper.getLevel();
		// what the ignition path does
		level.scheduleTick(helper.absolutePos(pos), CharcoalBlocks.LOG_PILE.block(), LogPileBlock.TICK_RATE);

		// a move is a removal followed by a placement of the same state somewhere; the tick at pos goes with the removal
		helper.setBlock(pos, Blocks.AIR);
		helper.setBlock(pos.above(), activePile());

		helper.assertTrue(hasTick(helper, pos.above()), "a placed active log pile has no scheduled tick, so it never ages");
		helper.succeed();
	}

	@ForestryGameTest
	public static void activeLogPileKeepsAgingAfterPistonPush(GameTestHelper helper) {
		helper.setBlock(PISTON, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.EAST));
		helper.setBlock(PILE, activePile());
		helper.getLevel().scheduleTick(helper.absolutePos(PILE), CharcoalBlocks.LOG_PILE.block(), LogPileBlock.TICK_RATE);
		helper.assertTrue(hasTick(helper, PILE), "ignited log pile has no scheduled tick before the push");

		helper.setBlock(POWER, Blocks.REDSTONE_BLOCK);

		helper.succeedWhen(() -> {
			helper.assertBlockPresent(CharcoalBlocks.LOG_PILE.block(), PUSHED);
			helper.assertTrue(helper.getBlockState(PUSHED).getValue(LogPileBlock.IS_ACTIVE), "pushed log pile is no longer active");
			helper.assertTrue(hasTick(helper, PUSHED), "pushed active log pile has no scheduled tick, so it never ages (issue 278)");
		});
	}
}
