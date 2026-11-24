package forestry.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.ForestryTags;
import forestry.core.engine.genetics.flowers.TagFlowerType;

/**
 * Bees plant copies of the flowers around their hive ({@link TagFlowerType#plantRandomFlower}) when the block is in
 * {@code #forestry:flowers/plantable}. That tag took all of {@code #minecraft:flowers}, which in 26.3 also holds
 * flowering azalea leaves and cherry leaves, so bees "planted" leaf blocks: a free leaf (and sapling/flower drop)
 * duplicator (ForestryCE#421).
 */
public class PlantableFlowersTest {
	private static final BlockPos SOIL = new BlockPos(1, 1, 1);
	private static final BlockPos PLANT = SOIL.above();

	@ForestryGameTest
	public static void leavesAreNotPlantableFlowers(GameTestHelper helper) {
		for (BlockState leaves : List.of(Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), Blocks.CHERRY_LEAVES.defaultBlockState())) {
			helper.assertFalse(leaves.is(ForestryTags.Blocks.PLANTABLE_FLOWERS), leaves.getBlock() + " is in #forestry:flowers/plantable");
		}
		helper.succeed();
	}

	@ForestryGameTest
	public static void beesDoNotPlantLeaves(GameTestHelper helper) {
		helper.setBlock(SOIL, Blocks.GRASS_BLOCK);
		helper.setBlock(PLANT, Blocks.AIR);
		TagFlowerType vanilla = new TagFlowerType(ForestryTags.Blocks.VANILLA_FLOWERS, false);
		BlockState leaves = Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState();

		boolean planted = vanilla.plantRandomFlower(helper.getLevel(), helper.absolutePos(PLANT), List.of(leaves));
		helper.assertFalse(planted, "a bee planted flowering azalea leaves");
		helper.assertBlockPresent(Blocks.AIR, PLANT);

		// real flowers, small and tall, are still planted
		helper.assertTrue(vanilla.plantRandomFlower(helper.getLevel(), helper.absolutePos(PLANT), List.of(Blocks.POPPY.defaultBlockState())), "poppy not planted");
		helper.assertBlockPresent(Blocks.POPPY, PLANT);
		helper.setBlock(PLANT, Blocks.AIR);
		helper.assertTrue(vanilla.plantRandomFlower(helper.getLevel(), helper.absolutePos(PLANT), List.of(Blocks.ROSE_BUSH.defaultBlockState())), "rose bush not planted");
		helper.assertBlockPresent(Blocks.ROSE_BUSH, PLANT);
		helper.succeed();
	}
}
