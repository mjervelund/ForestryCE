package forestry.core.platform.gui.widgets;

import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.core.IToolPipette;
import forestry.api.core.tooltips.ToolTip;
import forestry.core.platform.fluids.StandardTank;
import forestry.core.platform.gui.IContainerLiquidTanks;
import forestry.core.platform.gui.IContainerTank;
import forestry.core.platform.util.ResourceUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import forestry.core.platform.legacy.fluids.IFluidTank;

import javax.annotation.Nullable;

/**
 * Slot for liquid tanks
 */
@OnlyIn(Dist.CLIENT)
public class TankWidget extends Widget {

	private int overlayTexX = 176;
	private int overlayTexY = 0;
	private final int slot;
	protected boolean drawOverlay = true;

	public TankWidget(WidgetManager manager, int xPos, int yPos, int slot) {
		super(manager, xPos, yPos);
		this.slot = slot;
		this.height = 58;
	}

	public TankWidget setOverlayOrigin(int x, int y) {
        this.overlayTexX = x;
        this.overlayTexY = y;
		return this;
	}

	@Nullable
	public IFluidTank getTank() {
		AbstractContainerMenu container = this.manager.gui.getMenu();
		if (container instanceof IContainerTank tanks) {
			return tanks.getTank(this.slot);
		}
		return null;
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, int startX, int startY) {
		IFluidTank tank = getTank();

		if (tank == null || tank.getCapacity() <= 0) {
			return;
		}

		FluidStack contents = tank.getFluid();

		if (!contents.isEmpty() && contents.getAmount() > 0 && contents.getFluid() != null) {
			Fluid fluid = contents.getFluid();

			if (fluid != null) {
				TextureAtlasSprite fluidStillSprite = ResourceUtil.getStillFluidSprite(fluid);
				int fluidColor = ARGB.opaque(ResourceUtil.getFluidTint(contents));

				int scaledAmount = contents.getAmount() * this.height / tank.getCapacity();
				if (contents.getAmount() > 0 && scaledAmount < 1) {
					scaledAmount = 1;
				}
				if (scaledAmount > this.height) {
					scaledAmount = this.height;
				}

				// 26.x: no immediate-mode tesselator in GUIs; whole 16x16 tiles are blitted from the bottom up
				// and clipped to the fluid level (1.21.1 cut the UVs of the top and right tiles instead)
				final int left = startX + this.xPos;
				final int bottom = startY + this.yPos + this.height;
				graphics.enableScissor(left, bottom - scaledAmount, left + this.width, bottom);
				for (int x = 0; x < this.width; x += 16) {
					for (int y = 16; y < scaledAmount + 16; y += 16) {
						graphics.blitSprite(RenderPipelines.GUI_TEXTURED, fluidStillSprite, left + x, bottom - y, 16, 16, fluidColor);
					}
				}
				graphics.disableScissor();
			}
		}

		if (this.drawOverlay) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.manager.gui.textureFile, startX + this.xPos, startY + this.yPos, this.overlayTexX, this.overlayTexY, 16, 60, 256, 256);
			// RenderSystem.disableAlphaTest();
		}

		//RenderSystem.enableBlend();
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		IFluidTank tank = getTank();
		if (!(tank instanceof StandardTank standardTank)) {
			return null;
		}
		return standardTank.getToolTip();
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		Player player = this.manager.minecraft.player;
		ItemStack itemstack = player.containerMenu.getCarried();
		if (itemstack.isEmpty()) {
			return;
		}

		Item held = itemstack.getItem();
		AbstractContainerMenu container = this.manager.gui.getMenu();
		if (held instanceof IToolPipette && container instanceof IContainerLiquidTanks) {
			((IContainerLiquidTanks) container).handlePipetteClickClient(this.slot, player);
		}
	}
}
