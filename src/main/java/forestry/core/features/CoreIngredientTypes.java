package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.platform.util.EmptyIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@FeatureProvider
public class CoreIngredientTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = REGISTRY.getRegistry(NeoForgeRegistries.Keys.INGREDIENT_TYPES);

	public static final DeferredHolder<IngredientType<?>, IngredientType<EmptyIngredient>> EMPTY = INGREDIENT_TYPES.register("empty", () -> EmptyIngredient.TYPE);
}
