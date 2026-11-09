package forestry.core.platform.item;

import net.minecraft.world.InteractionResult;
import forestry.core.platform.gui.ContainerItemInventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class WithScreenItem extends ItemForestry {
	public WithScreenItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level worldIn, Player player, InteractionHand handIn) {
		ItemStack stack = player.getItemInHand(handIn);

		if (player instanceof ServerPlayer serverPlayer) {
			openGui(serverPlayer, stack, handIn);
		}

		return InteractionResult.SUCCESS;
	}

	protected void openGui(ServerPlayer serverPlayer, ItemStack heldItem, InteractionHand hand) {
		serverPlayer.openMenu(getMenuProvider(heldItem), buffer -> writeContainerData(serverPlayer, heldItem, hand, buffer));
	}

	public SimpleMenuProvider getMenuProvider(ItemStack heldItem) {
		return new SimpleMenuProvider((windowId, playerInv, player) -> getContainer(windowId, player, heldItem), heldItem.getHoverName());
	}

	// The client menu factory reads this to find the held stack. It must be the hand that was right-clicked:
	// getUsedItemHand() is the hand of the last use-duration item (food, shield, smoker), so it pointed at the
	// other hand, and the client either bound the menu to the wrong stack or was disconnected on an empty hand.
	protected void writeContainerData(ServerPlayer player, ItemStack stack, InteractionHand hand, RegistryFriendlyByteBuf buffer) {
		buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack itemstack, Player player) {
		if (!itemstack.isEmpty() && player instanceof ServerPlayer && player.containerMenu instanceof ContainerItemInventory) {
			player.closeContainer();
		}

		return super.onDroppedByPlayer(itemstack, player);
	}

	@Nullable
	public abstract AbstractContainerMenu getContainer(int containerId, Player player, ItemStack heldItem);
}
