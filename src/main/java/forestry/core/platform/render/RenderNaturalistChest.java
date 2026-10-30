package forestry.core.platform.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.api.ForestryConstants;
import forestry.core.platform.block.BlockBase;
import forestry.core.platform.config.Constants;
import forestry.core.platform.tile.TileNaturalistChest;
import forestry.core.platform.util.RenderUtil;
import net.minecraft.client.model.Model;
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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RenderNaturalistChest implements BlockEntityRenderer<TileNaturalistChest, RenderNaturalistChest.ChestRenderState> {
	private static final String LID = "lid";
	private static final String BASE = "base";
	private static final String LOCK = "lock";

	private final NaturalistChestModel model;
	private final Identifier texture;

	public RenderNaturalistChest(BlockEntityRendererProvider.Context ctx, String textureName) {
		this(ctx.bakeLayer(ForestryModelLayers.NATURALIST_CHEST_LAYER), textureName);
	}

	public RenderNaturalistChest(ModelPart root, String textureName) {
		this.model = new NaturalistChestModel(root);
		this.texture = ForestryConstants.forestry(Constants.TEXTURE_PATH_BLOCK + "/" + textureName + ".png");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild(BASE, CubeListBuilder.create().texOffs(0, 19)
			.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.offset(0, 0, 0));
		partdefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0)
			.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0, 9.0F, 1.0F));
		partdefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0)
			.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0, 8.0F, 0));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public ChestRenderState createRenderState() {
		return new ChestRenderState();
	}

	@Override
	public void extractRenderState(TileNaturalistChest chest, ChestRenderState state, float partialTick, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(chest, state, partialTick, camera, breakProgress);
		state.facing = chest.getBlockState().getValue(BlockBase.FACING);
		float angle = chest.prevLidAngle + (chest.lidAngle - chest.prevLidAngle) * partialTick;
		angle = 1.0F - angle;
		state.open = 1.0F - angle * angle * angle;
	}

	@Override
	public void submit(ChestRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		stack.pushPose();

		RenderUtil.rotateByHorizontalDirection(stack, state.facing);
		collector.submitModel(this.model, state.open, stack, this.model.renderType(this.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

		stack.popPose();
	}

	public static class ChestRenderState extends BlockEntityRenderState {
		public Direction facing = Direction.SOUTH;
		public float open;
	}

	public static class NaturalistChestModel extends Model<Float> {
		private final ModelPart lid;
		private final ModelPart lock;

		public NaturalistChestModel(ModelPart root) {
			super(root, RenderTypes::entityCutout);
			this.lid = root.getChild(LID);
			this.lock = root.getChild(LOCK);
		}

		@Override
		public void setupAnim(Float open) {
			super.setupAnim(open);
			float rotation = -(open * Mth.HALF_PI);
			this.lid.xRot = rotation;
			this.lock.xRot = rotation;
		}
	}
}
