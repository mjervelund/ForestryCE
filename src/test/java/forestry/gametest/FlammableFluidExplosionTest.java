package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.core.platform.fluids.ForestryFluids;

/**
 * Very flammable Forestry fluids (explosion power above 1: bio ethanol and biomass) turn into fire and explode on a
 * random tick when fire or lava is next to them. {@code isNearFire} used {@code noneMatch}, so a source block exploded
 * when nothing was burning near it and never when something was.
 * <p>
 * The tests call {@link BlockState#randomTick} directly. A test method body runs inside one server tick, so the fluid
 * never gets a fluid tick and cannot flow while the test runs. Everything sits inside an obsidian shell so a (buggy or
 * correct) explosion cannot reach neighbouring tests.
 */
public class FlammableFluidExplosionTest {
	private static final BlockPos FLUID_POS = new BlockPos(8, 4, 8);
	// isNearFire scans floor(x-1)..floor(x+1-0.001), i.e. the block itself and the -1 neighbours on each axis
	private static final BlockPos FIRE_POS = FLUID_POS.offset(-1, -1, 0);
	private static final int RANDOM_TICKS = 20;

	@ForestryGameTest
	public static void ethanolDoesNotExplodeAwayFromFire(GameTestHelper helper) {
		assertStableAwayFromFire(helper, ForestryFluids.BIO_ETHANOL);
	}

	@ForestryGameTest
	public static void biomassDoesNotExplodeAwayFromFire(GameTestHelper helper) {
		assertStableAwayFromFire(helper, ForestryFluids.BIOMASS);
	}

	@ForestryGameTest
	public static void ethanolExplodesNextToFire(GameTestHelper helper) {
		// 5x5x5 blast-proof shell with a 3x3x3 air interior around the fluid. Crying obsidian, not obsidian: fire inside
		// an obsidian frame lights a nether portal.
		fill(helper, FLUID_POS.offset(-2, -2, -2), FLUID_POS.offset(2, 2, 2), Blocks.CRYING_OBSIDIAN.defaultBlockState());
		fill(helper, FLUID_POS.offset(-1, -1, -1), FLUID_POS.offset(1, 1, 1), Blocks.AIR.defaultBlockState());
		helper.setBlock(FIRE_POS, Blocks.FIRE.defaultBlockState());
		helper.assertBlockPresent(Blocks.FIRE, FIRE_POS);

		Block fluidBlock = placeFluid(helper, ForestryFluids.BIO_ETHANOL);
		randomTick(helper);

		helper.assertFalse(helper.getBlockState(FLUID_POS).is(fluidBlock),
			"bio ethanol next to fire did not ignite after " + RANDOM_TICKS + " random ticks");
		helper.succeed();
	}

	private static void assertStableAwayFromFire(GameTestHelper helper, ForestryFluids fluid) {
		// fully encased: no fire, lava or burning block within reach
		fill(helper, FLUID_POS.offset(-2, -2, -2), FLUID_POS.offset(2, 2, 2), Blocks.OBSIDIAN.defaultBlockState());
		Block fluidBlock = placeFluid(helper, fluid);
		randomTick(helper);

		helper.assertTrue(helper.getBlockState(FLUID_POS).is(fluidBlock),
			fluid + " source with no fire nearby was replaced by " + helper.getBlockState(FLUID_POS) + " after random ticks");
		helper.succeed();
	}

	private static Block placeFluid(GameTestHelper helper, ForestryFluids fluid) {
		BlockState state = fluid.getFluid().defaultFluidState().createLegacyBlock();
		helper.assertTrue(state.isRandomlyTicking(), fluid + " no longer random ticks, so this test covers nothing");
		helper.setBlock(FLUID_POS, state);
		return state.getBlock();
	}

	private static void randomTick(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos abs = helper.absolutePos(FLUID_POS);
		for (int i = 0; i < RANDOM_TICKS; i++) {
			BlockState state = level.getBlockState(abs);
			if (!state.isRandomlyTicking()) {
				return;
			}
			state.randomTick(level, abs, level.getRandom());
		}
	}

	private static void fill(GameTestHelper helper, BlockPos from, BlockPos to, BlockState state) {
		for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
			helper.setBlock(pos, state);
		}
	}
}
