package forestry.core.features;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import forestry.api.ForestryConstants;
import forestry.apiculture.bees.PollenClusterItem;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.content.resources.EnumCraftingMaterial;

/**
 * Composter chances and furnace burn times for Forestry items.
 * <p>
 * Deviation from 1.21.1: these were the NeoForge {@code neoforge:compostables} and {@code neoforge:furnace_fuels}
 * data maps (ForestryDataMapProvider). 26.3 removed both; the composter reads {@link DataComponents#COMPOSTABLE}
 * and furnaces read {@link DataComponents#COOKING_FUEL}, so the values are default components now. Both name a
 * {@code context_int_provider}: vanilla's where one has the same value, otherwise Forestry's own under
 * {@code data/forestry/context_int_provider}, written like vanilla's so blast furnaces and smokers still burn
 * fuel twice as fast.
 */
public final class CompostablesAndFuels {
	public static final ResourceKey<ContextIntProvider> COMPOSTABLE_HIGH = key("compostable/high");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_PEAT = key("cooking/time_peat");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_BITUMINOUS_PEAT = key("cooking/time_bituminous_peat");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_LOG_PILE = key("cooking/time_log_pile");
	public static final ResourceKey<ContextIntProvider> COOKING_TIME_PLYWOOD_SHEET = key("cooking/time_plywood_sheet");

	private CompostablesAndFuels() {
	}

	public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
		// 0.3 -> LOW, 0.65 -> MEDIUM, 0.75 -> forestry:compostable/high, 1.0 -> ALWAYS_ADD_ONE
		for (Item fruit : CoreItems.FRUITS.getItems()) {
			compostable(event, fruit, ContextIntProviders.COMPOSTABLE_MEDIUM);
		}
		compostable(event, CoreItems.MOULDY_WHEAT.item(), ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, CoreItems.DECAYING_WHEAT.item(), ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, CoreItems.MULCH.item(), ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, CoreItems.ASH.item(), ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.WOOD_PULP), ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, CoreItems.PEAT.item(), COMPOSTABLE_HIGH);
		compostable(event, CoreItems.COMPOST.item(), ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		for (PollenClusterItem pollen : ApicultureItems.POLLEN_CLUSTER.getItems()) {
			compostable(event, pollen, ContextIntProviders.COMPOSTABLE_LOW);
		}
		compostable(event, ArboricultureItems.TREE_SAPLING.item(), ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, ArboricultureItems.TREE_POLLEN.item(), ContextIntProviders.COMPOSTABLE_LOW);
		for (BlockItem leaves : ArboricultureBlocks.LEAVES_DECORATIVE.getItems()) {
			compostable(event, leaves, ContextIntProviders.COMPOSTABLE_LOW);
		}
		// The cocoon is set by the butterflies jar (ModuleLepidopterology)

		fuel(event, CharcoalBlocks.CHARCOAL.item(), ContextIntProviders.COOKING_TIME_COAL_BLOCK);
		fuel(event, CoreItems.BITUMINOUS_PEAT.item(), COOKING_TIME_BITUMINOUS_PEAT);
		fuel(event, CoreItems.PEAT.item(), COOKING_TIME_PEAT);
		fuel(event, CharcoalBlocks.LOG_PILE.item(), COOKING_TIME_LOG_PILE);
		fuel(event, CharcoalBlocks.DECORATIVE_LOG_PILE.item(), COOKING_TIME_LOG_PILE);
		fuel(event, CoreBlocks.PLYWOOD_BLOCK.item(), ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
		fuel(event, CoreBlocks.CORK.item(), ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
		fuel(event, CoreBlocks.PLYWOOD_SHEET.item(), COOKING_TIME_PLYWOOD_SHEET);
	}

	public static void compostable(ModifyDefaultComponentsEvent event, ItemLike item, ResourceKey<ContextIntProvider> layers) {
		event.modify(item, (components, context, unused) -> components.set(DataComponents.COMPOSTABLE, new Compostable(layers)));
	}

	private static void fuel(ModifyDefaultComponentsEvent event, ItemLike item, ResourceKey<ContextIntProvider> burnTime) {
		event.modify(item, (components, context, unused) -> components.set(DataComponents.COOKING_FUEL,
			new CookingFuel(burnTime, ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER)));
	}

	private static ResourceKey<ContextIntProvider> key(String path) {
		return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, ForestryConstants.forestry(path));
	}
}
