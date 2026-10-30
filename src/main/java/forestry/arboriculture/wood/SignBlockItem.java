package forestry.arboriculture.wood;

import forestry.arboriculture.features.ArboricultureBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;

// 26.x: SignItem is gone; vanilla signs are StandingAndWallBlockItems with sign text in their properties
public class SignBlockItem extends StandingAndWallBlockItem {
	public SignBlockItem(ForestryStandingSignBlock block, Item.Properties properties) {
		super(block, ArboricultureBlocks.WALL_SIGN.get(block.getWoodType()).block(), Direction.DOWN, properties.signText());
	}
}
