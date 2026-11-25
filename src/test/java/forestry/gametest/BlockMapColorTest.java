package forestry.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Regression test for ForestryCE#266 (Forestry blocks do not show on maps). {@code BlockBehaviour.Properties.of()}
 * defaults to {@link MapColor#NONE}, which a map draws as transparent, so every block registered without
 * {@code mapColor(...)} (machines, engines, planks, logs, hives, alveary, farms, storage blocks, ...) was invisible.
 * Asserts that every Forestry block whose default state has a full collision cube has a map colour.
 */
public class BlockMapColorTest {
	@ForestryGameTest
	public static void solidBlocksHaveMapColor(GameTestHelper helper) {
		List<String> missing = new ArrayList<>();
		for (Block block : BuiltInRegistries.BLOCK) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			if (!id.getNamespace().startsWith("forestry")) {
				continue;
			}
			BlockState state = block.defaultBlockState();
			if (!state.isCollisionShapeFullBlock(helper.getLevel(), BlockPos.ZERO)) {
				continue;
			}
			if (state.getMapColor(helper.getLevel(), BlockPos.ZERO) == MapColor.NONE) {
				missing.add(id.toString());
			}
		}
		helper.assertTrue(missing.isEmpty(), missing.size() + " solid Forestry blocks have no map colour: " + missing);
		helper.succeed();
	}
}
