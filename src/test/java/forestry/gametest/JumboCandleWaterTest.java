package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import forestry.core.content.decorative.BlockJumboCandle;
import forestry.core.content.decorative.BlockTypeJumboCandle;
import forestry.core.features.CoreBlocks;

// https://github.com/thedarkcolour/ForestryCE/issues/377: a jumbo candle under water could be lit with flint and steel
// or a fire charge, and stayed lit until a neighbour update put it out
public final class JumboCandleWaterTest {
	private static final BlockPos CANDLE = new BlockPos(2, 2, 2);

	private JumboCandleWaterTest() {
	}

	@ForestryGameTest
	public static void jumboCandleLightsInOpenAir(GameTestHelper helper) {
		helper.setBlock(CANDLE.above(), Blocks.AIR);
		assertLighting(helper, new ItemStack(Items.FLINT_AND_STEEL), true);
		helper.succeed();
	}

	@ForestryGameTest
	public static void jumboCandleDoesNotLightUnderWater(GameTestHelper helper) {
		helper.setBlock(CANDLE.above(), Blocks.WATER);
		assertLighting(helper, new ItemStack(Items.FLINT_AND_STEEL), false);
		assertLighting(helper, new ItemStack(Items.FIRE_CHARGE), false);
		helper.succeed();
	}

	@ForestryGameTest
	public static void jumboCandleDoesNotLightUnderWaterloggedBlock(GameTestHelper helper) {
		helper.setBlock(CANDLE.above(), Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true));
		assertLighting(helper, new ItemStack(Items.FLINT_AND_STEEL), false);
		helper.succeed();
	}

	private static void assertLighting(GameTestHelper helper, ItemStack stack, boolean expectLit) {
		helper.setBlock(CANDLE, CoreBlocks.JUMBO_CANDLES.get(BlockTypeJumboCandle.NORMAL).defaultState());
		BlockPos pos = helper.absolutePos(CANDLE);
		BlockState state = helper.getLevel().getBlockState(pos);
		helper.assertFalse(state.getValue(BlockJumboCandle.LIT), "a freshly placed jumbo candle is lit");

		ServerPlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
		InteractionResult result = state.useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);

		boolean lit = helper.getLevel().getBlockState(pos).getValue(BlockJumboCandle.LIT);
		String what = stack.getItem() + " with " + helper.getLevel().getBlockState(pos.above()) + " above";
		helper.assertTrue(lit == expectLit, what + ": lit=" + lit + ", expected " + expectLit + " (result " + result + ")");
		if (!expectLit) {
			helper.assertFalse(result instanceof InteractionResult.Success, what + ": the interaction succeeded although the candle stayed unlit");
		}
	}
}
