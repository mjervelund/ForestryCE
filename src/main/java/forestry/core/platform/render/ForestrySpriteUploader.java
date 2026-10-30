package forestry.core.platform.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

/**
 * Looks up the forestry gui icon sprites.
 * <p>
 * 26.x: TextureAtlasHolder is gone. The icons (textures/forestry/atlas/gui) are added to vanilla's gui sprite atlas
 * by assets/minecraft/atlases/gui.json, with the same sprite ids the forestry gui atlas had, so they can also be
 * drawn with GuiGraphicsExtractor#blitSprite and used as slot backgrounds.
 *
 * @see ForestryTextureManager
 */
public class ForestrySpriteUploader {
	public static final Identifier ATLAS_PATH = AtlasIds.GUI;

	public TextureAtlasSprite getSprite(Identifier location) {
		return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(location);
	}
}
