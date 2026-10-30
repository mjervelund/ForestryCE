package forestry.core.content.machines;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import forestry.api.ForestryConstants;
import forestry.core.content.machines.TileMill;
import forestry.core.platform.util.RenderUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import forestry.core.platform.render.ForestryModelLayers;

// This is called Mill because it used to be for the Forester and Treetap blocks in older versions of Forestry
public class RenderMill implements BlockEntityRenderer<TileMill, RenderMill.MillRenderState> {
	private enum Textures {PEDESTAL, EXTENSION, BLADE, CHARGE}

	private final Identifier[] textures;
	private final ModelPart pedestal;
	private final ModelPart column;
	private final ModelPart extension;
	private final ModelPart blade;

	public RenderMill(BlockEntityRendererProvider.Context ctx, String baseTexture) {
		this(ctx.bakeLayer(ForestryModelLayers.MILL_LAYER), baseTexture);
	}

	public RenderMill(ModelPart root, String baseTexture) {
		this.pedestal = root.getChild(Textures.PEDESTAL.name());
		this.column = root.getChild(Textures.CHARGE.name());
		this.extension = root.getChild(Textures.EXTENSION.name());
		this.blade = root.getChild(Textures.BLADE.name());

		this.textures = new Identifier[11];

		this.textures[Textures.PEDESTAL.ordinal()] = ForestryConstants.forestry(baseTexture + "pedestal.png");
		this.textures[Textures.EXTENSION.ordinal()] = ForestryConstants.forestry(baseTexture + "extension.png");
		this.textures[Textures.BLADE.ordinal()] = ForestryConstants.forestry(baseTexture + "blade.png");

		for (int i = 0; i < 8; i++) {
			this.textures[Textures.CHARGE.ordinal() + i] = ForestryConstants.forestry(baseTexture + "column_" + i + ".png");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild(Textures.PEDESTAL.name(), CubeListBuilder.create().texOffs(0, 0)
			.addBox(0f, 0f, 0f, 16, 1, 16), PartPose.offset(0, 0, 0));
		partdefinition.addOrReplaceChild(Textures.CHARGE.name(), CubeListBuilder.create().texOffs(0, 0)
			.addBox(0f, 0f, 0f, 4, 15, 4), PartPose.offset(6, 1, 6));
		partdefinition.addOrReplaceChild(Textures.EXTENSION.name(), CubeListBuilder.create().texOffs(0, 0)
			.addBox(0f, 0f, 0f, 14, 2, 2), PartPose.offset(1, 8, 7));
		partdefinition.addOrReplaceChild(Textures.BLADE.name(), CubeListBuilder.create().texOffs(0, 0)
			.addBox(0f, 0f, 0f, 1, 12, 8), PartPose.offset(10, 3, 4));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public MillRenderState createRenderState() {
		return new MillRenderState();
	}

	@Override
	public void extractRenderState(TileMill mill, MillRenderState state, float partialTick, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(mill, state, partialTick, camera, breakProgress);
		state.charge = mill.charge;
		state.bladeStep = getBladeStep(mill, partialTick);
	}

	@Override
	public void submit(MillRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		submitMill(this.pedestal, this.column, this.extension, this.blade, this.textures, state.charge, state.bladeStep, stack, collector, state.lightCoords);
	}

	// item/special model render: a resting mill
	public void submitIdle(PoseStack stack, SubmitNodeCollector collector, int light) {
		submitMill(this.pedestal, this.column, this.extension, this.blade, this.textures, 0, 0.0f, stack, collector, light);
	}

	public void getItemExtents(java.util.function.Consumer<org.joml.Vector3fc> output) {
		PoseStack stack = new PoseStack();
		this.pedestal.getExtentsForGui(stack, output);
		this.column.getExtentsForGui(stack, output);
		this.extension.getExtentsForGui(stack, output);
		this.blade.getExtentsForGui(stack, output);
	}

	static void submitMill(ModelPart pedestal, ModelPart column, ModelPart extension, ModelPart blade, Identifier[] textures, int charge, float bladeStep, PoseStack stack, SubmitNodeCollector collector, int light) {
		stack.pushPose();

		// todo implement and add rotated voxel shape
		Direction orientation = Direction.SOUTH;//mill.getBlockState().getValue(BlockBase.FACING);
		RenderUtil.rotateByHorizontalDirection(stack, orientation);

		int overlay = OverlayTexture.NO_OVERLAY;
		collector.submitModelPart(pedestal, stack, RenderTypes.entityCutout(textures[Textures.PEDESTAL.ordinal()]), light, overlay, null);
		collector.submitModelPart(column, stack, RenderTypes.entityCutout(textures[Textures.CHARGE.ordinal() + charge]), light, overlay, null);
		collector.submitModelPart(extension, stack, RenderTypes.entityCutout(textures[Textures.EXTENSION.ordinal()]), light, overlay, null);

		float step = bladeStep / 16f;
		RenderType bladeType = RenderTypes.entityCutout(textures[Textures.BLADE.ordinal()]);
		stack.pushPose();
		stack.translate(step, 0, 0);
		collector.submitModelPart(blade, stack, bladeType, light, overlay, null);
		stack.popPose();
		stack.translate(1, 0, 1);
		stack.rotate(Axis.YP, Mth.PI);
		stack.translate(step, 0, 0);
		collector.submitModelPart(blade, stack, bladeType, light, overlay, null);

		stack.popPose();
	}

	private static float getBladeStep(TileMill mill, float partialTick) {
		float progress;

		if (mill.hasLevel()) {
			progress = mill.progress;
			if (mill.stage != 0) {
				float smoothing = mill.speed * partialTick;
				progress = (progress + smoothing);
			}
		} else {
			progress = 0.0f;
		}

		if (progress > 0.5f) {
			return 3.99f - (progress - 0.5f) * 2f * 3.99f;
		} else {
			return progress * 2f * 3.99f;
		}
	}

	public static class MillRenderState extends BlockEntityRenderState {
		public int charge;
		public float bladeStep;
	}
}
