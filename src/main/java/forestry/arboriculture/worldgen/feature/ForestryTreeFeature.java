package forestry.arboriculture.worldgen.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.core.genetics.IGenome;
import forestry.arboriculture.commands.TreeGenHelper;
import forestry.core.platform.util.SpeciesUtil;

/** A tree of a fixed genome, placeable from worldgen data ("forestry:custom_tree"). */
public record ForestryTreeFeature(IGenome genome) implements Feature {
	public static final MapCodec<ForestryTreeFeature> CODEC = MapCodec.recursive("forestry:custom_tree", self -> RecordCodecBuilder.mapCodec(builder -> builder.group(
		SpeciesUtil.TREE_TYPE.get().getKaryotype().getGenomeCodec().fieldOf("genome").forGetter(ForestryTreeFeature::genome)
	).apply(builder, ForestryTreeFeature::new)));

	@Override
	public MapCodec<ForestryTreeFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		ITreeSpecies species = this.genome.getActiveSpecies().cast();
		return TreeGenHelper.generateTree(species, this.genome, level, random, origin);
	}
}
