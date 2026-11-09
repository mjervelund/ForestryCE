package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.arboriculture.ForestryFruits;
import forestry.arboriculture.fruit.FruitPodBlockEntity;
import forestry.core.platform.util.SpeciesUtil;

/**
 * https://github.com/thedarkcolour/ForestryCE/issues/373: pods (cocoa, dates, papaya, coconut) show growth stages but
 * never ripen for the orchard. FruitPodBlock extends CocoaBlock, whose randomTick only raises the AGE state; the block
 * entity's maturity (what the orchard and the drops read) only grew in a scheduled tick nothing ever schedules.
 */
public class FruitPodGrowthTest {
	private static final BlockPos LOG_POS = new BlockPos(2, 2, 2);
	private static final BlockPos POD_POS = LOG_POS.south();

	@ForestryGameTest
	public static void randomTicksRipenThePod(GameTestHelper helper) {
		helper.setBlock(LOG_POS, Blocks.JUNGLE_LOG);
		ServerLevel level = helper.getLevel();
		BlockPos pod = helper.absolutePos(POD_POS);

		var treeType = SpeciesUtil.TREE_TYPE.get();
		boolean placed = treeType.setFruitBlock(level, treeType.getDefaultSpecies().getDefaultGenome(),
			treeType.getFruit(ForestryFruits.COCOA), 1.0f, pod);
		helper.assertTrue(placed, "the cocoa pod was not placed next to the jungle log");

		for (int i = 0; i < 100; i++) {
			BlockState state = level.getBlockState(pod);
			state.randomTick(level, pod, level.getRandom());
		}

		if (!(level.getBlockEntity(pod) instanceof FruitPodBlockEntity tile)) {
			helper.fail("the pod lost its block entity");
			return;
		}
		int age = level.getBlockState(pod).getValue(CocoaBlock.AGE);
		helper.assertValueEqual((int) tile.getMaturity(), 2, "pod maturity after 100 random ticks at yield 1.0 (state age " + age + ")");
		helper.assertValueEqual(age, 2, "pod state age");
		helper.succeed();
	}
}
