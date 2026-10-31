package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.core.features.CompostablesAndFuels;
import forestry.core.features.CoreItems;

/**
 * Guard for composter chances and burn times. 26.3 removed the {@code neoforge:compostables} and
 * {@code neoforge:furnace_fuels} data maps Forestry shipped them in; the old files only log a WARN at boot, and
 * every Forestry fuel and compostable silently stops working. They are default item components now, naming
 * {@code context_int_provider} entries, three of them Forestry's own datapack JSON.
 */
public class CompostablesAndFuelsTest {
	private static final BlockPos POS = new BlockPos(1, 1, 1);

	@ForestryGameTest
	public static void forestryContextProvidersLoad(GameTestHelper helper) {
		// Loot data like the context providers lives in the reloadable registries, not the level's registryAccess
		var providers = helper.getLevel().getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.CONTEXT_INT_PROVIDER);
		for (ResourceKey<ContextIntProvider> key : new ResourceKey[]{CompostablesAndFuels.COMPOSTABLE_HIGH,
				CompostablesAndFuels.COOKING_TIME_PEAT, CompostablesAndFuels.COOKING_TIME_BITUMINOUS_PEAT,
				CompostablesAndFuels.COOKING_TIME_LOG_PILE, CompostablesAndFuels.COOKING_TIME_PLYWOOD_SHEET}) {
			helper.assertTrue(providers.get(key).isPresent(), key.identifier() + " did not load from the datapack");
		}
		helper.succeed();
	}

	@ForestryGameTest
	public static void peatCarriesItsComponents(GameTestHelper helper) {
		ItemStack peat = new ItemStack(CoreItems.PEAT.item());
		CookingFuel fuel = peat.get(DataComponents.COOKING_FUEL);
		Compostable compostable = peat.get(DataComponents.COMPOSTABLE);

		helper.assertTrue(fuel != null, "Peat is not a furnace fuel");
		helper.assertTrue(fuel.burnTime().equals(ResolvableInt.fromKey(CompostablesAndFuels.COOKING_TIME_PEAT)),
			"Peat burns for " + fuel.burnTime() + " instead of forestry:cooking/time_peat");
		helper.assertTrue(compostable != null, "Peat is not compostable");
		helper.succeed();
	}

	@ForestryGameTest
	public static void furnaceBurnsPeatFor2000Ticks(GameTestHelper helper) {
		burnPeat(helper, Blocks.FURNACE, 2000);
	}

	@ForestryGameTest
	public static void blastFurnaceBurnsPeatTwiceAsFast(GameTestHelper helper) {
		burnPeat(helper, Blocks.BLAST_FURNACE, 1000);
	}

	private static void burnPeat(GameTestHelper helper, Block furnaceBlock, int expected) {
		helper.setBlock(POS, furnaceBlock);
		var furnace = (AbstractFurnaceBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(POS));
		furnace.setItem(0, new ItemStack(Items.RAW_IRON, 8));
		furnace.setItem(1, new ItemStack(CoreItems.PEAT.item()));

		helper.succeedWhen(() -> {
			int litTotalTime = furnace.saveWithoutMetadata(helper.getLevel().registryAccess()).getIntOr("lit_total_time", 0);
			helper.assertTrue(litTotalTime == expected, "Peat lit the furnace for " + litTotalTime + " ticks instead of " + expected);
		});
	}

	@ForestryGameTest
	public static void composterTakesLeavesAndCompost(GameTestHelper helper) {
		helper.setBlock(POS, Blocks.COMPOSTER);
		BlockPos pos = helper.absolutePos(POS);
		ItemStack leaves = new ItemStack(ArboricultureBlocks.LEAVES_DECORATIVE.getItems().iterator().next());
		ItemStack compost = new ItemStack(CoreItems.COMPOST.item());

		// An empty composter always takes a layer, and compost always adds one, so neither is random
		BlockState state = ComposterBlock.insertItem(null, helper.getLevel().getBlockState(pos), helper.getLevel(), leaves, pos);
		helper.assertTrue(state.getValue(ComposterBlock.LEVEL) == 1, "Decorative leaves did not fill an empty composter");
		state = ComposterBlock.insertItem(null, state, helper.getLevel(), compost, pos);
		helper.assertTrue(state.getValue(ComposterBlock.LEVEL) == 2, "Compost did not always add a layer");
		helper.succeed();
	}
}
