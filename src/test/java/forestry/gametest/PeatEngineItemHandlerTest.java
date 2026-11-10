package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import forestry.core.content.energy.blocks.EngineBlockType;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.inventory.InventoryEnginePeat;
import forestry.core.features.CoreItems;
import forestry.core.platform.legacy.LegacyCapabilities;
import forestry.core.platform.legacy.items.IItemHandler;

/**
 * The peat engine has a fuel slot and four ash slots but registered no item capability, so pipes, hoppers of other
 * mods and Forestry's own automation could neither feed it peat nor take its ash out (upstream 1.21.1 has the same gap).
 */
public class PeatEngineItemHandlerTest {
	private static final BlockPos ENGINE = new BlockPos(2, 2, 2);

	@ForestryGameTest
	public static void peatEngineAcceptsPeatThroughItemHandler(GameTestHelper helper) {
		helper.setBlock(ENGINE, EnergyBlocks.ENGINES.get(EngineBlockType.PEAT).defaultState());

		IItemHandler handler = LegacyCapabilities.getItemHandler(helper.getLevel(), helper.absolutePos(ENGINE), Direction.UP);
		helper.assertTrue(handler != null, "peat engine exposes no item handler");
		ItemStack left = handler.insertItem(InventoryEnginePeat.SLOT_FUEL, CoreItems.PEAT.stack(4), false);
		helper.assertTrue(left.isEmpty(), "peat engine refused peat through its item handler: " + left);
		helper.succeed();
	}
}
