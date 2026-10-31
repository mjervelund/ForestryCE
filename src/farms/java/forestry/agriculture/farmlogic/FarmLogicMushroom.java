package forestry.agriculture.farmlogic;

import forestry.api.agriculture.IFarmHousing;
import forestry.api.agriculture.IFarmType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FarmLogicMushroom extends FarmLogicArboreal {
	public FarmLogicMushroom(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public List<ItemStack> collect(Level level, IFarmHousing farmHousing) {
		return List.of();//Needed to override Arboreal #collect
	}

	/**
	 * Planted small mushrooms never become huge ones by themselves (vanilla only does that through bonemeal),
	 * so the farm grows them with the same call bonemeal uses, one per work cycle, before planting new ones.
	 */
	@Override
	protected boolean maintainSeedlings(Level level, IFarmHousing farmHousing, BlockPos pos, Direction direction, int extent) {
		if (level instanceof ServerLevel serverLevel) {
			for (int i = 0; i < extent; i++) {
				BlockPos position = translateWithOffset(pos, direction, i);
				BlockState state = level.getBlockState(position);
				if (state.getBlock() instanceof MushroomBlock mushroom && mushroom.growMushroom(serverLevel, position, state, level.getRandom())) {
					return true;
				}
			}
		}
		return super.maintainSeedlings(level, farmHousing, pos, direction, extent);
	}
}
