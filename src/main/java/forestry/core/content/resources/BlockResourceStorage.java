package forestry.core.content.resources;

import forestry.core.platform.registration.RegistrationIds;
import net.minecraft.world.level.block.Block;

public class BlockResourceStorage extends Block {
	private final EnumResourceType type;

	public BlockResourceStorage(EnumResourceType type) {
		super(RegistrationIds.block().strength(3f, 5f));
		this.type = type;
	}

	public EnumResourceType getType() {
		return this.type;
	}
}
