package forestry.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.leaves.ForestryLeafType;

/**
 * ForestryCE#369: Forestry leaves never dropped sticks; vanilla leaves drop 1-2 with a 2% chance (more with fortune)
 * when not broken with shears or silk touch. 3000 breaks without sticks would happen with p = 0.98^3000, about 1e-26.
 */
public class LeavesStickDropTest {
	private static final int BREAKS = 3000;

	@ForestryGameTest
	public static void leavesDropSticks(GameTestHelper helper) {
		List<BlockState> leaves = List.of(
			ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.LARCH).defaultState(),
			ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.LEMON).defaultState(),
			ArboricultureBlocks.LEAVES_DECORATIVE.get(ForestryLeafType.LARCH).defaultState());
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		for (BlockState state : leaves) {
			int sticks = 0;
			for (int i = 0; i < BREAKS; i++) {
				for (ItemStack drop : Block.getDrops(state, helper.getLevel(), pos, null)) {
					if (drop.is(Items.STICK)) {
						sticks += drop.getCount();
					}
				}
			}
			helper.assertTrue(sticks > 0, state.getBlock() + " dropped no stick in " + BREAKS + " breaks");
		}
		helper.succeed();
	}
}
