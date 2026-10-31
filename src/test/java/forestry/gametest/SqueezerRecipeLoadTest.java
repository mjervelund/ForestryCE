package forestry.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.fluids.FluidStack;

import forestry.api.ForestryConstants;
import forestry.api.core.machines.ISqueezerRecipe;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.machines.features.FactoryRecipeTypes;

/**
 * End-to-end oracle: the real built-in squeezer recipes load from datapack JSON (the new nested
 * {@code "output": {"stack": {...}}} shape) and resolve to the expected non-empty fluid output.
 */
public class SqueezerRecipeLoadTest {
	@ForestryGameTest
	public static void squeezerRecipesResolveOutput(GameTestHelper helper) {
		RecipeMap manager = RecipeUtils.recipes(helper.getLevel());

		long count = RecipeUtils.getRecipes(manager, FactoryRecipeTypes.SQUEEZER).count();
		if (count == 0) {
			helper.fail("No squeezer recipes loaded");
			return;
		}
		boolean anyEmpty = RecipeUtils.getRecipes(manager, FactoryRecipeTypes.SQUEEZER)
			.anyMatch(recipe -> recipe.getFluidOutput().createFluidStack().isEmpty());
		if (anyEmpty) {
			helper.fail("A built-in squeezer recipe resolved to an empty fluid output");
			return;
		}

		ISqueezerRecipe honeyBlock = RecipeUtils.getRecipes(manager, FactoryRecipeTypes.SQUEEZER)
			.filter(recipe -> recipe.getId().equals(ForestryConstants.forestry("squeezer/honey_block")))
			.findFirst()
			.orElse(null);
		if (honeyBlock == null) {
			helper.fail("Missing built-in squeezer/honey_block recipe");
			return;
		}
		FluidStack expected = ForestryFluids.HONEY.getFluid(Constants.FLUID_PER_HONEY_DROP * 8);
		FluidStack actual = honeyBlock.getFluidOutput().createFluidStack();
		if (!(FluidStack.isSameFluidSameComponents(actual, expected) && actual.getAmount() == expected.getAmount())) {
			helper.fail("honey_block output changed: " + actual + ", expected " + expected);
			return;
		}

		helper.succeed();
	}
}
