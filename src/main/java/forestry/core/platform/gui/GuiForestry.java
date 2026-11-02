package forestry.core.platform.gui;

import org.joml.Matrix3x2fStack;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import forestry.Forestry;
import forestry.api.ForestryConstants;
import forestry.api.core.climate.IClimateProvider;
import forestry.api.core.IErrorLogicSource;
import forestry.api.core.IErrorSource;
import forestry.core.platform.config.ForestryConfig;
import forestry.core.platform.gui.ledgers.*;
import forestry.core.platform.gui.widgets.TankWidget;
import forestry.core.platform.gui.widgets.Widget;
import forestry.core.platform.gui.widgets.WidgetManager;
import forestry.core.platform.owner.IOwnedTile;
import forestry.core.platform.render.ColourProperties;
import forestry.core.content.energy.ForestryEnergyStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public abstract class GuiForestry<C extends AbstractContainerMenu> extends AbstractContainerScreen<C> implements IGuiSizable {
	// Used to display "Did you know?" ledgers in GUI
	public static final LinkedListMultimap<String, String> HINTS = readDefaultHints();
	public static final int DEFAULT_IMAGE_WIDTH = 176;
	public static final int DEFAULT_IMAGE_HEIGHT = 166;

	public final Identifier textureFile;
	protected final WidgetManager widgetManager;
	protected final LedgerManager ledgerManager;
	protected TextLayoutHelper textLayout;

	protected GuiForestry(String texture, C menu, Inventory inv, Component title) {
		this(ForestryConstants.forestry(texture), menu, inv, title);
	}

	protected GuiForestry(Identifier texture, C menu, Inventory inv, Component title) {
		this(texture, menu, inv, title, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
	}

	// 26.x: imageWidth/imageHeight are final and set through the constructor
	protected GuiForestry(String texture, C menu, Inventory inv, Component title, int imageWidth, int imageHeight) {
		this(ForestryConstants.forestry(texture), menu, inv, title, imageWidth, imageHeight);
	}

	protected GuiForestry(Identifier texture, C menu, Inventory inv, Component title, int imageWidth, int imageHeight) {
		super(menu, inv, title, imageWidth, imageHeight);

		this.widgetManager = new WidgetManager(this);
		this.ledgerManager = new LedgerManager(this);

		this.textureFile = texture;
	}

	/* LEDGERS */
	@Override
	public void init() {
		super.init();

		int maxLedgerWidth = (this.width - this.imageWidth) / 2;

		this.ledgerManager.setMaxWidth(maxLedgerWidth);
		this.ledgerManager.clear();

		this.textLayout = new TextLayoutHelper(this, ColourProperties.INSTANCE);

		addLedgers();
	}

	@Override
	public void containerTick() {
		super.containerTick();
	}

	// 26.x: the background (renderBg in 1.21.1) is extracted in its own stratum before the slots and labels
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		renderBg(graphics, partialTicks, mouseX, mouseY);
	}

	protected abstract void addLedgers();

	protected final void addErrorLedger(IErrorSource errorSource) {
        this.ledgerManager.add(errorSource);
	}

	protected final void addErrorLedger(IErrorLogicSource errorSource) {
        this.ledgerManager.add(errorSource.getErrorLogic());
	}

	protected final void addClimateLedger(IClimateProvider climatised) {
        this.ledgerManager.add(new ClimateLedger(this.ledgerManager, climatised));
	}

	protected final void addPowerLedger(ForestryEnergyStorage energyStorage) {
        this.ledgerManager.add(new PowerLedger(this.ledgerManager, energyStorage));
	}

	protected final void addHintLedger(String hintsKey) {
		if (ForestryConfig.CLIENT.enableHints.get()) {
			List<String> hints = HINTS.get(hintsKey);
			addHintLedger(hints);
		}
	}

	protected final void addHintLedger(List<String> hints) {
		if (ForestryConfig.CLIENT.enableHints.get()) {
			if (!hints.isEmpty()) {
                this.ledgerManager.add(new HintLedger(this.ledgerManager, hints));
			}
		}
	}

	protected final void addOwnerLedger(IOwnedTile ownedTile) {
        this.ledgerManager.add(new OwnerLedger(this.ledgerManager, ownedTile));
	}

	@Override
	public void onClose() {
		super.onClose();
        this.ledgerManager.onClose();
	}

	public ColourProperties getFontColor() {
		return ColourProperties.INSTANCE;
	}

	public Font font() {
		return this.font;
	}

	/**
	 * Ledgers and widgets (and the packets they send, e.g. the worktable recipe click) number mouse buttons
	 * 0 = left, 1 = right, 2 = middle. 26.3 input events use SDL numbers ({@link InputConstants#MOUSE_BUTTON_LEFT} = 1,
	 * {@link InputConstants#MOUSE_BUTTON_RIGHT} = 3), so convert like vanilla's container click button does.
	 */
	public static int legacyMouseButton(MouseButtonEvent event) {
		return switch (event.button()) {
			case InputConstants.MOUSE_BUTTON_LEFT -> 0;
			case InputConstants.MOUSE_BUTTON_RIGHT -> 1;
			default -> event.button();
		};
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		// / Handle ledger clicks
		int mouseButton = legacyMouseButton(event);
		this.ledgerManager.handleMouseClicked(event.x(), event.y(), mouseButton);
		this.widgetManager.handleMouseClicked(event.x(), event.y(), mouseButton);
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.widgetManager.handleMouseRelease(event.x(), event.y(), legacyMouseButton(event))) {
			return true;
		}
		return super.mouseReleased(event);
	}

	@Nullable
	public TankWidget getTankAtPosition(double mouseX, double mouseY) {
		for (Widget widget : this.widgetManager.getWidgets()) {
			if (widget instanceof TankWidget tankWidget && widget.isMouseOver(mouseX - this.leftPos, mouseY - this.topPos)) {
				return tankWidget;
			}
		}
		return null;
	}

	@Nullable
	protected Slot getSlotAtPosition(double mouseX, double mouseY) {
		for (int k = 0; k < this.menu.slots.size(); ++k) {
			Slot slot = this.menu.slots.get(k);

			if (isMouseOverSlot(slot, mouseX, mouseY)) {
				return slot;
			}
		}

		return null;
	}

	private boolean isMouseOverSlot(Slot par1Slot, double mouseX, double mouseY) {
		return isHovering(par1Slot.x, par1Slot.y, 16, 16, mouseX, mouseY);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        this.ledgerManager.drawTooltips(graphics, mouseX, mouseY);

		if (this.menu.getCarried().isEmpty()) {
			GuiUtil.drawToolTips(graphics, this, this.widgetManager.getWidgets(), mouseX, mouseY);
			GuiUtil.drawToolTips(graphics, this, this.renderables, mouseX, mouseY);
			GuiUtil.drawToolTips(graphics, this, this.menu.slots, mouseX, mouseY);
		}
	}

	protected void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
		drawBackground(graphics);

        this.widgetManager.updateWidgets(mouseX - this.leftPos, mouseY - this.topPos);

		Matrix3x2fStack transform = graphics.pose();
		transform.pushMatrix();
		transform.translate(this.leftPos, this.topPos);
		drawWidgets(graphics);
		transform.popMatrix();
	}

	protected void drawBackground(GuiGraphicsExtractor transform) {
		transform.blit(RenderPipelines.GUI_TEXTURED, this.textureFile, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	protected void drawWidgets(GuiGraphicsExtractor graphics) {
        this.ledgerManager.drawLedgers(graphics);
        this.widgetManager.drawWidgets(graphics);
	}

	// 26.x: textures are passed to each blit; kept for subclasses
	protected void bindTexture(Identifier texturePath) {
	}

	@Override
	public int getGuiLeft() {
		return this.leftPos;
	}

	@Override
	public int getGuiTop() {
		return this.topPos;
	}

	@Override
	public int getSizeX() {
		return this.imageWidth;
	}

	@Override
	public int getSizeY() {
		return this.imageHeight;
	}

	@Override
	public Minecraft getGameInstance() {
		return Preconditions.checkNotNull(this.minecraft);
	}

	public List<Rect2i> getExtraGuiAreas() {
		return this.ledgerManager.getLedgerAreas();
	}

	public TextLayoutHelper getTextLayout() {
		return this.textLayout;
	}

	private static LinkedListMultimap<String, String> readDefaultHints() {
		LinkedListMultimap<String, String> map = LinkedListMultimap.create();
		Properties prop = new Properties();

		try {
			InputStream hintStream = GuiForestry.class.getResourceAsStream("/config/forestry/hints.properties");
			prop.load(hintStream);
		} catch (IOException | NullPointerException e) {
			Forestry.LOGGER.error("Failed to load hints file.", e);
		}

		for (String key : prop.stringPropertyNames()) {
			String list = prop.getProperty(key);

			if (!list.isEmpty()) {
				for (String parsedHint : list.split(";+")) {
					map.put(key, parsedHint);
				}
			}
		}

		return map;
	}
}
