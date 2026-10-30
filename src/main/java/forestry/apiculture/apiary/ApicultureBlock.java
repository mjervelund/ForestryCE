package forestry.apiculture.apiary;

import forestry.core.platform.registration.RegistrationIds;
import forestry.core.platform.block.BlockBase;
import net.minecraft.world.level.block.SoundType;

public class ApicultureBlock extends BlockBase<ApicultureBlockType> {
	public ApicultureBlock(ApicultureBlockType type) {
		super(RegistrationIds.block().sound(SoundType.WOOD), type);
	}
}
