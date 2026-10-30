package forestry.arboriculture.commands;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.core.genetics.IGenome;
import forestry.core.platform.util.BlockUtil;
import forestry.core.platform.worldgen.AbstractForestryFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

import javax.annotation.Nullable;
import java.util.Optional;

public class TreeGenHelper {
	public static boolean generateTree(ITreeSpecies tree, @Nullable IGenome genome, WorldGenLevel level, RandomSource random, BlockPos pos) {
		Feature gen = tree.getGenerator().getTreeFeature(tree);

		BlockState state = level.getBlockState(pos);
		if (BlockUtil.canPlaceTree(state, level, pos)) {
			// todo require FeatureBase so that the genome is always respected
			if (gen instanceof AbstractForestryFeature base) {
				if (genome == null) {
					genome = tree.getDefaultGenome();
				}
				return base.place(genome, level, random, pos, true);
			} else {
				return gen.place(level, ((ServerChunkCache) level.getChunkSource()).getGenerator(), random, pos);
			}
		}
		return false;
	}
}
