package forestry.core.platform.models;

import forestry.core.platform.registration.FeatureBlock;
import forestry.core.platform.registration.FeatureGroup;
import forestry.core.platform.registration.FeatureItem;
import forestry.core.platform.registration.FeatureTable;
import forestry.core.platform.util.ModUtil;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Installs Forestry's code-built models into the model baking result.
 * <p>
 * 26.x: block state models and item models (keyed by item id) replace the BakedModels that 1.21.1 put into the
 * model registry by ModelResourceLocation. Block and item colours are {@link ForestryTints}.
 */
public enum ClientManager {
	INSTANCE;

	/* CUSTOM MODELS*/
	// Keyed, not appended: models are registered once per resource reload, so registering the same block again
	// must replace its entry instead of growing these
	private final Map<Block, BlockModelEntry> customBlockModels = new LinkedHashMap<>();
	private final Map<Item, ItemModel> customItemModels = new LinkedHashMap<>();

	public void registerModel(ModelBlockDefault<?, ?> model, Object feature) {
		if (feature instanceof FeatureGroup<?, ?, ?> group) {
			group.getFeatures().forEach(f -> registerModel(model, f));
		} else if (feature instanceof FeatureTable<?, ?, ?, ?> group) {
			group.getFeatures().forEach(f -> registerModel(model, f));
		} else if (feature instanceof FeatureBlock<?, ?> block) {
			registerModel(model, block.block(), block.item());
		} else if (feature instanceof FeatureItem<?> item) {
			registerItemModel(model.itemModel(), item.item());
		}
	}

	public void registerModel(ModelBlockDefault<?, ?> model, Block block, @Nullable BlockItem item) {
		registerModel(model, block, item, block.getStateDefinition().getPossibleStates());
	}

	public void registerModel(ModelBlockDefault<?, ?> model, Block block, @Nullable BlockItem item, Collection<BlockState> states) {
		this.customBlockModels.put(block, new BlockModelEntry(model, states));
		if (item != null) {
			registerItemModel(model.itemModel(), item);
		}
	}

	public void registerBlockModel(BlockStateModel model, Block block) {
		this.customBlockModels.put(block, new BlockModelEntry(model, block.getStateDefinition().getPossibleStates()));
	}

	public void registerItemModel(ItemModel model, Item item) {
		this.customItemModels.put(item, model);
	}

	public void onBakeModels(ModelEvent.ModifyBakingResult event) {
		Map<BlockState, BlockStateModel> blockStateModels = event.getBakingResult().blockStateModels();
		for (BlockModelEntry entry : this.customBlockModels.values()) {
			for (BlockState state : entry.states) {
				blockStateModels.put(state, entry.model);
			}
		}

		Map<Identifier, ItemModel> itemModels = event.getBakingResult().itemStackModels();
		this.customItemModels.forEach((item, model) -> {
			Identifier registryName = ModUtil.getRegistryName(item);
			if (registryName != null) {
				itemModels.put(registryName, model);
			}
		});
	}

	private record BlockModelEntry(BlockStateModel model, Collection<BlockState> states) {
	}
}
