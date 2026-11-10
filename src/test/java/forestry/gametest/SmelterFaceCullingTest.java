package forestry.gametest;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.core.content.machines.blocks.BlockTypeFactoryPlain;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.platform.block.BlockBase;

/**
 * The smelter's model is the hollow plain-machine frame (two end plates and a smaller core), not a full cube, so it must
 * not cull the faces of neighbours beside its open sides (ForestryCE#402). Its end plates still cull, and the thermionic
 * fabricator, whose model is a full cube, still culls on every side.
 */
public class SmelterFaceCullingTest {
	@ForestryGameTest
	public static void smelterDoesNotCullNeighboursBesideItsOpenSides(GameTestHelper helper) {
		// facing north: end plates on the north and south faces, open core on east and west
		BlockState smelter = FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.SMELTER).defaultState()
			.setValue(BlockBase.FACING, Direction.NORTH);
		BlockState fabricator = FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR).defaultState()
			.setValue(BlockBase.FACING, Direction.NORTH);
		BlockState stone = Blocks.STONE.defaultBlockState();

		// a stone block east of the smelter: its west face looks at the smelter's open side
		helper.assertTrue(Block.shouldRenderFace(stone, smelter, Direction.WEST),
			"smelter culls the face of a neighbour beside its open (east) side");
		// a stone block north of the smelter: its south face is covered by the smelter's full end plate
		helper.assertFalse(Block.shouldRenderFace(stone, smelter, Direction.SOUTH),
			"smelter end plate no longer culls the neighbour face it covers");
		helper.assertFalse(Block.shouldRenderFace(stone, fabricator, Direction.WEST),
			"full-cube thermionic fabricator no longer culls its neighbour");
		helper.succeed();
	}
}
