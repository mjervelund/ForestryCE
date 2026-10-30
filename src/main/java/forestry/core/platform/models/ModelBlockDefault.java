package forestry.core.platform.models;

import com.google.common.base.Preconditions;
import forestry.core.platform.models.baker.ModelBaker;
import forestry.core.platform.models.baker.ModelBakerModel;
import forestry.core.platform.util.ResourceUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A block model baked at render time from the block (and its block entity's model data), and the matching item
 * model.
 * <p>
 * 26.x: a {@link DynamicBlockStateModel} (was a BakedModel with getQuads/ItemOverrides). The world model is chosen in
 * {@link #collectParts} from {@link BlockAndTintGetter#getModelData}; the item model is {@link #itemModel()}.
 * Both are installed by {@link ClientManager#onBakeModels}.
 */
public abstract class ModelBlockDefault<B extends Block, K> implements DynamicBlockStateModel {
	private static final int ITEM_TINT_INDICES = ForestryTints.TINT_INDICES;

	protected final Class<B> blockClass;
	private final ItemModel itemModel = new BlockItemModel();

	protected ModelBlockDefault(Class<B> blockClass) {
		this.blockClass = blockClass;
	}

	protected ModelBakerModel bakeModel(BlockState state, K key, B block, ModelData extraData) {
		ModelBaker baker = new ModelBaker();
		bakeBlock(block, extraData, key, baker, false);
		onCreateModel(baker);
		return baker.bake();
	}

	protected ModelBakerModel getModel(BlockState state, ModelData extraData) {
		Preconditions.checkArgument(this.blockClass.isInstance(state.getBlock()));

		K worldKey = getWorldKey(state, extraData);
		B block = this.blockClass.cast(state.getBlock());
		return bakeModel(state, worldKey, block, extraData);
	}

	protected ModelBakerModel bakeModel(ItemStack stack, K key) {
		ModelBaker baker = new ModelBaker();
		Block block = Block.byItem(stack.getItem());
		Preconditions.checkArgument(this.blockClass.isInstance(block));
		bakeBlock(this.blockClass.cast(block), ModelData.EMPTY, key, baker, true);
		return baker.bake();
	}

	protected ModelBakerModel getModel(ItemStack stack) {
		return bakeModel(stack, getInventoryKey(stack));
	}

	protected void onCreateModel(ModelBaker baker) {
		baker.setAmbientOcclusion(true);
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		if (this.blockClass.isInstance(state.getBlock())) {
			parts.add(getModel(state, level.getModelData(pos)));
		}
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		if (!this.blockClass.isInstance(state.getBlock())) {
			return particleMaterial();
		}
		return getModel(state, level.getModelData(pos)).particleMaterial();
	}

	/**
	 * Used as the last resort for callers that have no level.
	 */
	@Override
	public Material.Baked particleMaterial() {
		return new Material.Baked(ResourceUtil.getMissingTexture(), false);
	}

	@Override
	public int materialFlags() {
		return 0;
	}

	@Override
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return this.blockClass.isInstance(state.getBlock()) ? getModel(state, level.getModelData(pos)).materialFlags() : 0;
	}

	public ItemModel itemModel() {
		return this.itemModel;
	}

	protected abstract K getInventoryKey(ItemStack stack);

	protected abstract K getWorldKey(BlockState state, ModelData extraData);

	protected abstract void bakeBlock(B block, ModelData extraData, K key, ModelBaker baker, boolean inventory);

	// 26.x item model: one layer with the baked quads, the block display transforms and Forestry's item tints
	private class BlockItemModel implements ItemModel {
		@Override
		public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			ModelBakerModel model = getModel(item);
			List<net.minecraft.client.resources.model.geometry.BakedQuad> quads = model.getAllQuads();
			output.appendModelIdentityElement(model);
			ItemStackRenderState.LayerRenderState layer = output.newLayer();
			for (int i = 0; i < ITEM_TINT_INDICES; i++) {
				int tint = new ForestryTints.ItemTint(i).calculate(item, level, owner == null ? null : owner.asLivingEntity());
				layer.tintLayers().add(tint);
				output.appendModelIdentityElement(tint);
			}
			layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
			layer.setUsesBlockLight(true);
			layer.setParticleMaterial(model.particleMaterial());
			layer.setItemTransform(ModelTransforms.BLOCK.getTransform(displayContext));
			layer.setQuads(ItemQuads.split(quads));
		}
	}
}
