package forestry.api.arboriculture;

import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Implements the tree generation for a tree species.
 */
public interface ITreeGenerator {
	Feature getTreeFeature(ITreeGenData tree);

	boolean setLogBlock(IGenome genome, LevelAccessor level, BlockPos pos, Direction facing);

	boolean setLeaves(IGenome genome, LevelAccessor level, BlockPos pos, RandomSource rand);
}
