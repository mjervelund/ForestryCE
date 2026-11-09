package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.core.content.sorting.DefaultFilterRuleType;
import forestry.core.content.sorting.features.SortingBlocks;
import forestry.core.content.sorting.tiles.TileGeneticFilter;
import forestry.core.platform.legacy.items.IItemHandler;
import forestry.core.platform.util.SpeciesUtil;

/**
 * Regression test for an item duplication in {@code ItemHandlerFilter.insertItem}.
 * <p>
 * The handler loops over every side whose rule accepts the stack and inserts into that side's slot. It used to offer
 * the ORIGINAL stack to each side instead of what the previous side left over. When the first side's slot had room
 * for only part of the stack and the next side took the rest, the full stack landed in the second slot as well, and
 * the handler returned EMPTY: the inserter lost N items while the filter gained more than N.
 */
public class GeneticFilterInsertDupeTest {
	private static final BlockPos FILTER_POS = new BlockPos(8, 2, 8);
	// Direction.VALUES order puts WEST before EAST, so WEST is the first valid side.
	private static final Direction FIRST = Direction.WEST;
	private static final Direction SECOND = Direction.EAST;
	private static final Direction INSERT_FROM = Direction.UP;

	private static final int PREFILL = 60;
	private static final int OFFERED = 16;

	@ForestryGameTest
	public static void geneticFilterInsertConservesItems(GameTestHelper helper) {
		helper.setBlock(FILTER_POS.relative(FIRST), Blocks.CHEST);
		helper.setBlock(FILTER_POS.relative(SECOND), Blocks.CHEST);
		helper.setBlock(FILTER_POS, SortingBlocks.FILTER.defaultState());

		if (!(helper.getLevel().getBlockEntity(helper.absolutePos(FILTER_POS)) instanceof TileGeneticFilter filter)) {
			helper.fail("Expected a TileGeneticFilter at " + FILTER_POS);
			return;
		}

		filter.getLogic().setRule(FIRST, DefaultFilterRuleType.ANYTHING);
		filter.getLogic().setRule(SECOND, DefaultFilterRuleType.ANYTHING);

		ItemStack drone = SpeciesUtil.BEE_TYPE.get().getDefaultSpecies().createStack(BeeLifeStage.DRONE);
		// PREFILL leaves room for some, but not all, of the offered drones in the first slot
		helper.assertTrue(drone.getMaxStackSize() > PREFILL && drone.getMaxStackSize() < PREFILL + OFFERED,
			"drone stack size " + drone.getMaxStackSize() + " does not fit this test");

		// the first valid side only has room for part of the offered stack
		filter.setItem(FIRST.get3DDataValue(), drone.copyWithCount(PREFILL));

		helper.assertTrue(filter.getValidDirections(drone, INSERT_FROM).size() >= 2,
			"expected at least two valid sides, got " + filter.getValidDirections(drone, INSERT_FROM));

		IItemHandler handler = filter.getItemHandler(INSERT_FROM);
		int before = countInFilter(filter);
		ItemStack returned = handler.insertItem(0, drone.copyWithCount(OFFERED), false);
		int after = countInFilter(filter);

		int accepted = OFFERED - returned.getCount();
		helper.assertTrue(after - before == accepted,
			"inserter gave up " + accepted + " drones but the filter gained " + (after - before)
				+ " (before " + before + ", after " + after + ", returned " + returned.getCount() + ")");
		helper.assertTrue(accepted == OFFERED,
			"filter refused " + returned.getCount() + " drones although two sides had room");
		helper.succeed();
	}

	private static int countInFilter(TileGeneticFilter filter) {
		int count = 0;
		for (int i = 0; i < filter.getContainerSize(); i++) {
			count += filter.getItem(i).getCount();
		}
		return count;
	}
}
