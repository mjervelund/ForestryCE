package forestry.agriculture.client;

import forestry.api.client.IClientModuleHandler;
import forestry.agriculture.features.MinifarmBlocks;
import forestry.agriculture.features.MinifarmMenuTypes;
import forestry.agriculture.minifarm.gui.MinifarmScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class MinifarmClientHandler implements IClientModuleHandler {
	// 26.x: ItemBlockRenderTypes is gone; the chunk layer comes from each sprite's transparency
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(MinifarmClientHandler::registerMenuScreens);
	}

	private static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(MinifarmMenuTypes.PLANTER.menuType(), MinifarmScreen::new);
	}
}
