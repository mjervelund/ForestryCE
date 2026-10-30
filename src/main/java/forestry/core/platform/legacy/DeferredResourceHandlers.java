package forestry.core.platform.legacy;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import forestry.core.platform.legacy.energy.IEnergyStorage;
import forestry.core.platform.legacy.fluids.capability.IFluidHandler;
import forestry.core.platform.legacy.items.IItemHandler;

/**
 * Exposes legacy (pre-transfer-API) handlers through the 26.x transactional handler interfaces.
 * <p>
 * Legacy handlers cannot roll back, so these adapters only simulate while a transaction is open and
 * record what was accepted; the recorded operations are executed when the root transaction commits
 * ({@link SnapshotJournal#onRootCommit}). An aborted transaction, which is how the new API simulates,
 * therefore has no side effects. Repeated operations on the same slot (items) or fluid within one
 * transaction are simulated cumulatively; mixing insertion and extraction of the same slot or fluid
 * within one transaction is refused (returns 0).
 * <p>
 * Container-backed item handlers do not need this: {@link LegacyCapabilities#items} maps them to
 * NeoForge's own transactional container wrappers.
 */
public final class DeferredResourceHandlers {
	private static final Logger LOGGER = LogUtils.getLogger();

	private DeferredResourceHandlers() {
	}

	private record Pending<R>(R resource, int inserted, int extracted) {
	}

	private static int accepted(int total, int prior, int amount) {
		return Math.clamp(total - prior, 0, amount);
	}

	public static final class Items extends SnapshotJournal<Map<Integer, Pending<ItemResource>>> implements ResourceHandler<ItemResource> {
		private final IItemHandler handler;
		private Map<Integer, Pending<ItemResource>> pending = new HashMap<>();

		public Items(IItemHandler handler) {
			this.handler = handler;
		}

		public IItemHandler handler() {
			return this.handler;
		}

		@Override
		public int size() {
			return this.handler.getSlots();
		}

		@Override
		public ItemResource getResource(int index) {
			return ItemResource.of(this.handler.getStackInSlot(index));
		}

		@Override
		public long getAmountAsLong(int index) {
			return this.handler.getStackInSlot(index).getCount();
		}

		@Override
		public long getCapacityAsLong(int index, ItemResource resource) {
			int limit = this.handler.getSlotLimit(index);
			return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
		}

		@Override
		public boolean isValid(int index, ItemResource resource) {
			return !resource.isEmpty() && this.handler.isItemValid(index, resource.toStack(1));
		}

		@Override
		public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
			TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
			Pending<ItemResource> p = this.pending.get(index);
			if (p != null && (p.extracted() > 0 || !p.resource().equals(resource))) {
				return 0;
			}
			int prior = p == null ? 0 : p.inserted();
			ItemStack remainder = this.handler.insertItem(index, resource.toStack(prior + amount), true);
			int added = accepted(prior + amount - remainder.getCount(), prior, amount);
			if (added > 0) {
				updateSnapshots(transaction);
				this.pending.put(index, new Pending<>(resource, prior + added, 0));
			}
			return added;
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
			Pending<ItemResource> p = this.pending.get(index);
			if (p != null && (p.inserted() > 0 || !p.resource().equals(resource))) {
				return 0;
			}
			if (!resource.matches(this.handler.getStackInSlot(index))) {
				return 0;
			}
			int prior = p == null ? 0 : p.extracted();
			ItemStack extracted = this.handler.extractItem(index, prior + amount, true);
			int added = resource.matches(extracted) ? accepted(extracted.getCount(), prior, amount) : 0;
			if (added > 0) {
				updateSnapshots(transaction);
				this.pending.put(index, new Pending<>(resource, 0, prior + added));
			}
			return added;
		}

		@Override
		protected Map<Integer, Pending<ItemResource>> createSnapshot() {
			return new HashMap<>(this.pending);
		}

		@Override
		protected void revertToSnapshot(Map<Integer, Pending<ItemResource>> snapshot) {
			this.pending = snapshot;
		}

