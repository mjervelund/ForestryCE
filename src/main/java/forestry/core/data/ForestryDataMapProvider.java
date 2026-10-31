package forestry.core.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;

import forestry.api.ForestryConstants;
import forestry.api.ForestryDataMaps;
import forestry.api.core.machines.fuels.BiogasEngineFuel;
import forestry.api.core.machines.fuels.FermenterFuel;
import forestry.api.core.machines.fuels.MoistenerFuel;
import forestry.api.core.machines.fuels.PeatEngineFuel;
import forestry.api.core.machines.fuels.RainmakerSubstrate;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.features.CoreItems;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.ForestryFluids;

/**
 * Generates the data maps for Forestry items, fluids and villagers: Forestry's machine fuels and
 * {@code neoforge:raid_hero_gifts}, the Hero of the Village gifts for Forestry professions. 26.3 dropped the
 * {@code neoforge:compostables} and {@code neoforge:furnace_fuels} data maps; composter chances and burn times
 * are item components set in {@link forestry.core.features.CompostablesAndFuels}.
 */
public class ForestryDataMapProvider extends DataMapProvider {
	public ForestryDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		gatherRaidHeroGifts();
		gatherFermenterFuels();
		gatherMoistenerFuels();
		gatherRainmakerFuels();
		gatherEngineFuels();
		gatherSwarmerFeed();
	}

	private void gatherFermenterFuels() {
		Builder<FermenterFuel, Item> fuels = builder(ForestryDataMaps.FERMENTER_FUELS);

		fuels.add(CoreItems.FERTILIZER_COMPOUND.item().builtInRegistryHolder(), new FermenterFuel(56, 200), false);
		fuels.add(CoreItems.COMPOST.item().builtInRegistryHolder(), new FermenterFuel(48, 250), false);
		fuels.add(CoreItems.MULCH.item().builtInRegistryHolder(), new FermenterFuel(48, 250), false);
	}

	private void gatherMoistenerFuels() {
		Builder<MoistenerFuel, Item> fuels = builder(ForestryDataMaps.MOISTENER_FUELS);

		// Each entry names the item the working slot leaves behind, so the three form a chain
		fuels.add(Items.WHEAT.builtInRegistryHolder(), new MoistenerFuel(CoreItems.MOULDY_WHEAT.template(), 0, 300), false);
		fuels.add(CoreItems.MOULDY_WHEAT.item().builtInRegistryHolder(), new MoistenerFuel(CoreItems.DECAYING_WHEAT.template(), 1, 600), false);
		fuels.add(CoreItems.DECAYING_WHEAT.item().builtInRegistryHolder(), new MoistenerFuel(CoreItems.MULCH.template(), 2, 900), false);
	}

	private void gatherRainmakerFuels() {
		Builder<RainmakerSubstrate, Item> fuels = builder(ForestryDataMaps.RAINMAKER_FUELS);

		fuels.add(CoreItems.IODINE_CHARGE.item().builtInRegistryHolder(), new RainmakerSubstrate(10000, 0.01f, false), false);
		fuels.add(CoreItems.DISSIPATION_CHARGE.item().builtInRegistryHolder(), new RainmakerSubstrate(0, 0.075f, true), false);
	}

	private void gatherEngineFuels() {
		Builder<BiogasEngineFuel, Fluid> biogas = builder(ForestryDataMaps.BIOGAS_FUELS);

		biogas.add(ForestryFluids.BIOMASS.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_BIOMASS, Constants.ENGINE_CYCLE_DURATION_BIOMASS, 1), false);
		biogas.add(NeoForgeMod.MILK, new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_MILK, Constants.ENGINE_CYCLE_DURATION_MILK, 3), false);
		biogas.add(ForestryFluids.SEED_OIL.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_SEED_OIL, Constants.ENGINE_CYCLE_DURATION_SEED_OIL, 1), false);
		biogas.add(ForestryFluids.HONEY.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_HONEY, Constants.ENGINE_CYCLE_DURATION_HONEY, 1), false);
		biogas.add(ForestryFluids.JUICE.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_JUICE, Constants.ENGINE_CYCLE_DURATION_JUICE, 2), false);

		Builder<BiogasEngineFuel, Fluid> combustion = builder(ForestryDataMaps.COMBUSTION_FUELS);

		combustion.add(ForestryFluids.BIO_ETHANOL.holder(), new BiogasEngineFuel(Constants.ENGINE_FUEL_VALUE_ETHANOL, Constants.ENGINE_CYCLE_DURATION_ETHANOL, 1), false);

		// Power per cycle is unread for a coolant, so it stays 0
		Builder<BiogasEngineFuel, Fluid> coolants = builder(ForestryDataMaps.COMBUSTION_COOLANTS);

		coolants.add(Fluids.WATER.builtInRegistryHolder(), new BiogasEngineFuel(0, Constants.ENGINE_COOLANT_VALUE_WATER, 0), false);
		coolants.add(ForestryFluids.ICE.holder(), new BiogasEngineFuel(0, Constants.ENGINE_COOLANT_VALUE_CRUSHED_ICE, 20), false);

		Builder<PeatEngineFuel, Item> peat = builder(ForestryDataMaps.PEAT_FUELS);

		peat.add(CoreItems.PEAT.item().builtInRegistryHolder(), new PeatEngineFuel(Constants.ENGINE_COPPER_FUEL_VALUE_PEAT, Constants.ENGINE_COPPER_CYCLE_DURATION_PEAT), false);
		peat.add(CoreItems.BITUMINOUS_PEAT.item().builtInRegistryHolder(), new PeatEngineFuel(Constants.ENGINE_COPPER_FUEL_VALUE_BITUMINOUS_PEAT, Constants.ENGINE_COPPER_CYCLE_DURATION_BITUMINOUS_PEAT), false);
	}

	private void gatherSwarmerFeed() {
		Builder<Float, Item> feed = builder(ForestryDataMaps.SWARMER_FEED);

		feed.add(ApicultureItems.ROYAL_JELLY.item().builtInRegistryHolder(), 0.01f, false);
	}

	private void gatherRaidHeroGifts() {
		Builder<RaidHeroGift, VillagerProfession> gifts = builder(NeoForgeDataMaps.RAID_HERO_GIFTS);
		gifts.add(ForestryConstants.forestry("beekeeper"), new RaidHeroGift(ForestryGiftLootTables.BEEKEEPER_GIFT), false);
		gifts.add(ForestryConstants.forestry("arborist"), new RaidHeroGift(ForestryGiftLootTables.ARBORIST_GIFT), false);
	}

	@Override
	public String getName() {
		return "Forestry Data Maps";
	}
}
