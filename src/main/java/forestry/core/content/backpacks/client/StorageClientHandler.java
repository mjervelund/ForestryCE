package forestry.core.content.backpacks.client;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.core.platform.gui.GuiNaturalistInventory;
import forestry.core.content.backpacks.features.BackpackMenuTypes;
import forestry.core.content.backpacks.gui.ContainerNaturalistBackpack;
import forestry.core.content.backpacks.gui.GuiBackpack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;

public class StorageClientHandler implements IClientModuleHandler {

	// 26.x: the filled crate is a data-driven composite item model (base crate + tinted contents layers),
	// and the backpack mode switch is a select property; the 1.21.1 geometry loader and
	// ItemProperties registration are gone.
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(StorageClientHandler::registerSelectProperties);
		modBus.addListener(StorageClientHandler::onClientSetup);
	}

	private static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
		event.register(ForestryConstants.forestry("backpack_mode"), BackpackModeProperty.TYPE);
	}

	private static void onClientSetup(RegisterMenuScreensEvent event) {
		event.register(BackpackMenuTypes.BACKPACK.menuType(), GuiBackpack::new);
		event.register(BackpackMenuTypes.NATURALIST_BACKPACK.menuType(), GuiNaturalistInventory<ContainerNaturalistBackpack>::new);
	}
}
