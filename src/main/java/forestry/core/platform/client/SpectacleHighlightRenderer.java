package forestry.core.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import forestry.api.ForestryConstants;
import forestry.api.core.ISpectacleBlock;
import forestry.core.platform.util.GeneticsUtil;
import forestry.core.platform.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.awt.Color;

/**
 * Spectacles: outlines pollinated leaves, wild hives and multiblock parts through walls.
 * <p>
 * 26.x: 1.21.1 drew these in RenderLevelStageEvent with a RenderType built from RenderStateShards (gone). Now the
 * lines are submitted as custom geometry with a lines pipeline that skips the depth test.
 */
public final class SpectacleHighlightRenderer {
	public static final RenderPipeline LINES_XRAY_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
		.withLocation(ForestryConstants.forestry("pipeline/lines_xray"))
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
		.build();
	public static final RenderType LINES_XRAY = RenderType.create("forestry_lines_xray",
		RenderSetup.builder(LINES_XRAY_PIPELINE).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());

	private SpectacleHighlightRenderer() {
	}

	public static void registerPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(LINES_XRAY_PIPELINE);
	}

	public static void submit(SubmitCustomGeometryEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || minecraft.level == null || !GeneticsUtil.hasNaturalistEye(player)) {
			return;
		}

		PoseStack stack = event.getPoseStack();
		Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
		float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		long gameTime = minecraft.level.getGameTime();

		// Steady rainbow outline (pollinated leaves, assembled multiblock anchors)...
		Color color = RenderUtil.getRainbowColor(gameTime, partialTick);
		int rainbow = ARGB.color(255, color.getRed(), color.getGreen(), color.getBlue());
		// ...and a pulsing white outline for work-in-progress markers (unformed multiblock parts).
		int flashing = ARGB.white(RenderUtil.getFlashingAlpha(gameTime, partialTick));

		int renderDistance = minecraft.options.renderDistance().get();
		ChunkPos playerChunkPos = ChunkPos.containing(player.blockPosition());

		for (int chunkX = playerChunkPos.x() - renderDistance; chunkX <= playerChunkPos.x() + renderDistance; chunkX++) {
			for (int chunkZ = playerChunkPos.z() - renderDistance; chunkZ <= playerChunkPos.z() + renderDistance; chunkZ++) {
				LevelChunk chunk = minecraft.level.getChunk(chunkX, chunkZ);
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					if (be instanceof ISpectacleBlock naturalist && naturalist.isHighlighted(player)) {
						BlockPos pos = be.getBlockPos();
						int boxColor = naturalist.usesFlashingHighlight(player) ? flashing : rainbow;

						stack.pushPose();
						// Translate the matrix stack to avoid floating point precision errors
						stack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
						// inflate slightly to avoid weirdness with the selection box
						event.getSubmitNodeCollector().submitCustomGeometry(stack, LINES_XRAY, (pose, buffer) ->
							lineBox(pose, buffer, -0.001f, -0.001f, -0.001f, 1.001f, 1.001f, 1.001f, boxColor));
						stack.popPose();
					}
				}
			}
		}
	}

	private static void lineBox(PoseStack.Pose pose, VertexConsumer buffer, float x0, float y0, float z0, float x1, float y1, float z1, int color) {
		float[][] corners = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
		int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
		float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
		for (int[] edge : edges) {
			float[] a = corners[edge[0]];
			float[] b = corners[edge[1]];
			float nx = Math.signum(b[0] - a[0]);
			float ny = Math.signum(b[1] - a[1]);
			float nz = Math.signum(b[2] - a[2]);
			buffer.addVertex(pose, a[0], a[1], a[2]).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
			buffer.addVertex(pose, b[0], b[1], b[2]).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
		}
	}
}
