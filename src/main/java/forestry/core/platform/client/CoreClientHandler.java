package forestry.core.platform.client;

import forestry.core.platform.models.FluidContainerModel;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import forestry.core.platform.models.ForestryTints;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.FluidModel;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.api.client.IForestryClientApi;
import forestry.apiimpl.client.ForestryClientApiImpl;
import forestry.apiimpl.plugin.PluginManager;
import forestry.core.engine.circuits.GuiSolderingIron;
import forestry.core.platform.config.Constants;
import forestry.core.engine.genetics.GeneticsReloadHandler;
import forestry.core.features.*;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.gui.*;
import forestry.core.content.analyzer.*;
import forestry.core.content.burnbarrel.GuiBurnBarrel;
import forestry.core.content.escritoire.*;
import forestry.core.platform.models.ClientManager;
import forestry.core.platform.models.ModelBlockCached;
import forestry.core.platform.particles.CoreParticles;
import forestry.core.platform.render.*;
import forestry.core.content.machines.*;
import forestry.core.platform.registration.FeatureFluid;
import forestry.core.content.energy.features.EnergyTiles;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.features.FactoryTiles;
import forestry.modules.ModuleUtil;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.awt.*;

public class CoreClientHandler implements IClientModuleHandler {

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(CoreClientHandler::onClientSetup);
		modBus.addListener(CoreClientHandler::registerMenuScreens);
		modBus.addListener(CoreClientHandler::registerModelLoaders);
		modBus.addListener(CoreClientHandler::bakeModels);
		modBus.addListener(CoreClientHandler::registerFluidModels);
		modBus.addListener(CoreClientHandler::registerItemTintSources);
		modBus.addListener(CoreClientHandler::registerItemModels);
		modBus.addListener(SpectacleHighlightRenderer::registerPipelines);
		modBus.addListener(CoreClientHandler::setupLayers);
		modBus.addListener(CoreClientHandler::clientSetupRenderers);
		modBus.addListener(CoreClientHandler::registerSpecialModelRenderers);
		modBus.addListener(CoreClientHandler::registerReloadListeners);
		modBus.addListener(CoreClientHandler::registerBlockColors);
		modBus.addListener(CoreClientHandler::registerParticleFactory);
		NeoForge.EVENT_BUS.addListener(SpectacleHighlightRenderer::submit);
		// HIGH priority so the client mutation index is rebuilt before NORMAL-priority consumers (e.g. JEI, which can
		// start synchronously during RecipesReceivedEvent on a dedicated-server connection) read it.
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, CoreClientHandler::onRecipesUpdated);

		ModuleUtil.getModBus(ForestryConstants.MOD_ID).addListener(EventPriority.HIGHEST, ((ForestryClientApiImpl) IForestryClientApi.INSTANCE)::initializeTextureManager);
	}

	// 26.x: ItemBlockRenderTypes is gone; the chunk layer comes from each sprite's transparency
	private static void onClientSetup(FMLClientSetupEvent event) {
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(CoreMenuTypes.ALYZER.menuType(), PortableAnalyzerScreen::new);
		event.register(CoreMenuTypes.ANALYZER.menuType(), GuiAnalyzer::new);
		event.register(CoreMenuTypes.NATURALIST_INVENTORY.menuType(), GuiNaturalistInventory<ContainerNaturalistInventory>::new);
		event.register(CoreMenuTypes.ESCRITOIRE.menuType(), GuiEscritoire::new);
		event.register(CoreMenuTypes.BURN_BARREL.menuType(), GuiBurnBarrel::new);
		event.register(CoreMenuTypes.SOLDERING_IRON.menuType(), GuiSolderingIron::new);
	}

	private static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
		// Client model registration depends on TreeManager / BeeManager / etc. being
		// initialized. RegisterGeometryLoaders fires before FMLCommonSetupEvent, so make
		// sure the API is bootstrapped before plugin client registration runs.
		forestry.core.ModuleCore.ensureApiInitialized();
		PluginManager.registerClient();
	}

	private static void bakeModels(ModelEvent.ModifyBakingResult event) {
		ClientManager.INSTANCE.onBakeModels(event);
	}

	private static void setupLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ForestryModelLayers.NATURALIST_CHEST_LAYER, RenderNaturalistChest::createBodyLayer);
		event.registerLayerDefinition(ForestryModelLayers.MILL_LAYER, RenderMill::createBodyLayer);
		event.registerLayerDefinition(ForestryModelLayers.ENGINE_LAYER, RenderEngine::createBodyLayer);
	}

	private static void clientSetupRenderers(EntityRenderersEvent.RegisterRenderers event) {
		// Core
		event.registerBlockEntityRenderer(CoreTiles.ANALYZER.tileType(), RenderAnalyzer::new);
		event.registerBlockEntityRenderer(CoreTiles.ESCRITOIRE.tileType(), RenderEscritoire::new);
		// Apiculture/Arboriculture/Lepidopterology
		event.registerBlockEntityRenderer(CoreTiles.APIARIST_CHEST.tileType(), ctx -> new RenderNaturalistChest(ctx, "apiaristchest"));
		event.registerBlockEntityRenderer(CoreTiles.ARBORIST_CHEST.tileType(), ctx -> new RenderNaturalistChest(ctx, "arbchest"));
		event.registerBlockEntityRenderer(CoreTiles.LEPIDOPTERIST_CHEST.tileType(), ctx -> new RenderNaturalistChest(ctx, "lepichest"));
		// Engine
		event.registerBlockEntityRenderer(EnergyTiles.CLOCKWORK_ENGINE.tileType(), ctx -> new RenderEngine(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_clock_"));
		event.registerBlockEntityRenderer(EnergyTiles.BIOGAS_ENGINE.tileType(), ctx -> new RenderEngine(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_bronze_"));
		event.registerBlockEntityRenderer(EnergyTiles.PEAT_ENGINE.tileType(), ctx -> new RenderEngine(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_copper_"));
		event.registerBlockEntityRenderer(EnergyTiles.COMBUSTION_ENGINE.tileType(), ctx -> new RenderEngine(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_iron_"));
		event.registerBlockEntityRenderer(EnergyTiles.SOLAR_ENGINE.tileType(), ctx -> new RenderEngine(ctx, Constants.TEXTURE_PATH_BLOCK + "/engine_tin_"));
		// Factory
		event.registerBlockEntityRenderer(FactoryTiles.RAINMAKER.tileType(), ctx -> new RenderMill(ctx, Constants.TEXTURE_PATH_BLOCK + "/rainmaker_"));
	}

	// item models of the BER-rendered blocks (replaces the 1.21.1 ForestryBewlr)
	private static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
		event.register(ForestryConstants.forestry("naturalist_chest"), ForestrySpecialModelRenderers.ChestSpecialRenderer.Unbaked.MAP_CODEC);
		event.register(ForestryConstants.forestry("engine"), ForestrySpecialModelRenderers.EngineSpecialRenderer.Unbaked.MAP_CODEC);
		event.register(ForestryConstants.forestry("mill"), ForestrySpecialModelRenderers.MillSpecialRenderer.Unbaked.MAP_CODEC);
	}

	private static void registerReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(ForestryConstants.forestry("colours"), ColourProperties.INSTANCE);

		event.addListener(ForestryConstants.forestry("model_cache"), (sharedState, taskExecutor, barrier, reloadExecutor) -> {
			return barrier.wait(Unit.INSTANCE).thenRunAsync(ModelBlockCached::clear, reloadExecutor);
		});
	}

	private static void registerParticleFactory(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(CoreParticles.REFRACTORY_WAX.get(), RefractoryWaxParticle::new);
	}

	// 26.x: fluid textures are fluid models (were IClientFluidTypeExtensions). The textures are already tinted,
	// so there is no tint source
	private static void registerFluidModels(RegisterFluidModelsEvent event) {
		for (ForestryFluids fluid : ForestryFluids.values()) {
			if (fluid.getFluid().getFluidType() instanceof FeatureFluid.ForestryFluidType forestryType) {
				event.register(new FluidModel.Unbaked(new Material(forestryType.getStillTexture()), new Material(forestryType.getFlowingTexture()), null, (FluidTintSource) null),
					fluid::getFluid, fluid::getFlowing);
			}
		}
	}

	private static void registerItemModels(RegisterItemModelsEvent event) {
		event.register(ForestryConstants.forestry("fluid_container"), FluidContainerModel.Unbaked.MAP_CODEC);
	}

	private static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
		event.register(ForestryConstants.forestry("item_color"), ForestryTints.ItemTint.CODEC);
	}

	private static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
		// Turf wears the grass texture and tints it by biome, the same way vanilla tints its grass block.
		// Deviation from 1.20.1: that tree named the two blocks inside ClientManager's shared block colour,
		// which every other forestry block reaches through IColoredBlock. Neither turf block is one, so they
		// get vanilla's own lambda here instead
		event.register(List.of(BlockTintSources.grass()), CoreBlocks.TURF_BLOCK.block(), CoreBlocks.TURF.block());

		// Packaging machine tank slices, tinted with the fluid colour of whatever they hold
		event.register(ForestryTints.BLOCK, FactoryBlocks.PLAIN.blockArray());
	}

	private static void onRecipesUpdated(RecipesReceivedEvent event) {
		// Forestry's recipe types are synced by ModuleCore#onDatapackSync; rebuild the client-side mutation index
		// for JEI/analyzer display. Species (BeeSpeciesSyncPacket) arrive separately and are rebuilt there.
		ClientsideCode.setRecipeMap(event.getRecipeMap());
		GeneticsReloadHandler.rebuildMutations(event.getRecipeMap());
	}
}
