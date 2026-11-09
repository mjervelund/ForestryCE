package forestry.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import forestry.agriculture.farmlogic.crops.CropPeat;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;

// https://github.com/thedarkcolour/ForestryCE/issues/350
public class PeatBogHarvestTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	// A peat block broken by hand drops 2 peat (loot table blocks/peat.json); the peat bog farm must not give less.
	@ForestryGameTest
	public static void peatBogHarvestsTwoPeat(GameTestHelper helper) {
		helper.setBlock(POS, CoreBlocks.PEAT.defaultState());
		List<ItemStack> drops = new CropPeat(helper.getLevel(), helper.absolutePos(POS)).harvest();
		helper.assertTrue(drops != null, "peat block was not recognised as a crop");

		int peat = drops.stream().filter(stack -> stack.is(CoreItems.PEAT.item())).mapToInt(ItemStack::getCount).sum();
		helper.assertValueEqual(peat, 2, "peat harvested by the peat bog");
		helper.succeed();
	}
}
