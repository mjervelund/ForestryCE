package forestry.apiculture.proxy;

import forestry.api.apiculture.genetics.BeeLifeStage;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import forestry.core.platform.models.ForestryTints;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.alveary.AlvearyScreen;
import forestry.apiculture.alveary.AlvearyHygroregulatorScreen;
import forestry.apiculture.alveary.AlvearySieveScreen;
import forestry.apiculture.alveary.AlvearySwarmerScreen;
import forestry.apiculture.bees.BeeHousingScreen;
import forestry.apiculture.models.ModelBee;
import forestry.apiculture.particles.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.bus.api.IEventBus;

public class ApicultureClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ApicultureClientHandler::registerBlockColors);
		modBus.addListener(ApicultureClientHandler::registerMenus);
		modBus.addListener(ApicultureClientHandler::registerParticleFactory);
		modBus.addListener(ApicultureClientHandler::handleSprites);
		modBus.addListener(ApicultureClientHandler::registerModelLoaders);
	}

	private static void registerMenus(RegisterMenuScreensEvent event) {
		event.register(ApicultureMenuTypes.ALVEARY.menuType(), AlvearyScreen::new);
		event.register(ApicultureMenuTypes.ALVEARY_HYGROREGULATOR.menuType(), AlvearyHygroregulatorScreen::new);
		event.register(ApicultureMenuTypes.ALVEARY_SIEVE.menuType(), AlvearySieveScreen::new);
		event.register(ApicultureMenuTypes.ALVEARY_SWARMER.menuType(), AlvearySwarmerScreen::new);
		event.register(ApicultureMenuTypes.BEE_HOUSING.menuType(), BeeHousingScreen::new);
	}

	private static void registerParticleFactory(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ApicultureParticles.BEE_EXPLORER_PARTICLE.value(), sprites -> (data, level, x, y, z, xSpeed, ySpeed, zSpeed, random) -> new BeeExploreParticle(level, x, y, z, data.destination(), data.color(), sprites.get(random)));
		event.registerSpriteSet(ApicultureParticles.BEE_ROUND_TRIP_PARTICLE.value(), sprites -> (data, level, x, y, z, xSpeed, ySpeed, zSpeed, random) -> new BeeRoundTripParticle(level, x, y, z, data.destination(), data.color(), sprites.get(random)));
		event.registerSpriteSet(ApicultureParticles.BEE_TARGET_ENTITY_PARTICLE.value(), sprites -> (data, level, x, y, z, xSpeed, ySpeed, zSpeed, random) -> {
			Entity entity = level.getEntity(data.entity());
			return entity == null ? null : new BeeTargetEntityParticle(level, x, y, z, entity, data.color(), sprites.get(random));
		});
	}

	private static void handleSprites(TextureAtlasStitchedEvent event) {
		TextureAtlas map = event.getAtlas();
		if (map.location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			for (int i = 0; i < SnowParticle.SPRITES.length; i++) {
				SnowParticle.SPRITES[i] = map.getSprite(ForestryConstants.forestry("snow." + (i + 1)));
			}
		}
	}

	private static void registerModelLoaders(RegisterItemModelsEvent event) {
		for (BeeLifeStage stage : BeeLifeStage.values()) {
			event.register(ModelBee.typeId(stage), ModelBee.kind(stage).codec());
		}
	}

	private static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
		event.register(ForestryTints.BLOCK, ApicultureBlocks.COMB_BLOCK.blockArray());
	}

}
