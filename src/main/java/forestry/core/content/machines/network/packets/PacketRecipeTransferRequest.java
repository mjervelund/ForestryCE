package forestry.core.content.machines.network.packets;

import forestry.core.platform.network.PacketIdServer;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import forestry.core.content.machines.tiles.TileCarpenter;
import forestry.core.content.machines.tiles.TileFabricator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketRecipeTransferRequest(BlockPos pos,
										  NonNullList<ItemStack> craftingInventory) implements CustomPacketPayload {
	public static void handle(PacketRecipeTransferRequest msg, ServerPlayer player) {
		BlockPos pos = msg.pos();
		NonNullList<ItemStack> craftingInventory = msg.craftingInventory();

		BlockEntity tile = TileUtil.getTile(player.level(), pos);
		if (tile instanceof TileCarpenter carpenter) {
			NonNullList<ItemStack> ghostItems = setGhostItems(carpenter.getCraftingInventory(), craftingInventory);

			NetworkUtil.sendNetworkPacket(new PacketRecipeTransferUpdate(carpenter.getBlockPos(), ghostItems), pos, player.level());
		} else if (tile instanceof TileFabricator fabricator) {
			NonNullList<ItemStack> ghostItems = setGhostItems(fabricator.getCraftingInventory(), craftingInventory);

			NetworkUtil.sendNetworkPacket(new PacketRecipeTransferUpdate(fabricator.getBlockPos(), ghostItems), pos, player.level());
		}
	}

	/**
	 * The stacks come from the client: more of them than the grid has slots used to throw, so ignore the extra
	 * ones (and relay what was stored, not what was sent, to the clients nearby). Ghost slots only show an item, so
	 * store one of each.
	 */
	private static NonNullList<ItemStack> setGhostItems(Container grid, NonNullList<ItemStack> stacks) {
		int size = Math.min(grid.getContainerSize(), stacks.size());
		NonNullList<ItemStack> stored = NonNullList.withSize(size, ItemStack.EMPTY);
		for (int index = 0; index < size; index++) {
			ItemStack stack = stacks.get(index).copyWithCount(1);
			grid.setItem(index, stack);
			stored.set(index, stack);
		}
		return stored;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PacketIdServer.RECIPE_TRANSFER_REQUEST;
	}

	public static void encode(RegistryFriendlyByteBuf buffer, PacketRecipeTransferRequest msg) {
		buffer.writeBlockPos(msg.pos);
		NetworkUtil.writeItemStacks(buffer, msg.craftingInventory);
	}

	public static PacketRecipeTransferRequest decode(RegistryFriendlyByteBuf buffer) {
		return new PacketRecipeTransferRequest(buffer.readBlockPos(), NetworkUtil.readItemStacks(buffer));
	}
}
