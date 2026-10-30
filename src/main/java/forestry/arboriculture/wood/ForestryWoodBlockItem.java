package forestry.arboriculture.wood;

import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import forestry.core.platform.registration.RegistrationIds;
import forestry.core.platform.item.ItemBlockForestry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class ForestryWoodBlockItem<B extends Block & IWoodTyped> extends ItemBlockForestry<B> {
	private final IWoodTyped wood;

	public ForestryWoodBlockItem(B block) {
		this(block, ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
	}

	// 26.x: burn times are the COOKING_FUEL component instead of getBurnTime (300 ticks, doors 200)
	protected ForestryWoodBlockItem(B block, ResourceKey<ContextIntProvider> burnTime) {
		super(block, block.isFireproof() ? RegistrationIds.item() : RegistrationIds.item().cookingFuel(burnTime));

		// Safeguard against Diagonal Fence's registry replacements causing crashes
		this.wood = block;
	}

}
