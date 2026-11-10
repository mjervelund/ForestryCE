package forestry.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import forestry.arboriculture.features.CharcoalBlocks;

/**
 * The ash block left by a burnt charcoal pile must be mineable with a shovel, so shovels break it faster than a bare hand
 * (ForestryCE#289).
 */
public class AshBlockToolTest {
	@ForestryGameTest
	public static void ashIsMineableWithShovel(GameTestHelper helper) {
		BlockState ash = CharcoalBlocks.ASH.defaultState();
		helper.assertTrue(ash.is(BlockTags.MINEABLE_WITH_SHOVEL), "ash block is not in minecraft:mineable/shovel");
		float speed = new ItemStack(Items.IRON_SHOVEL).getDestroySpeed(ash);
		helper.assertTrue(speed > 1.0F, "iron shovel destroy speed on ash is " + speed + ", expected > 1");
		helper.succeed();
	}
}
