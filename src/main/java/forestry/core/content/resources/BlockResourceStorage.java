package forestry.core.content.resources;

import net.minecraft.world.level.material.MapColor;
import forestry.core.platform.registration.RegistrationIds;
import net.minecraft.world.level.block.Block;

public class BlockResourceStorage extends Block {
	private final EnumResourceType type;

	public BlockResourceStorage(EnumResourceType type) {
		super(RegistrationIds.block().mapColor(switch (type) {
			case APATITE -> MapColor.COLOR_LIGHT_BLUE;
			case TIN -> MapColor.METAL;
			case BRONZE -> MapColor.COLOR_ORANGE;
			case AMBER -> MapColor.TERRACOTTA_ORANGE;
			case SILICON -> MapColor.COLOR_GRAY;
		}).strength(3f, 5f));
		this.type = type;
	}

	public EnumResourceType getType() {
		return this.type;
	}
}
