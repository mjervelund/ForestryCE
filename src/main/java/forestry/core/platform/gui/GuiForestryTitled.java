package forestry.core.platform.gui;

import forestry.core.platform.render.ColourProperties;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class GuiForestryTitled<C extends AbstractContainerMenu> extends GuiForestry<C> {
	protected GuiForestryTitled(String texture, C container, Inventory inv, Component title) {
		super(texture, container, inv, title);
	}

	// For Forestry addons
	protected GuiForestryTitled(Identifier texture, C container, Inventory inv, Component title) {
		super(texture, container, inv, title);
	}

	protected GuiForestryTitled(String texture, C container, Inventory inv, Component title, int imageWidth, int imageHeight) {
		super(texture, container, inv, title, imageWidth, imageHeight);
	}

	protected GuiForestryTitled(Identifier texture, C container, Inventory inv, Component title, int imageWidth, int imageHeight) {
		super(texture, container, inv, title, imageWidth, imageHeight);
	}

	@Override
	protected void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

        this.textLayout.line = 6;
		if (centeredTitle()) {
            this.textLayout.drawCenteredLine(graphics, this.title, 0, ColourProperties.INSTANCE.get("gui.title"));
		} else {
            this.textLayout.drawLine(graphics, this.title.getString(), 8, ColourProperties.INSTANCE.get("gui.title"));
		}
	}

	protected boolean centeredTitle() {
		return true;
	}
}
