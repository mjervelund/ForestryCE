package forestry.core.platform.item;

import javax.annotation.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import forestry.core.features.CoreDataComponents;

/**
 * 26.x item capability for Forestry's fluid containers, matching {@link FluidHandlerItemForestry}:
 * all or nothing (fills only when empty and with a full bucket, drains everything), the container is
 * consumed when emptied, a filled container shows the filled model (damage 1), and wax capsules
 * refuse hot fluids.
 */
public class FluidContainerResourceHandler extends ItemAccessFluidHandler {
	private final FluidContainerType containerType;

	public FluidContainerResourceHandler(ItemAccess itemAccess, FluidContainerType containerType) {
		super(itemAccess, CoreDataComponents.FLUID_CONTENT.get(), FluidType.BUCKET_VOLUME);
		this.containerType = containerType;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return super.isValid(index, resource) && FluidHandlerItemForestry.contentsAllowed(this.containerType, resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		int full = this.capacity * this.itemAccess.getAmount();
		if (full == 0 || amount < full || getAmountFrom(this.itemAccess.getResource(), index) != 0) {
			return 0;
		}
		return super.insert(index, resource, full, transaction);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		int all = getAmountFrom(this.itemAccess.getResource(), index) * this.itemAccess.getAmount();
		if (all == 0 || amount < all || !isValid(index, resource)) {
			return 0;
		}
		return super.extract(index, resource, all, transaction);
	}

	@Override
	@Nullable
	protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
		if (newAmount == 0) {
			// consumed once emptied
			return null;
		}
		return accessResource.with(this.component, SimpleFluidContent.copyOf(newResource.toStack(newAmount)))
			.with(DataComponents.DAMAGE, 1);
	}
}
