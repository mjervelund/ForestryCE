package forestry.arboriculture.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import forestry.arboriculture.wood.ForestryWoodType;
import forestry.arboriculture.entities.IForestryBoat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.joml.Quaternionf;

import java.util.EnumMap;

import static forestry.api.ForestryConstants.forestry;

/**
 * 26.x boat renderers are one model and texture per renderer (each vanilla wood is its own entity type);
 * Forestry keeps one boat entity whose wood is synced data, so this renderer carries the wood in its render
 * state and replicates {@link AbstractBoatRenderer}'s submit with a per-wood model and texture.
 */
public class ForestryBoatRenderer extends AbstractBoatRenderer {
	private final EnumMap<ForestryWoodType, Pair<Identifier, BoatModel>> boatResources;
	private final Model.Simple waterPatchModel;

	public ForestryBoatRenderer(EntityRendererProvider.Context ctx, boolean hasChest) {
		super(ctx, forestry(getTextureLocation(ForestryWoodType.VALUES[0], hasChest)));

		this.waterPatchModel = new Model.Simple(ctx.bakeLayer(ModelLayers.BOAT_WATER_PATCH), t -> RenderTypes.waterMask());
		this.boatResources = new EnumMap<>(ForestryWoodType.class);
		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			var pair = new Pair<>(forestry(getTextureLocation(type, hasChest)), createBoatModel(ctx, type, hasChest));
			this.boatResources.put(type, pair);
		}
	}

	private static String getTextureLocation(ForestryWoodType type, boolean hasChest) {
		return (hasChest ? "textures/entity/chest_boat/" : "textures/entity/boat/") + type.getSerializedName() + ".png";
	}

	private static BoatModel createBoatModel(EntityRendererProvider.Context ctx, ForestryWoodType type, boolean hasChest) {
		ModelLayerLocation modelLoc = createBoatModelLocation(type, hasChest);
		ModelPart root = ctx.bakeLayer(modelLoc);

		return new BoatModel(root);
	}

	public static ModelLayerLocation createBoatModelLocation(ForestryWoodType type, boolean hasChest) {
		return new ModelLayerLocation(forestry((hasChest ? "chest_boat/" : "boat/") + type.getSerializedName()), "main");
	}

	@Override
	protected EntityModel<BoatRenderState> model() {
		return this.boatResources.get(ForestryWoodType.VALUES[0]).getSecond();
	}

	@Override
	public BoatRenderState createRenderState() {
		return new ForestryBoatRenderState();
	}

	@Override
	public void extractRenderState(AbstractBoat entity, BoatRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		if (entity instanceof IForestryBoat boat && state instanceof ForestryBoatRenderState forestryState) {
			forestryState.wood = boat.getWoodType();
		}
	}

	@Override
	public void submit(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		ForestryWoodType wood = state instanceof ForestryBoatRenderState forestryState ? forestryState.wood : ForestryWoodType.VALUES[0];
		Pair<Identifier, BoatModel> resources = this.boatResources.get(wood);

		// vanilla AbstractBoatRenderer.submit, with the wood's model and texture
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.375F, 0.0F);
		poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
		float hurt = state.hurtTime;
		if (hurt > 0.0F) {
			poseStack.rotateDegrees(Axis.XP, Mth.sin(hurt) * hurt * state.damageTime / 10.0F * state.hurtDir);
		}

		if (!state.isUnderWater && !Mth.equal(state.bubbleAngle, 0.0F)) {
			poseStack.rotate(new Quaternionf().setAngleAxis(state.bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.rotateDegrees(Axis.YP, 90.0F);
		collector.submitModel(resources.getSecond(), state, poseStack, resources.getFirst(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		if (!state.isUnderWater) {
			collector.submitModel(this.waterPatchModel, Unit.INSTANCE, poseStack, resources.getFirst(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		}
		poseStack.popPose();

		// EntityRenderer.submit (we skip AbstractBoatRenderer's own model submit)
		if (state.leashStates != null) {
			for (EntityRenderState.LeashState leashState : state.leashStates) {
				collector.submitLeash(poseStack, leashState);
			}
		}
		this.submitNameDisplay(state, poseStack, collector, camera);
	}

	public static class ForestryBoatRenderState extends BoatRenderState {
		public ForestryWoodType wood = ForestryWoodType.VALUES[0];
	}
}
