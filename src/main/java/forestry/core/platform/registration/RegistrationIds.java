package forestry.core.platform.registration;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 26.x items and blocks need their registry id in their properties ({@code Properties#setId}) before
 * construction. Forestry builds properties inside its registration suppliers, so {@link FeatureItem} and
 * {@link FeatureBlock} publish the id being registered while the supplier runs, and {@link #item()} /
 * {@link #block()} create properties carrying it.
 */
public final class RegistrationIds {
	private static final ThreadLocal<ResourceKey<Item>> ITEM = new ThreadLocal<>();
	private static final ThreadLocal<ResourceKey<Block>> BLOCK = new ThreadLocal<>();

	private RegistrationIds() {
	}

	public static <T> T withItemId(Identifier id, Supplier<T> constructor) {
		return with(ITEM, ResourceKey.create(Registries.ITEM, id), constructor);
	}

	public static <T> T withBlockId(Identifier id, Supplier<T> constructor) {
		return with(BLOCK, ResourceKey.create(Registries.BLOCK, id), constructor);
	}

	private static <K, T> T with(ThreadLocal<K> current, K key, Supplier<T> constructor) {
		K previous = current.get();
		current.set(key);
		try {
			return constructor.get();
		} finally {
			current.set(previous);
		}
	}

	public static Item.Properties item() {
		return new Item.Properties().setId(itemKey());
	}

	public static BlockBehaviour.Properties block() {
		return BlockBehaviour.Properties.of().setId(blockKey());
	}

	public static BlockBehaviour.Properties block(BlockBehaviour.Properties copied) {
		return copied.setId(blockKey());
	}

	private static ResourceKey<Item> itemKey() {
		return Objects.requireNonNull(ITEM.get(), "Item properties created outside a Forestry item registration");
	}

	private static ResourceKey<Block> blockKey() {
		return Objects.requireNonNull(BLOCK.get(), "Block properties created outside a Forestry block registration");
	}
}
