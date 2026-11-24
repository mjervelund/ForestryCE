package forestry.gametest;

import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.trees.TreeItem;
import forestry.core.platform.util.SpeciesUtil;

/**
 * ForestryCE#173 "Bug in sapling placement logic": clicking the side of a grass block with a sapling was reported to
 * plant it on top of the clicked block instead of next to it. On 26.3 the sapling goes next to the clicked face, like
 * a vanilla sapling; this guards that.
 */
public class SaplingSidePlacementTest {
	private static final BlockPos GRASS = new BlockPos(1, 2, 1);
	private static final BlockPos BESIDE = GRASS.east();

	@ForestryGameTest
	public static void saplingGoesBesideTheClickedFace(GameTestHelper helper) {
		helper.setBlock(GRASS.below(), Blocks.DIRT);
		helper.setBlock(GRASS, Blocks.GRASS_BLOCK);
		helper.setBlock(GRASS.above(), Blocks.AIR);
		helper.setBlock(BESIDE.below(), Blocks.DIRT);
		helper.setBlock(BESIDE, Blocks.AIR);

		ITree tree = SpeciesUtil.TREE_TYPE.get().getDefaultSpecies().createIndividual();
		ItemStack sapling = tree.createStack(TreeLifeStage.SAPLING);
		Player player = FakePlayerFactory.getMinecraft(helper.getLevel());
		player.setItemInHand(InteractionHand.MAIN_HAND, sapling);

		BlockPos grassAbs = helper.absolutePos(GRASS);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(grassAbs).add(0.5, 0, 0), Direction.EAST, grassAbs, false);
		BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
		InteractionResult result;
		try {
			Method place = TreeItem.class.getDeclaredMethod("onItemRightClickSapling", ItemStack.class, Level.class, Player.class, BlockPos.class, ITree.class, BlockPlaceContext.class);
			place.setAccessible(true);
			result = (InteractionResult) place.invoke(null, sapling, helper.getLevel(), player, grassAbs, tree, context);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		} finally {
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}

		helper.assertTrue(result.consumesAction(), "sapling was not planted: " + result);
		helper.assertBlockPresent(ArboricultureBlocks.SAPLING_GE.block(), BESIDE);
		helper.assertBlockPresent(Blocks.AIR, GRASS.above());
		helper.succeed();
	}
}
