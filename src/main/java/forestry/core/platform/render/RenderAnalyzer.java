package forestry.core.platform.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.core.content.analyzer.TileAnalyzer;
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

public class RenderAnalyzer implements BlockEntityRenderer<TileAnalyzer, RenderAnalyzer.AnalyzerRenderState> {

	private final ItemModelResolver itemModelResolver;

	public RenderAnalyzer(BlockEntityRendererProvider.Context ctx) {
		this.itemModelResolver = ctx.itemModelResolver();
	}

	@Override
	public AnalyzerRenderState createRenderState() {
		return new AnalyzerRenderState();
	}

	@Override
	public void extractRenderState(TileAnalyzer analyzer, AnalyzerRenderState state, float partialTick, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(analyzer, state, partialTick, camera, breakProgress);
		state.item.clear();
		ItemStack displayStack = analyzer.getIndividualOnDisplay();
		if (!displayStack.isEmpty()) {
			this.itemModelResolver.updateForTopItem(state.item, displayStack, ItemDisplayContext.GROUND, analyzer.getLevel(), null, (int) analyzer.getBlockPos().asLong());
		}
		state.animTime = (int) analyzer.getLevel().getGameTime() + partialTick;
	}

	@Override
	public void submit(AnalyzerRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.item.isEmpty()) {
			stack.pushPose();
			stack.translate(0.5f, 0.2f, 0.5f);

			RenderUtil.submitDisplayStack(stack, state.item, state.animTime, collector, state.lightCoords);

			stack.popPose();
		}
	}

	public static class AnalyzerRenderState extends BlockEntityRenderState {
		public final ItemStackRenderState item = new ItemStackRenderState();
		public float animTime;
	}
}
