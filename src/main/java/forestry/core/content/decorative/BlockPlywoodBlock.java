package forestry.core.content.decorative;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A full block of plywood, which lays its grain along the axis it was placed on.
 * <p>
 * The fire overrides below repeat {@link BlockBurnable} verbatim. A pillar block has to extend
 * RotatedPillarBlock for its axis, so it cannot extend BlockBurnable as well.
 */
public class BlockPlywoodBlock extends RotatedPillarBlock {
	public BlockPlywoodBlock(Properties properties) {
		super(properties);
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 5;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 20;
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return true;
	}
}
