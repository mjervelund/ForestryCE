package forestry.core.platform.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.ForestryConstants;
import forestry.core.content.machines.RenderMill;
import forestry.core.platform.config.Constants;
import forestry.core.platform.tile.TemperatureState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Special item model renderers for the blocks that render through a block entity renderer. They replace the
 * 1.21.1 {@code ForestryBewlr} (BlockEntityWithoutLevelRenderer is gone in 26.x): the generated
 * {@code assets/forestry/items/*.json} reference them as {@code forestry:naturalist_chest},
 * {@code forestry:engine} and {@code forestry:mill} special models.
 */
public class ForestrySpecialModelRenderers {

	public record ChestSpecialRenderer(RenderNaturalistChest.NaturalistChestModel model, Identifier texture) implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack stack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
			collector.submitModel(this.model, 0.0F, stack, this.model.renderType(this.texture), light, overlay, outlineColor);
		}

		@Override
		public void getExtents(Consumer<Vector3fc> output) {
			this.model.root().getExtentsForGui(new PoseStack(), output);
		}

		public record Unbaked(String texture) implements NoDataSpecialModelRenderer.Unbaked {
			public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
					Codec.STRING.fieldOf("texture").forGetter(Unbaked::texture)
			).apply(builder, Unbaked::new));

			@Override
			public NoDataSpecialModelRenderer bake(SpecialModelRenderer.BakingContext context) {
				ModelPart root = context.entityModelSet().bakeLayer(ForestryModelLayers.NATURALIST_CHEST_LAYER);
				return new ChestSpecialRenderer(new RenderNaturalistChest.NaturalistChestModel(root), ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/" + this.texture + ".png"));
			}

			@Override
			public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
				return MAP_CODEC;
			}
		}
	}

	public record EngineSpecialRenderer(RenderEngine renderer) implements NoDataSpecialModelRenderer {
		// the resting piston step: getPistonStep at progress 0.25 (what the old item render showed)
		private static final float IDLE_STEP = 3.0F;

		@Override
		public void submit(PoseStack stack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
			this.renderer.submitIdle(stack, collector, light, IDLE_STEP);
		}

		@Override
		public void getExtents(Consumer<Vector3fc> output) {
			this.renderer.getItemExtents(output);
		}

		public record Unbaked(String base) implements NoDataSpecialModelRenderer.Unbaked {
			public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
					Codec.STRING.fieldOf("base").forGetter(Unbaked::base)
			).apply(builder, Unbaked::new));

			@Override
			public NoDataSpecialModelRenderer bake(SpecialModelRenderer.BakingContext context) {
				ModelPart root = context.entityModelSet().bakeLayer(ForestryModelLayers.ENGINE_LAYER);
				return new EngineSpecialRenderer(new RenderEngine(root, this.base));
			}

			@Override
			public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
				return MAP_CODEC;
			}
		}
	}

	public record MillSpecialRenderer(RenderMill renderer) implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack stack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
			this.renderer.submitIdle(stack, collector, light);
		}

		@Override
		public void getExtents(Consumer<Vector3fc> output) {
			this.renderer.getItemExtents(output);
		}

		public record Unbaked(String base) implements NoDataSpecialModelRenderer.Unbaked {
			public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
					Codec.STRING.fieldOf("base").forGetter(Unbaked::base)
			).apply(builder, Unbaked::new));

			@Override
			public NoDataSpecialModelRenderer bake(SpecialModelRenderer.BakingContext context) {
				ModelPart root = context.entityModelSet().bakeLayer(ForestryModelLayers.MILL_LAYER);
				return new MillSpecialRenderer(new RenderMill(root, this.base));
			}

			@Override
			public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
				return MAP_CODEC;
			}
		}
	}
}
