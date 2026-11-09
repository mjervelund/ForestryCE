package forestry.core.content.backpacks;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class BackpackFilter implements Predicate<ItemStack> {
	private final TagKey<Item> acceptKey;
	private final TagKey<Item> rejectKey;

	public BackpackFilter(TagKey<Item> acceptKey, TagKey<Item> rejectKey) {
		this.acceptKey = acceptKey;
		this.rejectKey = rejectKey;
	}

	@Override
	public boolean test(ItemStack itemStack) {
		// The backpack denies anything except what is allowed,
		// but from what is allowed you can say what will be rejected (like an override)
		// This allows broad wildcard "accept" types where you can still reject certain ones.
		// ItemStack.is(TagKey) treats a tag that is not bound (failed to load, e.g. one bad entry) as empty
		// instead of throwing on every container click (GitHub #454).
		return !itemStack.isEmpty() && itemStack.is(this.acceptKey) && !itemStack.is(this.rejectKey);
	}
}
