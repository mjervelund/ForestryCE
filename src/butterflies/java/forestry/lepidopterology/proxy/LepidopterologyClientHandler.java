package forestry.lepidopterology.proxy;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.core.platform.render.ForestryModelLayers;
import forestry.lepidopterology.features.LepidopterologyEntities;
import forestry.lepidopterology.render.ButterflyEntityRenderer;
import forestry.lepidopterology.render.ButterflyModel;
import forestry.lepidopterology.render.ButterflySpecialRenderer;
import forestry.lepidopterology.render.CocoonAgeProperty;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.bus.api.IEventBus;

public class LepidopterologyClientHandler implements IClientModuleHandler {
	// 26.x: item colors come from forestry:item_color tints in the generated items JSON, the butterfly item is
	// the forestry:butterfly special model and the cocoon age switch is a select property; the 1.21.1 color
	// handlers, geometry loader and ItemProperties registration are gone.
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(LepidopterologyClientHandler::setupRenderers);
		modBus.addListener(LepidopterologyClientHandler::setupLayers);
		modBus.addListener(LepidopterologyClientHandler::registerSpecialModelRenderers);
		modBus.addListener(LepidopterologyClientHandler::registerSelectProperties);
	}

	private static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
		event.register(ForestryConstants.forestry("cocoon_age"), CocoonAgeProperty.TYPE);
	}

	private static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
		event.register(ForestryConstants.forestry("butterfly"), ButterflySpecialRenderer.Unbaked.MAP_CODEC);
	}

	public static void setupRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(LepidopterologyEntities.BUTTERFLY.entityType(), ButterflyEntityRenderer::new);
	}

	public static void setupLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ForestryModelLayers.BUTTERFLY_LAYER, ButterflyModel::createLayer);
	}
}
