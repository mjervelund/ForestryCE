package forestry.core.platform.legacy;

import java.util.function.Supplier;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Item handler for containers that delegate to a shared inventory (multiblock hatches). NeoForge's container
 * wrappers cache the container size when they are created and assume the container owns its slots, but a
 * multiblock part reports size 0 while unformed and the controller's size once formed. A capability cache
 * (Mekanism transporters keep one) can hold a wrapper built in the other state and then index past its size
 * (IndexOutOfBoundsException while ticking). This handler builds a fresh wrapper for every call, which resizes
 * it to the container's current state.
 */
final class LiveContainerHandler implements ResourceHandler<ItemResource> {
	private final Supplier<ResourceHandler<ItemResource>> factory;

	LiveContainerHandler(Supplier<ResourceHandler<ItemResource>> factory) {
		this.factory = factory;
	}

	private ResourceHandler<ItemResource> current() {
		return this.factory.get();
	}

	@Override
	public int size() {
		return current().size();
	}

	@Override
	public ItemResource getResource(int index) {
		ResourceHandler<ItemResource> h = current();
		return index < h.size() ? h.getResource(index) : ItemResource.EMPTY;
	}

	@Override
	public long getAmountAsLong(int index) {
		ResourceHandler<ItemResource> h = current();
		return index < h.size() ? h.getAmountAsLong(index) : 0;
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		ResourceHandler<ItemResource> h = current();
		return index < h.size() ? h.getCapacityAsLong(index, resource) : 0;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		ResourceHandler<ItemResource> h = current();
		return index < h.size() && h.isValid(index, resource);
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		ResourceHandler<ItemResource> h = current();
		return index < h.size() ? h.insert(index, resource, amount, transaction) : 0;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		ResourceHandler<ItemResource> h = current();
		return index < h.size() ? h.extract(index, resource, amount, transaction) : 0;
	}
}
