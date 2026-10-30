package forestry.core.platform.worldgen;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.MapCodec;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * A tree or hive generator. These are called directly by saplings, commands and the decorators and are never
 * registered or serialized as worldgen features, so they have no codec.
 */
public abstract class AbstractForestryFeature implements Feature {
	@Override
	public MapCodec<? extends Feature> codec() {
		throw new UnsupportedOperationException("Forestry generators are not serializable worldgen features");
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		return place(getDefaultGenome(), level, random, origin, false);
	}

	public abstract IGenome getDefaultGenome();

	public abstract boolean place(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos pos, boolean forced);
}