		@Override
		protected void onRootCommit(Map<Integer, Pending<ItemResource>> originalState) {
			Map<Integer, Pending<ItemResource>> ops = this.pending;
			this.pending = new HashMap<>();
			ops.forEach((index, op) -> {
				if (op.inserted() > 0) {
					ItemStack remainder = this.handler.insertItem(index, op.resource().toStack(op.inserted()), false);
					if (!remainder.isEmpty()) {
						LOGGER.warn("Legacy item handler {} accepted fewer items on commit than simulated: {} lost", this.handler, remainder);
					}
				} else if (op.extracted() > 0) {
					ItemStack extracted = this.handler.extractItem(index, op.extracted(), false);
					if (extracted.getCount() != op.extracted()) {
						LOGGER.warn("Legacy item handler {} extracted {} on commit, simulated {}", this.handler, extracted, op.extracted());
					}
				}
			});
		}
	}

	public static final class Fluids extends SnapshotJournal<Map<FluidResource, Pending<FluidResource>>> implements ResourceHandler<FluidResource> {
		private final IFluidHandler handler;
		private Map<FluidResource, Pending<FluidResource>> pending = new HashMap<>();

		public Fluids(IFluidHandler handler) {
			this.handler = handler;
		}

		public IFluidHandler handler() {
			return this.handler;
		}

		@Override
		public int size() {
			return this.handler.getTanks();
		}

		@Override
		public FluidResource getResource(int index) {
			return FluidResource.of(this.handler.getFluidInTank(index));
		}

		@Override
		public long getAmountAsLong(int index) {
			return this.handler.getFluidInTank(index).getAmount();
		}

		@Override
		public long getCapacityAsLong(int index, FluidResource resource) {
			return this.handler.getTankCapacity(index);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			return !resource.isEmpty() && this.handler.isFluidValid(index, resource.toStack(1));
		}

		// Legacy fill/drain act on the whole handler, so the index only matters for the
		// default multi-index loops, which the cumulative bookkeeping keeps correct.
		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
			Pending<FluidResource> p = this.pending.get(resource);
			if (p != null && p.extracted() > 0) {
				return 0;
			}
			int prior = p == null ? 0 : p.inserted();
			int filled = this.handler.fill(resource.toStack(prior + amount), IFluidHandler.FluidAction.SIMULATE);
			int added = accepted(filled, prior, amount);
			if (added > 0) {
				updateSnapshots(transaction);
				this.pending.put(resource, new Pending<>(resource, prior + added, 0));
			}
			return added;
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
			Pending<FluidResource> p = this.pending.get(resource);
			if (p != null && p.inserted() > 0) {
				return 0;
			}
			int prior = p == null ? 0 : p.extracted();
			FluidStack drained = this.handler.drain(resource.toStack(prior + amount), IFluidHandler.FluidAction.SIMULATE);
			int added = resource.matches(drained) ? accepted(drained.getAmount(), prior, amount) : 0;
			if (added > 0) {
				updateSnapshots(transaction);
				this.pending.put(resource, new Pending<>(resource, 0, prior + added));
			}
			return added;
		}

		@Override
		protected Map<FluidResource, Pending<FluidResource>> createSnapshot() {
			return new HashMap<>(this.pending);
		}

		@Override
		protected void revertToSnapshot(Map<FluidResource, Pending<FluidResource>> snapshot) {
			this.pending = snapshot;
		}

		@Override
		protected void onRootCommit(Map<FluidResource, Pending<FluidResource>> originalState) {
			Map<FluidResource, Pending<FluidResource>> ops = this.pending;
			this.pending = new HashMap<>();
			ops.forEach((resource, op) -> {
				if (op.inserted() > 0) {
					int filled = this.handler.fill(resource.toStack(op.inserted()), IFluidHandler.FluidAction.EXECUTE);
					if (filled != op.inserted()) {
						LOGGER.warn("Legacy fluid handler {} filled {} mB on commit, simulated {}", this.handler, filled, op.inserted());
					}
				} else if (op.extracted() > 0) {
					FluidStack drained = this.handler.drain(resource.toStack(op.extracted()), IFluidHandler.FluidAction.EXECUTE);
					if (drained.getAmount() != op.extracted()) {
						LOGGER.warn("Legacy fluid handler {} drained {} mB on commit, simulated {}", this.handler, drained.getAmount(), op.extracted());
					}
				}
			});
		}
	}

	public static final class Energy extends SnapshotJournal<Pending<Void>> implements EnergyHandler {
		private static final Pending<Void> NONE = new Pending<>(null, 0, 0);
		private final IEnergyStorage storage;
		private Pending<Void> pending = NONE;

		public Energy(IEnergyStorage storage) {
			this.storage = storage;
		}

		public IEnergyStorage storage() {
			return this.storage;
		}

		@Override
		public long getAmountAsLong() {
			return this.storage.getEnergyStored();
		}

		@Override
		public long getCapacityAsLong() {
			return this.storage.getMaxEnergyStored();
		}

		@Override
		public int insert(int amount, TransactionContext transaction) {
			TransferPreconditions.checkNonNegative(amount);
			if (this.pending.extracted() > 0) {
				return 0;
			}
			int prior = this.pending.inserted();
			int added = accepted(this.storage.receiveEnergy(prior + amount, true), prior, amount);
			if (added > 0) {
				updateSnapshots(transaction);
				this.pending = new Pending<>(null, prior + added, 0);
			}
			return added;
		}

		@Override
		public int extract(int amount, TransactionContext transaction) {
			TransferPreconditions.checkNonNegative(amount);
			if (this.pending.inserted() > 0) {
				return 0;
			}
			int prior = this.pending.extracted();
			int added = accepted(this.storage.extractEnergy(prior + amount, true), prior, amount);
			if (added > 0) {
				updateSnapshots(transaction);
				this.pending = new Pending<>(null, 0, prior + added);
			}
			return added;
		}

		@Override
		protected Pending<Void> createSnapshot() {
			return this.pending;
		}

		@Override
		protected void revertToSnapshot(Pending<Void> snapshot) {
			this.pending = snapshot;
		}

		@Override
		protected void onRootCommit(Pending<Void> originalState) {
			Pending<Void> op = this.pending;
			this.pending = NONE;
			if (op.inserted() > 0) {
				this.storage.receiveEnergy(op.inserted(), false);
			} else if (op.extracted() > 0) {
				this.storage.extractEnergy(op.extracted(), false);
			}
		}
	}
}
