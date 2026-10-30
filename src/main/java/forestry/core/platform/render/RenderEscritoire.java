package forestry.core.platform.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.core.content.escritoire.TileEscritoire;
import forestry.core.platform.util.RenderUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RenderEscritoire implements BlockEntityRenderer<TileEscritoire, RenderEscritoire.EscritoireRenderState> {

	private final ItemModelResolver itemModelResolver;

	public RenderEscritoire(BlockEntityRendererProvider.Context ctx) {
		this.itemModelResolver = ctx.itemModelResolver();
	}

	@Override
	public EscritoireRenderState createRenderState() {
		return new EscritoireRenderState();
	}

	@Override
	public void extractRenderState(TileEscritoire escritoire, EscritoireRenderState state, float partialTick, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(escritoire, state, partialTick, camera, breakProgress);
		state.item.clear();
		ItemStack displayStack = escritoire.getIndividualOnDisplay();
		if (!displayStack.isEmpty()) {
			this.itemModelResolver.updateForTopItem(state.item, displayStack, ItemDisplayContext.GROUND, escritoire.getLevel(), null, (int) escritoire.getBlockPos().asLong());
		}
		state.animTime = (int) escritoire.getLevel().getGameTime() + partialTick;
	}

	@Override
	public void submit(EscritoireRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.item.isEmpty()) {
			stack.pushPose();
			stack.translate(0.5, 0.9, 0.5);
			stack.scale(0.75f, 0.75f, 0.75f);
			RenderUtil.submitDisplayStack(stack, state.item, state.animTime, collector, state.lightCoords);
			stack.popPose();
		}
	}

	public static class EscritoireRenderState extends BlockEntityRenderState {
		public final ItemStackRenderState item = new ItemStackRenderState();
		public float animTime;
	}
}
