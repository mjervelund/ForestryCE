package forestry.core.platform.models;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An item model that shows a different model per stack, like a bee or sapling per species.
 * <p>
 * 26.x replacement for the BakedModels with ItemOverrides that 1.21.1 used: every model the stacks can pick is
 * baked with the item models, and {@link #update} delegates to the one the stack selects. The model ids come from
 * the client managers, so a {@link Kind} is registered as a unit item model type and named by the item's client
 * item definition (assets/forestry/items/*.json).
 */
public final class SpeciesItemModel implements ItemModel {
	private final Function<ItemStack, Identifier> selector;
	private final Map<Identifier, ItemModel> models;
	@Nullable
	private final ItemModel fallback;

	private SpeciesItemModel(Function<ItemStack, Identifier> selector, Map<Identifier, ItemModel> models, @Nullable ItemModel fallback) {
		this.selector = selector;
		this.models = models;
		this.fallback = fallback;
	}

	@Override
	public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		ItemModel model = this.models.getOrDefault(this.selector.apply(item), this.fallback);
		if (model != null) {
			model.update(output, item, resolver, displayContext, level, owner, seed);
		}
	}

	/**
	 * One kind of species item model.
	 *
	 * @param modelIds every model a stack can select, resolved when the models load
	 * @param selector the model id for a stack
	 * @param fallback the model for stacks whose model is missing
	 */
	public record Kind(Supplier<Collection<Identifier>> modelIds, Function<ItemStack, Identifier> selector, Supplier<Identifier> fallback) {
		public MapCodec<Unbaked> codec() {
			Unbaked[] unbaked = new Unbaked[1];
			MapCodec<Unbaked> codec = MapCodec.unit(() -> unbaked[0]);
			unbaked[0] = new Unbaked(this, codec);
			return codec;
		}
	}

	public record Unbaked(Kind kind, MapCodec<Unbaked> type) implements ItemModel.Unbaked {
		private Set<Identifier> allModels() {
			Set<Identifier> ids = new LinkedHashSet<>(this.kind.modelIds().get());
			ids.add(this.kind.fallback().get());
			return ids;
		}

		private static CuboidItemModelWrapper.Unbaked wrapper(Identifier model) {
			List<ItemTintSource> tints = new ArrayList<>();
			for (int i = 0; i < ForestryTints.TINT_INDICES; i++) {
				tints.add(new ForestryTints.ItemTint(i));
			}
			return new CuboidItemModelWrapper.Unbaked(model, Optional.empty(), tints);
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
			for (Identifier id : allModels()) {
				resolver.markDependency(id);
			}
		}

		@Override
		public ItemModel bake(BakingContext context, Matrix4fc transformation) {
			Map<Identifier, ItemModel> models = new HashMap<>();
			for (Identifier id : allModels()) {
				models.put(id, wrapper(id).bake(context, transformation));
			}
			return new SpeciesItemModel(this.kind.selector(), models, models.get(this.kind.fallback().get()));
		}
	}
}
