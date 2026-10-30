package forestry.core.content.tools;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

import forestry.api.ForestryTags;

public final class ToolTier {
	// bronze sits between iron and stone, so it uses iron's incorrect-for-drops tag
	public static final ToolMaterial SURVIVALIST = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 200, 7.0F, 2.5F, 10, ForestryTags.Items.INGOTS_BRONZE);

	private ToolTier() {
	}
}
