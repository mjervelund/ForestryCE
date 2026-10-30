package forestry.core.platform.util;

import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Util methods used at the installation of the game or at the reloading or baking of resources like models or
 * textures.
 */
@OnlyIn(Dist.CLIENT)
public class ResourceUtil {

	public static TextureAtlasSprite getMissingTexture() {
		return getBlockSprite(MissingTextureAtlasSprite.getLocation());
	}

	// 26.x: atlases are looked up by atlas id (AtlasIds) in the atlas manager
	public static TextureAtlasSprite getBlockSprite(Identifier location) {
		return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(location);
	}

	public static TextureAtlasSprite getBlockSprite(String location) {
		return getBlockSprite(Identifier.parse(location));
	}

	// 26.x: fluid textures and tints come from the fluid's model instead of IClientFluidTypeExtensions
	public static FluidModel getFluidModel(Fluid fluid) {
		return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
	}

	public static TextureAtlasSprite getStillFluidSprite(Fluid fluid) {
		return getFluidModel(fluid).stillMaterial().sprite();
	}

	public static int getFluidTint(FluidStack stack) {
		FluidTintSource tintSource = getFluidModel(stack.getFluid()).fluidTintSource();
		return tintSource == null ? -1 : tintSource.colorAsStack(stack);
	}

	public static SimpleModelState loadTransform(Identifier location) {
		// todo
		return new SimpleModelState(Transformation.identity());
	}
}
