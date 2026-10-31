package forestry.lepidopterology.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.api.client.IForestryClientApi;
import forestry.core.platform.render.ForestryModelLayers;
import forestry.lepidopterology.entities.EntityButterfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class ButterflyEntityRenderer extends MobRenderer<EntityButterfly, ButterflyRenderState, ButterflyModel> {
	public ButterflyEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new ButterflyModel(context.bakeLayer(ForestryModelLayers.BUTTERFLY_LAYER)), 0.25f);
	}

	@Override
	public ButterflyRenderState createRenderState() {
		return new ButterflyRenderState();
	}

	@Override
	public void extractRenderState(EntityButterfly entity, ButterflyRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.renderable = entity.isRenderable();
		state.size = entity.getSize();
		state.wingFlap = entity.getWingFlap(partialTicks);
		state.texture = entity.getTexture();
	}

	@Override
	public void submit(ButterflyRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.renderable) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0, 0.2, 0);
		super.submit(state, poseStack, collector, camera);
		poseStack.popPose();
	}

	@Override
	protected void scale(ButterflyRenderState state, PoseStack poseStack) {
		// The old model applied this scale after the renderer's MODEL_Y_OFFSET translation; undo and redo that
		// offset around it so the butterfly sits where it did.
		poseStack.translate(0.0F, EntityModel.MODEL_Y_OFFSET, 0.0F);
		poseStack.scale(state.size, state.size, state.size);
		poseStack.translate(0.0F, 1.45f / state.size, 0.0F);
		poseStack.translate(0.0F, -EntityModel.MODEL_Y_OFFSET, 0.0F);
	}

	@Override
	public Identifier getTextureLocation(ButterflyRenderState state) {
		if (state.texture != null) {
			return state.texture;
		}
		return IForestryClientApi.INSTANCE.getButterflyManager().getDefaultTextures().getSecond();
	}
}
