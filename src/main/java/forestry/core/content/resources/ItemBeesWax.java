package forestry.core.content.resources;

import forestry.core.platform.registration.RegistrationIds;
import net.minecraft.world.item.HoneycombItem;

// Reuse the behavior from HoneycombItem
public class ItemBeesWax extends HoneycombItem {
	public ItemBeesWax() {
		super(RegistrationIds.item());
	}
}
