package forestry.arboriculture.models;

import com.mojang.datafixers.util.Pair;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.arboriculture.ITreeClientManager;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.arboriculture.sapling.SaplingBlockEntity;
import forestry.core.platform.models.SpeciesItemModel;
import forestry.core.platform.util.ResourceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The genetic sapling: one block and item model per species.
 * <p>
 * 26.x: the block model is a {@link DynamicBlockStateModel} over standalone-baked models (was a geometry loader
 * with a BakedModel), the item model a {@link SpeciesItemModel} kind ({@code forestry:sapling}).
 */
public class SaplingModel implements DynamicBlockStateModel {
	private static final Map<Identifier, StandaloneModelKey<BlockStateModelPart>> BLOCK_MODELS = new HashMap<>();

	public static final SpeciesItemModel.Kind ITEM = new SpeciesItemModel.Kind(
		() -> treeManager().getAllSaplingModels().stream().map(Pair::getSecond).toList(),
		stack -> {
			IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
			ITreeClientManager treeManager = treeManager();
			return individual == null ? treeManager.getDefaultSaplingModels().getSecond() : treeManager.getSaplingModels((ITreeSpecies) individual.getSpecies()).getSecond();
		},
		() -> treeManager().getDefaultSaplingModels().getSecond()
	);

	private static ITreeClientManager treeManager() {
		return IForestryClientApi.INSTANCE.getTreeManager();
	}

	public static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
		Set<Identifier> ids = new LinkedHashSet<>();
		for (Pair<Identifier, Identifier> pair : treeManager().getAllSaplingModels()) {
			ids.add(pair.getFirst());
		}
		ids.add(treeManager().getDefaultSaplingModels().getFirst());
		for (Identifier id : ids) {
			StandaloneModelKey<BlockStateModelPart> key = BLOCK_MODELS.computeIfAbsent(id, i -> new StandaloneModelKey<>(i::toString));
			event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(id));
		}
	}

	@Nullable
	private static BlockStateModelPart blockModelFor(@Nullable ITreeSpecies species) {
		ITreeClientManager treeManager = treeManager();
		Pair<Identifier, Identifier> pair = species != null ? treeManager.getSaplingModels(species) : treeManager.getDefaultSaplingModels();
		StandaloneModelKey<BlockStateModelPart> key = BLOCK_MODELS.get(pair.getFirst());
		if (key == null) {
			key = BLOCK_MODELS.get(treeManager.getDefaultSaplingModels().getFirst());
		}
		return key == null ? null : Minecraft.getInstance().getModelManager().getStandaloneModel(key);
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		BlockStateModelPart model = blockModelFor(level.getModelData(pos).get(SaplingBlockEntity.TREE_SPECIES));
		if (model != null) {
			parts.add(model);
		}
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		BlockStateModelPart model = blockModelFor(level.getModelData(pos).get(SaplingBlockEntity.TREE_SPECIES));
		return model != null ? model.particleMaterial() : particleMaterial();
	}

	@Override
	public Material.Baked particleMaterial() {
		BlockStateModelPart model = blockModelFor(null);
		return model != null ? model.particleMaterial() : new Material.Baked(ResourceUtil.getMissingTexture(), false);
	}

	@Override
	public int materialFlags() {
		return 0;
	}
}
