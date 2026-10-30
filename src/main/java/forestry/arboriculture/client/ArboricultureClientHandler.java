package forestry.arboriculture.client;

import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import forestry.arboriculture.models.SaplingModel;
import forestry.core.platform.models.ForestryTints;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.arboriculture.wood.ForestryWoodType;
import forestry.arboriculture.leaves.DecorativeLeavesBlock;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureTiles;
import forestry.arboriculture.models.*;
import forestry.core.platform.models.ClientManager;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ArboricultureClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ArboricultureClientHandler::registerBlockColors);
		modBus.addListener(ArboricultureClientHandler::registerModelLoaders);
		modBus.addListener(ArboricultureClientHandler::registerStandaloneModels);
		modBus.addListener(ArboricultureClientHandler::registerItemModels);
		modBus.addListener(ArboricultureClientHandler::registerEntityRenderers);
		modBus.addListener(ArboricultureClientHandler::registerModelLayers);
	}

	private static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
		SaplingModel.registerStandaloneModels(event);
	}

	private static void registerItemModels(RegisterItemModelsEvent event) {
		event.register(ForestryConstants.forestry("sapling"), SaplingModel.ITEM.codec());
	}

	private static void registerModelLoaders(ModelEvent.RegisterLoaders event) {

		// Registered here and not in FMLClientSetupEvent: this event is posted at the top of ModelManager.reload,
		// so it is ordered before the ModifyBakingResult that reads these models back. Client setup runs on its own
		// thread during the same reload, and losing that race leaves the leaf blocks on the particle-only models
		// they fall back to, which bake to no quads at all - a see-through block.
		ClientManager clientManager = ClientManager.INSTANCE;
		clientManager.registerModel(new LeavesModel(), ArboricultureBlocks.LEAVES);
		clientManager.registerModel(new DecorativeLeavesModel<>(DecorativeLeavesBlock.class), ArboricultureBlocks.LEAVES_DECORATIVE);
		clientManager.registerModel(new DefaultLeavesModel(), ArboricultureBlocks.LEAVES_DEFAULT);
		clientManager.registerModel(new DefaultFruitLeavesModel(), ArboricultureBlocks.LEAVES_DEFAULT_FRUIT);
		clientManager.registerBlockModel(new SaplingModel(), ArboricultureBlocks.SAPLING_GE.block());
	}

	private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ArboricultureEntities.BOAT.entityType(), ctx -> new ForestryBoatRenderer(ctx, false));
		event.registerEntityRenderer(ArboricultureEntities.CHEST_BOAT.entityType(), ctx -> new ForestryBoatRenderer(ctx, true));
		event.registerBlockEntityRenderer(ArboricultureTiles.SIGN.tileType(), StandingSignRenderer::new);
		// HANGING_SIGN BE renderer is provided by vanilla; Forestry's hanging-sign blocks reuse
		// vanilla's BlockEntityType.HANGING_SIGN via BlockEntityTypeAddBlocksEvent.
	}

	private static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			event.registerLayerDefinition(ForestryBoatRenderer.createBoatModelLocation(type, false), BoatModel::createBoatModel);
			event.registerLayerDefinition(ForestryBoatRenderer.createBoatModelLocation(type, true), BoatModel::createChestBoatModel);
			// Vanilla / NeoForge auto-registers hanging-sign and standing-sign layers for any
			// WoodType added via Sheets.addWoodType (see beforeResourceLoad). Registering them
			// manually here causes a duplicate-key crash at boot.
		}
	}

	private static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
		event.register(ForestryTints.BLOCK, ArboricultureBlocks.LEAVES.block());
		event.register(ForestryTints.BLOCK, ArboricultureBlocks.LEAVES_DEFAULT.blockArray());
		event.register(ForestryTints.BLOCK, ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.blockArray());
		event.register(ForestryTints.BLOCK, ArboricultureBlocks.LEAVES_DECORATIVE.blockArray());
	}

}
