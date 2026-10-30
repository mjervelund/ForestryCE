package forestry.core.content.machines;

import forestry.core.platform.legacy.LegacyCapabilities;
import forestry.api.ForestryDataMaps;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.core.content.machines.client.FactoryClientHandler;
import forestry.core.content.machines.features.FactoryTiles;
import forestry.core.content.machines.network.packets.PacketRecipeTransferRequest;
import forestry.core.content.machines.network.packets.PacketRecipeTransferUpdate;
import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.network.PacketIdServer;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.function.Consumer;

@ForestryModule
public class ModuleFactory extends BlankForestryModule {
	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		LegacyCapabilities.registerItems(event, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.CENTRIFUGE.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.RAINMAKER.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.MOISTENER.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.SMELTER.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getItemHandler(side));
		LegacyCapabilities.registerItems(event, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getItemHandler(side));

		LegacyCapabilities.registerEnergy(event, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerEnergy(event, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerEnergy(event, FactoryTiles.CENTRIFUGE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerEnergy(event, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerEnergy(event, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerEnergy(event, FactoryTiles.SMELTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerEnergy(event, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		LegacyCapabilities.registerEnergy(event, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getEnergyHandler(side));

		LegacyCapabilities.registerFluids(event, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		LegacyCapabilities.registerFluids(event, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		LegacyCapabilities.registerFluids(event, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getFluidHandler(side));
		LegacyCapabilities.registerFluids(event, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		LegacyCapabilities.registerFluids(event, FactoryTiles.MOISTENER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		LegacyCapabilities.registerFluids(event, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		LegacyCapabilities.registerFluids(event, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getFluidHandler(side));
	}

	@Override
	public Identifier getId() {
		return ForestryModuleIds.FACTORY;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleFactory::registerCapabilities);
		modBus.addListener(ModuleFactory::registerDataMaps);
	}

	private static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(ForestryDataMaps.FERMENTER_FUELS);
		event.register(ForestryDataMaps.MOISTENER_FUELS);
		event.register(ForestryDataMaps.RAINMAKER_FUELS);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new FactoryClientHandler());
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.RECIPE_TRANSFER_REQUEST, PacketRecipeTransferRequest::encode, PacketRecipeTransferRequest::decode, PacketRecipeTransferRequest::handle);
		registry.clientbound(PacketIdClient.RECIPE_TRANSFER_UPDATE, PacketRecipeTransferUpdate::encode, PacketRecipeTransferUpdate::decode, PacketRecipeTransferUpdate::handle);
	}
}
