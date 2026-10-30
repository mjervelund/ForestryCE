package forestry.core.platform.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;

/**
 * 26.x removed Block#appendHoverText; {@link forestry.core.platform.item.ItemBlockForestry} asks blocks that
 * implement this for their tooltip lines instead.
 */
public interface ITooltipBlock {
	void appendHoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag);
}
