package forestry.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.sapling.SaplingBlockEntity;
import forestry.core.features.CoreBlocks;
import forestry.core.platform.util.SpeciesUtil;

/**
 * 26.x shrank {@code #minecraft:dirt} to dirt, coarse dirt and rooted dirt; grass blocks, moss and mud moved to their
 * own tags, collected again in {@code #minecraft:substrate_overworld}. {@code Tree.canPlantTreeOn} still checked
 * {@code #dirt}, so Forestry saplings popped off grass and never grew there. Every block of 1.21.1's {@code #dirt} (plus
 * Forestry's humus and turf) must still hold a sapling, and a sapling on grass must grow.
 */
public class SaplingSoilTest {
	private static final List<Block> OLD_DIRT = List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.COARSE_DIRT,
		Blocks.MYCELIUM, Blocks.ROOTED_DIRT, Blocks.MOSS_BLOCK, Blocks.PALE_MOSS_BLOCK, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS);

	@ForestryGameTest
	public static void saplingStaysOnEveryOldDirtBlock(GameTestHelper helper) {
		ITree tree = SpeciesUtil.TREE_TYPE.get().getSpecies(ForestryTreeSpecies.LIME).createIndividual();
		BlockPos ground = new BlockPos(1, 1, 1);
		BlockPos above = helper.absolutePos(ground.above());

		List<Block> soils = new ArrayList<>(OLD_DIRT);
		soils.add(CoreBlocks.HUMUS.block());
		soils.add(CoreBlocks.TURF_BLOCK.block());
		for (Block soil : soils) {
			helper.setBlock(ground, soil);
			helper.assertTrue(tree.canStay(helper.getLevel(), above), "a sapling cannot stay on " + soil);
		}
		helper.setBlock(ground, Blocks.STONE);
		helper.assertFalse(tree.canStay(helper.getLevel(), above), "a sapling can stay on stone");
		helper.succeed();
	}

	@ForestryGameTest
	public static void saplingOnGrassGrows(GameTestHelper helper) {
		ITreeSpecies species = SpeciesUtil.TREE_TYPE.get().getSpecies(ForestryTreeSpecies.LIME);
		BlockPos ground = new BlockPos(2, 1, 2);
		BlockPos sapling = ground.above();
		helper.setBlock(ground, Blocks.GRASS_BLOCK);
		helper.setBlock(sapling, ArboricultureBlocks.SAPLING_GE.block());
		if (!(helper.getLevel().getBlockEntity(helper.absolutePos(sapling)) instanceof SaplingBlockEntity tile)) {
			helper.fail("Expected a SaplingBlockEntity at " + sapling);
			return;
		}
		tile.setTree(species.createIndividual());

		// the first bonemeal only matures the sapling, the second grows it
		for (int i = 0; i < 2; i++) {
			tile.tryGrow(helper.getLevel().getRandom(), true);
		}
		helper.assertTrue(!helper.getBlockState(sapling).is(ArboricultureBlocks.SAPLING_GE.block()),
			"a bonemealed lime sapling on grass did not grow");
		helper.succeed();
	}
}
