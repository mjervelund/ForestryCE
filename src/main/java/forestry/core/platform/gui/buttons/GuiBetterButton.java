package forestry.core.platform.gui.buttons;

import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.ForestryConstants;
import forestry.core.platform.config.Constants;
import forestry.core.platform.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiBetterButton extends Button {
	public static final Identifier TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/buttons.png");

	protected final IButtonTextureSet texture;

	public GuiBetterButton(int x, int y, IButtonTextureSet texture, OnPress handler) {
		super(x, y, texture.getWidth(), texture.getHeight(), Component.empty(), handler, DEFAULT_NARRATION);
		this.texture = texture;
	}

	// 26.x: Button is abstract; the content is drawn here (the label is always empty)
	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mX, int mY, float partialTick) {
		int xOffset = this.texture.getX();
		int yOffset = this.texture.getY();
		int h = this.height;
		int w = this.width;

		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), xOffset, yOffset + RenderUtil.getYImage(this) * h, w, h, 256, 256, ARGB.white(this.alpha));
	}
}
