package forestry.agriculture;

import forestry.core.platform.legacy.LegacyCapabilities;
import forestry.agriculture.client.MultifarmClientHandler;
import forestry.agriculture.farmlogic.FarmingManager;
import forestry.agriculture.features.MultifarmBlockEntities;
import forestry.agriculture.plugin.FarmingRegistration;
import forestry.api.ForestryDataMaps;
import forestry.api.IForestryApi;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.plugin.IForestryPlugin;
import forestry.apiimpl.ForestryApiImpl;
import forestry.apiimpl.plugin.PluginManager;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.function.Consumer;

@ForestryModule
public class ModuleFarming extends BlankForestryModule {
	@Override
	public Identifier getId() {
		return ForestryModuleIds.FARMING;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleFarming::registerCapabilities);
		modBus.addListener(ModuleFarming::registerDataMaps);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		LegacyCapabilities.registerEnergy(event, MultifarmBlockEntities.GEARBOX.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerItems(event, MultifarmBlockEntities.HATCH.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerFluids(event, MultifarmBlockEntities.VALVE.tileType(), (tile, side) -> tile.getFluidHandler(side));
	}

	private static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(ForestryDataMaps.FARM_FERTILIZERS);
	}

	// Farm types are built from item stacks (icons, soils, germlings), so they wait for component binding
	@Override
	public void applyComponentBoundRegistration() {
		FarmingRegistration registration = new FarmingRegistration();

		for (IForestryPlugin plugin : PluginManager.getLoadedPlugins()) {
			try {
				plugin.registerFarming(registration);
			} catch (Throwable t) {
				throw new RuntimeException("An error was thrown by plugin " + plugin.id() + " during IForestryPlugin.registerFarming", t);
			}
		}

		// Defensive copy of fertilizers
		FarmingManager manager = new FarmingManager(registration.buildFarmTypes());

		((ForestryApiImpl) IForestryApi.INSTANCE).setFarmingManager(manager);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new MultifarmClientHandler());
	}
}
