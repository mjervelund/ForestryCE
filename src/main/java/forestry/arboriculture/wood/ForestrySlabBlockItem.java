package forestry.arboriculture.wood;

import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import forestry.core.platform.registration.RegistrationIds;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public class ForestrySlabBlockItem extends BlockItem {
	public ForestrySlabBlockItem(ForestrySlabBlock block) {
		super(block, block.isFireproof() ? RegistrationIds.item() : RegistrationIds.item().cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_SLABS));
	}

}
