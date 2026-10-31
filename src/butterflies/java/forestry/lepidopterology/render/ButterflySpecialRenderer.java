package forestry.lepidopterology.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.lepidopterology.IButterflyClientManager;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.alleles.ButterflyChromosomes;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.core.platform.util.ResourceUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * {@code forestry:butterfly}: the GE butterfly item. Replaces the 1.21.1 {@code forestry:butterfly_ge} geometry
 * loader ({@code ButterflyItemModel}), which baked {@code item/butterfly_base} once per species texture and
 * scaled it by the butterfly's size. The generated {@code items/*.json} uses {@code item/butterfly_base} as the
 * special model's base, so its display transforms still apply; this draws its two wings and the body with the
 * species' item sprite from the blocks atlas, scaled by size around the block centre.
 */
public class ButterflySpecialRenderer implements SpecialModelRenderer<ButterflySpecialRenderer.Argument> {

	public record Argument(Identifier sprite, float size) {
	}

	@Override
	public @Nullable Argument extractArgument(ItemStack stack) {
		IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
		if (individual == null) {
			return null;
		}
		IGenome genome = individual.getGenome();
		IButterflySpecies species = genome.resolveActive(ButterflyChromosomes.SPECIES);
		float size = genome.getActiveValue(ButterflyChromosomes.SIZE);
		return new Argument(IForestryClientApi.INSTANCE.getButterflyManager().getTextures(species).getFirst(), size);
	}

	@Override
	public void submit(@Nullable Argument argument, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
		IButterflyClientManager manager = IForestryClientApi.INSTANCE.getButterflyManager();
		Identifier spriteId = argument != null ? argument.sprite() : manager.getDefaultTextures().getFirst();
		float size = argument != null ? argument.size() : 1.0F;
		TextureAtlasSprite sprite = ResourceUtil.getBlockSprite(spriteId);

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.scale(size, size, size);
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
			// coordinates and UVs in pixels, as in models/item/butterfly_base.json
			// left wing: down uv [0,0,7,13], up uv [9,0,16,13] rotated 180
			down(buffer, pose, sprite, light, overlay, 0.5F, 7.5F, 14F, 1F, 13.5F, 0, 0, 7, 13);
			up(buffer, pose, sprite, light, overlay, 0.5F, 7.5F, 14.001F, 1F, 13.5F, 16, 13, 9, 0);
			// right wing: down uv [9,0,16,13], up uv [0,0,7,13] rotated 180
			down(buffer, pose, sprite, light, overlay, 8.5F, 15.5F, 14F, 1F, 13.5F, 9, 0, 16, 13);
			up(buffer, pose, sprite, light, overlay, 8.5F, 15.5F, 14.001F, 1F, 13.5F, 7, 13, 0, 0);
			// body (the old model's 45 degree tilted bar, drawn flat between the wings)
			down(buffer, pose, sprite, light, overlay, 7.5F, 8.5F, 13.5F, 3.5F, 9.5F, 7, 7, 8, 13);
			up(buffer, pose, sprite, light, overlay, 7.5F, 8.5F, 14.5F, 3.5F, 9.5F, 8, 7, 9, 13);
		});
		poseStack.popPose();
	}

	// vertex order and UV mapping of vanilla's UP face: u follows x, v follows z
	private static void up(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int light, int overlay,
						   float x0, float x1, float y, float z0, float z1, float u0, float v0, float u1, float v1) {
		vertex(buffer, pose, sprite, light, overlay, x0, y, z0, u0, v0, 1);
		vertex(buffer, pose, sprite, light, overlay, x0, y, z1, u0, v1, 1);
		vertex(buffer, pose, sprite, light, overlay, x1, y, z1, u1, v1, 1);
		vertex(buffer, pose, sprite, light, overlay, x1, y, z0, u1, v0, 1);
	}

	// vertex order and UV mapping of vanilla's DOWN face: u follows x, v runs from z1 to z0
	private static void down(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int light, int overlay,
							 float x0, float x1, float y, float z0, float z1, float u0, float v0, float u1, float v1) {
		vertex(buffer, pose, sprite, light, overlay, x0, y, z1, u0, v0, -1);
		vertex(buffer, pose, sprite, light, overlay, x0, y, z0, u0, v1, -1);
		vertex(buffer, pose, sprite, light, overlay, x1, y, z0, u1, v1, -1);
		vertex(buffer, pose, sprite, light, overlay, x1, y, z1, u1, v0, -1);
	}

	private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int light, int overlay,
							   float x, float y, float z, float u, float v, float normalY) {
		buffer.addVertex(pose, x / 16F, y / 16F, z / 16F)
				.setColor(-1)
				.setUv(sprite.getU(u / 16F), sprite.getV(v / 16F))
				.setOverlay(overlay)
				.setLight(light)
				.setNormal(pose, 0, normalY, 0);
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		for (float x : new float[]{0.5F / 16F, 15.5F / 16F}) {
			for (float y : new float[]{13.5F / 16F, 14.5F / 16F}) {
				for (float z : new float[]{1F / 16F, 13.5F / 16F}) {
					output.accept(new Vector3f(x, y, z));
				}
			}
		}
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<Argument> {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Override
		public SpecialModelRenderer<Argument> bake(SpecialModelRenderer.BakingContext context) {
			return new ButterflySpecialRenderer();
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
