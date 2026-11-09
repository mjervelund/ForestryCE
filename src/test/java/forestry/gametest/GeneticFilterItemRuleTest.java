package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import forestry.core.content.sorting.DefaultFilterRuleType;
import forestry.core.content.sorting.features.SortingBlocks;
import forestry.core.content.sorting.tiles.TileGeneticFilter;
import forestry.core.platform.legacy.items.IItemHandler;

/**
 * https://github.com/thedarkcolour/ForestryCE/issues/461: a side set to the "item" rule must take non-genetic items
 * (honeycombs, ...), as it did before the July 2024 API refactor.
 */
public class GeneticFilterItemRuleTest {
	private static final BlockPos FILTER_POS = new BlockPos(8, 2, 8);
	private static final Direction ITEM_SIDE = Direction.SOUTH;
	private static final Direction INPUT_SIDE = Direction.UP;
	private static final BlockPos CHEST_POS = FILTER_POS.relative(ITEM_SIDE);

	@ForestryGameTest
	public static void itemSideAcceptsNonGeneticItems(GameTestHelper helper) {
		helper.setBlock(CHEST_POS, Blocks.CHEST);
		helper.setBlock(FILTER_POS, SortingBlocks.FILTER.defaultState());

		if (!(helper.getLevel().getBlockEntity(helper.absolutePos(FILTER_POS)) instanceof TileGeneticFilter filter)) {
			helper.fail("Expected a TileGeneticFilter at " + FILTER_POS);
			return;
		}
		for (Direction facing : Direction.values()) {
			filter.getLogic().setRule(facing, facing == ITEM_SIDE ? DefaultFilterRuleType.ITEM : DefaultFilterRuleType.CLOSED);
		}

		IItemHandler input = filter.getItemHandler(INPUT_SIDE);
		ItemStack left = input.insertItem(0, new ItemStack(Items.HONEYCOMB, 3), false);
		helper.assertTrue(left.isEmpty(), "the ITEM side refused " + left + " (non-genetic item)");

		helper.startSequence()
			.thenIdle(20)
			.thenExecute(() -> helper.assertValueEqual(countInChest(helper), 3, "honeycombs in the chest on the ITEM side"))
			.thenSucceed();
	}

	private static int countInChest(GameTestHelper helper) {
		Container chest = (Container) helper.getLevel().getBlockEntity(helper.absolutePos(CHEST_POS));
		int found = 0;
		for (int i = 0; i < chest.getContainerSize(); i++) {
			if (chest.getItem(i).is(Items.HONEYCOMB)) {
				found += chest.getItem(i).getCount();
			}
		}
		return found;
	}
}
