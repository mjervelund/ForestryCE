package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.arboriculture.ForestryFruits;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.fruit.FruitPodBlock;
import forestry.arboriculture.wood.ForestryWoodType;
import forestry.arboriculture.wood.VanillaWoodType;
import forestry.core.platform.util.BlockUtil;
import forestry.core.platform.util.SpeciesUtil;

/**
 * https://github.com/thedarkcolour/ForestryCE/issues/240: pod fruits (cocoa, dates, papaya, coconut) only accepted the
 * burnable log tag of their tree, so a tree grown with fireproof logs never set pods and a pod next to a fireproof log
 * could not survive. Each test first checks the burnable log (so the test is not vacuous), then the fireproof one.
 */
public class PodFireproofLogTest {
	private static final BlockPos LOG_POS = new BlockPos(2, 2, 2);
	private static final BlockPos POD_POS = LOG_POS.south();

	@ForestryGameTest
	public static void datesOnFireproofPalm(GameTestHelper helper) {
		assertPodGrowsOn(helper, ForestryFruits.DATES, ArboricultureBlocks.LOGS.get(ForestryWoodType.PALM).block(),
			ArboricultureBlocks.LOGS_FIREPROOF.get(ForestryWoodType.PALM).block());
		helper.succeed();
	}

	@ForestryGameTest
	public static void papayaOnFireproofPapaya(GameTestHelper helper) {
		assertPodGrowsOn(helper, ForestryFruits.PAPAYA, ArboricultureBlocks.LOGS.get(ForestryWoodType.PAPAYA).block(),
			ArboricultureBlocks.LOGS_FIREPROOF.get(ForestryWoodType.PAPAYA).block());
		helper.succeed();
	}

	@ForestryGameTest
	public static void coconutOnFireproofCoconut(GameTestHelper helper) {
		assertPodGrowsOn(helper, ForestryFruits.COCONUT, ArboricultureBlocks.LOGS.get(ForestryWoodType.COCONUT).block(),
			ArboricultureBlocks.LOGS_FIREPROOF.get(ForestryWoodType.COCONUT).block());
		helper.succeed();
	}

	@ForestryGameTest
	public static void cocoaPodOnFireproofJungle(GameTestHelper helper) {
		assertPodGrowsOn(helper, ForestryFruits.COCOA, Blocks.JUNGLE_LOG,
			ArboricultureBlocks.LOGS_VANILLA_FIREPROOF.get(VanillaWoodType.JUNGLE).block());
		helper.succeed();
	}

	/** The cocoa fruit's ripening path places vanilla cocoa, which must also stay on the fireproof jungle log. */
	@ForestryGameTest
	public static void vanillaCocoaOnFireproofJungle(GameTestHelper helper) {
		assertVanillaCocoaOn(helper, Blocks.JUNGLE_LOG);
		assertVanillaCocoaOn(helper, ArboricultureBlocks.LOGS_VANILLA_FIREPROOF.get(VanillaWoodType.JUNGLE).block());
		helper.succeed();
	}

	private static void assertPodGrowsOn(GameTestHelper helper, Identifier fruitId, Block burnableLog, Block fireproofLog) {
		assertPodGrowsOn(helper, fruitId, burnableLog);
		assertPodGrowsOn(helper, fruitId, fireproofLog);
	}

	private static void assertPodGrowsOn(GameTestHelper helper, Identifier fruitId, Block log) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(POD_POS, Blocks.AIR);
		helper.setBlock(LOG_POS, log);
		BlockPos pod = helper.absolutePos(POD_POS);
		String what = fruitId.getPath() + " next to " + log.getDescriptionId();

		var treeType = SpeciesUtil.TREE_TYPE.get();
		boolean placed = treeType.setFruitBlock(level, treeType.getDefaultSpecies().getDefaultGenome(),
			treeType.getFruit(fruitId), 1.0f, pod);
		helper.assertTrue(placed, what + ": setFruitBlock did not place a pod");

		BlockState state = level.getBlockState(pod);
		helper.assertTrue(state.getBlock() instanceof FruitPodBlock, what + ": expected a pod block, found " + state);
		helper.assertValueEqual(state.getValue(FruitPodBlock.FACING), Direction.NORTH, what + ": pod facing");
		helper.assertTrue(state.canSurvive(level, pod), what + ": the placed pod cannot survive");
	}

	private static void assertVanillaCocoaOn(GameTestHelper helper, Block log) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(POD_POS, Blocks.AIR);
		helper.setBlock(LOG_POS, log);
		BlockPos pod = helper.absolutePos(POD_POS);
		String what = "vanilla cocoa next to " + log.getDescriptionId();

		helper.assertTrue(BlockUtil.tryPlantCocoaPod(level, pod), what + ": tryPlantCocoaPod did not plant");
		BlockState state = level.getBlockState(pod);
		helper.assertTrue(state.is(Blocks.COCOA), what + ": expected cocoa, found " + state);
		helper.assertValueEqual(state.getValue(CocoaBlock.FACING), Direction.NORTH, what + ": cocoa facing");
		helper.assertTrue(state.canSurvive(level, pod), what + ": the planted cocoa cannot survive");
	}
}
