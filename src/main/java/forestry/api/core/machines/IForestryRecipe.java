package forestry.api.core.machines;

import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public interface IForestryRecipe extends Recipe<RecipeInput> {
	// <editor-fold desc="Ignore these methods, we just piggy back off Minecraft's system for recipe sync">
	@Deprecated
	@Override
	default boolean matches(RecipeInput inv, Level level) {
		return false;
	}

	@Deprecated
	@Override
	default ItemStack assemble(RecipeInput inv) {
		return ItemStack.EMPTY;
	}

	@Deprecated
	@Override
	default boolean isSpecial() {
		return true;
	}

	@Deprecated
	@Override
	default boolean showNotification() {
		return false;
	}

	@Deprecated
	@Override
	default String group() {
		return "forestry";
	}

	@Deprecated
	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Deprecated
	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}
	// </editor-fold>

	/** The main output for display (JEI, guide). No longer part of vanilla's {@code Recipe} in 26.x. */
	default ItemStack getResultItem(HolderLookup.Provider lookupProvider) {
		return ItemStack.EMPTY;
	}

	Identifier getId();

	@Override
	RecipeSerializer<? extends IForestryRecipe> getSerializer();

	@Override
	RecipeType<? extends IForestryRecipe> getType();
}
