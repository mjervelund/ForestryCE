package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import forestry.core.features.CoreItems;

/**
 * ForestryCE#399 "wrench doesn't work" on rotatable vanilla blocks. On 26.3 the wrench turns stairs and a furnace a
 * quarter turn clockwise; this guards that.
 */
public class WrenchRotationTest {
	private static final BlockPos STAIRS = new BlockPos(1, 1, 1);
	private static final BlockPos FURNACE = new BlockPos(3, 1, 1);

	@ForestryGameTest
	public static void wrenchRotatesVanillaBlocks(GameTestHelper helper) {
		helper.setBlock(STAIRS, Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
		helper.setBlock(FURNACE, Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));

		wrench(helper, STAIRS);
		wrench(helper, FURNACE);

		helper.assertBlockProperty(STAIRS, StairBlock.FACING, Direction.EAST);
		helper.assertBlockProperty(FURNACE, BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
		helper.succeed();
	}

	private static void wrench(GameTestHelper helper, BlockPos pos) {
		Player player = FakePlayerFactory.getMinecraft(helper.getLevel());
		ItemStack wrench = CoreItems.WRENCH.stack();
		player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
		BlockPos abs = helper.absolutePos(pos);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
		try {
			InteractionResult result = wrench.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
			helper.assertTrue(result.consumesAction(), "wrench did nothing on " + helper.getBlockState(pos) + ": " + result);
		} finally {
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}
	}
}
