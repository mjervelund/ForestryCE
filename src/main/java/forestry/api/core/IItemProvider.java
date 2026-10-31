package forestry.api.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public interface IItemProvider<I extends Item> {
	I item();

	default ItemStack stack() {
		return stack(1);
	}

	default ItemStack stack(int amount) {
		return new ItemStack(item(), amount);
	}

	// Unlike stack(), safe before item components are bound (plugin registration, reload listeners)
	default ItemStackTemplate template() {
		return template(1);
	}

	default ItemStackTemplate template(int amount) {
		return new ItemStackTemplate(item(), amount);
	}

	default boolean itemEqual(ItemStack stack) {
		return !stack.isEmpty() && itemEqual(stack.getItem());
	}

	default boolean itemEqual(Item item) {
		return item() == item;
	}
}
