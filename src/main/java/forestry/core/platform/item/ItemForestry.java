package forestry.core.platform.item;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import forestry.core.platform.registration.RegistrationIds;
import forestry.core.platform.util.ItemTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemForestry extends Item {
	public ItemForestry() {
		this(RegistrationIds.item());
	}

	public ItemForestry(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag advanced) {
		ItemTooltipUtil.addInformation(stack, tooltip);
	}
}
