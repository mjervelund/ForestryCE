package forestry.mail.blocks;

import net.minecraft.world.level.material.MapColor;
import forestry.core.platform.registration.RegistrationIds;
import forestry.core.platform.block.BlockBase;

public class MailBlock extends BlockBase<MailBlockType> {
	public MailBlock(MailBlockType blockType) {
		super(RegistrationIds.block().mapColor(MapColor.METAL), blockType);
	}
}
