package forestry.gametest;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import forestry.api.core.ILocationProvider;
import forestry.core.content.machines.blocks.BlockTypeFactoryPlain;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.network.packets.PacketRecipeTransferRequest;
import forestry.core.content.machines.tiles.TileCarpenter;
import forestry.core.content.sorting.FilterLogic;
import forestry.core.content.worktable.recipes.MemorizedRecipe;

/**
 * Security guard: serverbound packets carried sizes and indices the server used unchecked. More recipe transfer stacks
 * than the ghost grid has slots threw (and were relayed to every client nearby), a genome filter index past the
 * three per side threw, and a worktable recipe could name a non-crafting recipe (ClassCastException) or an out of
 * range selection (IndexOutOfBoundsException) that the worktable saved and threw on again for every player.
 */
public class PacketInputBoundsTest {
	private static final BlockPos CARPENTER = new BlockPos(1, 1, 1);

	@ForestryGameTest
	public static void recipeTransferIgnoresExtraStacks(GameTestHelper helper) {
		helper.setBlock(CARPENTER, FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.CARPENTER).block());
		TileCarpenter carpenter = helper.getBlockEntity(CARPENTER, TileCarpenter.class);
		BlockPos pos = helper.absolutePos(CARPENTER);
		ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
		player.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 2.5D);

		NonNullList<ItemStack> stacks = NonNullList.create();
		for (int i = 0; i < 40; i++) {
			stacks.add(new ItemStack(Items.STICK, 64));
		}
		PacketRecipeTransferRequest.handle(new PacketRecipeTransferRequest(pos, stacks), player);

		var grid = carpenter.getCraftingInventory();
		for (int i = 0; i < grid.getContainerSize(); i++) {
			helper.assertTrue(grid.getItem(i).is(Items.STICK) && grid.getItem(i).getCount() == 1, "ghost slot " + i + " holds " + grid.getItem(i));
		}
		helper.succeed();
	}

	@ForestryGameTest
	public static void genomeFilterIndexOutOfRange(GameTestHelper helper) {
		FilterLogic logic = new FilterLogic(new ILocationProvider() {
			@Override
			public BlockPos getBlockPos() {
				return helper.absolutePos(BlockPos.ZERO);
			}

			@Override
			public Level getLevel() {
				return helper.getLevel();
			}
		}, (filterLogic, level, player) -> {});
		helper.assertFalse(logic.setGenomeFilter(Direction.UP, 3, true, null), "index 3 was accepted");
		helper.assertFalse(logic.setGenomeFilter(Direction.UP, -1, true, null), "index -1 was accepted");
		helper.succeed();
	}

	@ForestryGameTest
	public static void worktableRecipeFromClientIsChecked(GameTestHelper helper) {
		// a smelting recipe id: getSelectedRecipe used to hand it out as a crafting recipe
		MemorizedRecipe smelting = decode(helper, 0, Identifier.withDefaultNamespace("iron_ingot_from_smelting_raw_iron"));
		helper.assertTrue(smelting.getSelectedRecipe() == null, "a smelting recipe was accepted as a worktable recipe");

		for (int selected : new int[]{1, 5, -1}) {
			MemorizedRecipe stick = decode(helper, selected, Identifier.withDefaultNamespace("stick"));
			helper.assertTrue(stick.getSelectedRecipe() != null, "selection " + selected + " lost the stick recipe");
		}
		helper.succeed();
	}

	private static MemorizedRecipe decode(GameTestHelper helper, int selected, Identifier recipe) {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		// craft matrix bigger than the worktable's 3x3
		buf.writeVarInt(12);
		for (int i = 0; i < 12; i++) {
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, new ItemStack(Items.OAK_PLANKS));
		}
		buf.writeBoolean(false);
		buf.writeVarInt(selected);
		buf.writeVarInt(1);
		buf.writeIdentifier(recipe);
		return new MemorizedRecipe(buf);
	}
}
