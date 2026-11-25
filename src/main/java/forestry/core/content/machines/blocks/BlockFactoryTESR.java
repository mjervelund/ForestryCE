package forestry.core.content.machines.blocks;

import net.minecraft.world.level.material.MapColor;
import forestry.core.platform.registration.RegistrationIds;
import forestry.core.platform.block.BlockBase;

public class BlockFactoryTESR extends BlockBase<BlockTypeFactoryTesr> {
	public BlockFactoryTESR(BlockTypeFactoryTesr type) {
		super(RegistrationIds.block().mapColor(MapColor.METAL), type);
	}
}
