package forestry.gametest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;

import forestry.agriculture.farmlogic.FarmLogicOrchard;
import forestry.api.IForestryApi;
import forestry.api.agriculture.ForestryFarmTypes;
import forestry.api.agriculture.ICrop;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.leaves.ForestryLeafType;
import forestry.arboriculture.leaves.LeavesBlockEntity;

/**
 * ForestryCE#372 "Orchards cannot harvest fruit/nuts from leaves". Trees of a species' default
 * genome (all worldgen trees, and trees grown from plain saplings) get tile-less {@code DefaultFruitLeavesBlock} fruit
 * leaves, which always look ripe; the orchard only looked for {@code IFruitBearer} block entities, so it never saw them.
 */
public class OrchardFruitLeavesTest {
	private static final BlockPos TRUNK = new BlockPos(1, 1, 1);
	private static final BlockPos FRUIT = new BlockPos(2, 2, 1);

	@ForestryGameTest
	public static void orchardHarvestsDefaultFruitLeaves(GameTestHelper helper) {
		helper.setBlock(TRUNK, Blocks.OAK_LOG);
		helper.setBlock(TRUNK.above(), Blocks.OAK_LOG);
		helper.setBlock(FRUIT, ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.LEMON).defaultState().setValue(LeavesBlock.PERSISTENT, true));

		FarmLogicOrchard orchard = (FarmLogicOrchard) IForestryApi.INSTANCE.getFarmingManager()
			.getFarmType(ForestryFarmTypes.ORCHARD).getLogic(false);
		Collection<ICrop> crops;
		try {
			Method scan = FarmLogicOrchard.class.getDeclaredMethod("getHarvestBlocks", Level.class, BlockPos.class);
			scan.setAccessible(true);
			@SuppressWarnings("unchecked")
			Collection<ICrop> found = (Collection<ICrop>) scan.invoke(orchard, helper.getLevel(), helper.absolutePos(TRUNK));
			crops = found;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
		helper.assertTrue(crops.stream().anyMatch(crop -> crop.getPosition().equals(helper.absolutePos(FRUIT))),
			"the orchard did not find the ripe lemon leaves (found " + crops.size() + " crops)");

		List<ItemStack> drops = new ArrayList<>();
		for (ICrop crop : crops) {
			List<ItemStack> harvested = crop.harvest();
			if (harvested != null) {
				drops.addAll(harvested);
			}
		}
		helper.assertTrue(drops.stream().anyMatch(stack -> !stack.isEmpty()), "harvesting the lemon leaves gave no fruit");

		// the leaves now carry the tree's genome and grow their fruit back
		helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(FRUIT)) instanceof LeavesBlockEntity, "harvested leaves have no block entity");
		LeavesBlockEntity leaves = (LeavesBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(FRUIT));
		helper.assertTrue(leaves.hasFruit() && leaves.getRipeness() < 1.0f, "harvested leaves are not regrowing fruit leaves");
		helper.succeed();
	}
}
