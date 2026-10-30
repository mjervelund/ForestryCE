package forestry.mail.blocks;

import forestry.core.platform.registration.RegistrationIds;
import forestry.core.platform.block.BlockBase;

public class MailBlock extends BlockBase<MailBlockType> {
	public MailBlock(MailBlockType blockType) {
		super(RegistrationIds.block(), blockType);
	}
}
