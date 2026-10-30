package forestry.core.content.machines.blocks;

import forestry.core.platform.registration.RegistrationIds;
import forestry.core.platform.block.BlockBase;

public class BlockFactoryTESR extends BlockBase<BlockTypeFactoryTesr> {
	public BlockFactoryTESR(BlockTypeFactoryTesr type) {
		super(RegistrationIds.block(), type);
	}
}
