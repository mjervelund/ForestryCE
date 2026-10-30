package forestry.core.platform.gui.slots;

import forestry.api.client.ForestrySprites;
import forestry.api.core.IFilterSlotDelegate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Slot which only takes specific items, specified by the IFilterSlotDelegate.
 */
public class SlotFiltered extends SlotWatched {
	private final IFilterSlotDelegate filterSlotDelegate;
	// 26.x: slot icons are sprite ids in the gui sprite atlas, where the forestry icons are now
	private Identifier blockedTexture = ForestrySprites.SLOT_BLOCKED;

	public <T extends Container & IFilterSlotDelegate> SlotFiltered(T inventory, int slotIndex, int xPos, int yPos) {
		super(inventory, slotIndex, xPos, yPos);
		this.filterSlotDelegate = inventory;
	}

	@Override
	public boolean mayPlace(ItemStack itemstack) {
		int slotIndex = getSlotIndex();
		return !this.filterSlotDelegate.isLocked(slotIndex) &&
			(itemstack.isEmpty() || this.filterSlotDelegate.canSlotAccept(slotIndex, itemstack));
	}

	public SlotFiltered setBlockedSprite(Identifier sprite) {
		this.blockedTexture = sprite;
		return this;
	}

	public SlotFiltered setBackgroundSprite(Identifier sprite) {
		setBackground(sprite);
		return this;
	}

	@Nullable
	@Override
	public Identifier getNoItemIcon() {
		return !mayPlace(getItem()) ? this.blockedTexture : super.getNoItemIcon();
	}
}
