package forestry.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import forestry.core.content.energy.blocks.EngineBlock;
import forestry.core.platform.block.BlockBase;

/**
 * Forestry machines keep their facing in BlockBase's own FACING property and overrode rotate() but not mirror(), so a
 * mirrored structure (structure block, jigsaw piece, WorldEdit flip) left every machine facing its unmirrored way
 * (ForestryCE#440). Engines use VERTICAL_FACING instead, where BlockBase's rotate(state, rotation) threw.
 */
public class BlockMirrorTest {
	@ForestryGameTest
	public static void machinesMirrorTheirFacing(GameTestHelper helper) {
		List<String> wrong = new ArrayList<>();
		int checked = 0;
		for (Block block : BuiltInRegistries.BLOCK) {
			if (!(block instanceof BlockBase<?>)) {
				continue;
			}
			checked++;
			EnumProperty<Direction> facing = block instanceof EngineBlock ? EngineBlock.VERTICAL_FACING : BlockBase.FACING;
			String id = BuiltInRegistries.BLOCK.getKey(block).toString();
			try {
				BlockState north = block.defaultBlockState().setValue(facing, Direction.NORTH);
				BlockState east = block.defaultBlockState().setValue(facing, Direction.EAST);
				if (north.mirror(Mirror.LEFT_RIGHT).getValue(facing) != Direction.SOUTH
					|| east.mirror(Mirror.FRONT_BACK).getValue(facing) != Direction.WEST
					|| north.mirror(Mirror.FRONT_BACK).getValue(facing) != Direction.NORTH
					|| north.rotate(Rotation.CLOCKWISE_90).getValue(facing) != Direction.EAST) {
					wrong.add(id);
				}
			} catch (RuntimeException e) {
				wrong.add(id + " (" + e.getMessage() + ")");
			}
		}
		helper.assertTrue(checked > 0, "no BlockBase blocks registered");
		helper.assertTrue(wrong.isEmpty(), wrong.size() + " of " + checked + " machine blocks do not mirror/rotate their facing: " + wrong);
		helper.succeed();
	}
}
