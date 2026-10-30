package forestry.apiculture.apiarist.villagers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.network.chat.Component;
import com.google.common.collect.ImmutableSet;
import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.apiary.ApicultureBlockType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.bees.EnumPropolis;
import forestry.core.platform.block.BlockTypeCoreTesr;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.platform.util.SpeciesUtil;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@FeatureProvider
public class ApicultureVillagers {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	private static final DeferredRegister<PoiType> POINTS_OF_INTEREST = REGISTRY.getRegistry(Registries.POINT_OF_INTEREST_TYPE);
	private static final DeferredRegister<VillagerProfession> PROFESSIONS = REGISTRY.getRegistry(Registries.VILLAGER_PROFESSION);

	public static final DeferredHolder<PoiType, PoiType> POI_ESCRITOIRE = POINTS_OF_INTEREST.register("escritoire", () -> new PoiType(Set.copyOf(CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).block().getStateDefinition().getPossibleStates()), 1, 1));
	public static final DeferredHolder<VillagerProfession, VillagerProfession> PROF_BEEKEEPER = PROFESSIONS.register("beekeeper", () -> {
		ResourceKey<PoiType> key = Objects.requireNonNull(POI_ESCRITOIRE.getKey());
		Predicate<Holder<PoiType>> jobSitePredicate = e -> e.is(key);
		// 26.x trades are data-driven (TradeSets); Forestry's randomized trades are not ported yet
		return new VillagerProfession(Component.translatable("entity.minecraft.villager.forestry.beekeeper"), jobSitePredicate, jobSitePredicate, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN, Int2ObjectMaps.emptyMap());
	});
}
