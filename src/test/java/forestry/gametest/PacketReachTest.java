package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import forestry.core.content.machines.blocks.BlockTypeFactoryPlain;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.network.packets.PacketRecipeTransferRequest;
import forestry.core.content.machines.tiles.TileCarpenter;

/**
 * Security guard: block-targeted serverbound packets (recipe transfer, genetic filter rules, worktable recipe) used
 * the client's position as is, so a modified client could rewrite any machine in the dimension and load or generate
 * chunks. They now require the position to be loaded and within the player's interaction range.
 */
public class PacketReachTest {
	private static final BlockPos CARPENTER = new BlockPos(1, 1, 1);

	@ForestryGameTest
	public static void recipeTransferNeedsReach(GameTestHelper helper) {
		helper.setBlock(CARPENTER, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.CARPENTER).block());
		if (!(helper.getBlockEntity(CARPENTER, TileCarpenter.class) instanceof TileCarpenter carpenter)) {
			helper.fail("Expected a TileCarpenter at " + CARPENTER);
			return;
		}
		BlockPos pos = helper.absolutePos(CARPENTER);
		ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);

		player.setPos(pos.getX() + 0.5D, pos.getY() + 40.0D, pos.getZ() + 0.5D);
		PacketRecipeTransferRequest.handle(new PacketRecipeTransferRequest(pos, NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.STICK))), player);
		helper.assertTrue(carpenter.getCraftingInventory().getItem(0).isEmpty(), "a recipe transfer from 40 blocks away changed the carpenter");

		player.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 2.5D);
		PacketRecipeTransferRequest.handle(new PacketRecipeTransferRequest(pos, NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.STICK))), player);
		helper.assertTrue(carpenter.getCraftingInventory().getItem(0).is(Items.STICK), "a recipe transfer next to the carpenter was ignored");
		helper.succeed();
	}
}
