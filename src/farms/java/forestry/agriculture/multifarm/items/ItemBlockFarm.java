package forestry.agriculture.multifarm.items;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import forestry.core.platform.registration.RegistrationIds;
import net.minecraft.client.Minecraft;
import forestry.core.platform.util.TranslationKeys;
import forestry.core.platform.item.ItemBlockForestry;
import forestry.agriculture.multifarm.blocks.MultifarmBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class ItemBlockFarm extends ItemBlockForestry<MultifarmBlock> {
	public ItemBlockFarm(MultifarmBlock block) {
		super(block, RegistrationIds.item());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (Minecraft.getInstance().hasShiftDown()) {
			tooltip.accept(Component.translatable("block.forestry.farm.tooltip").withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.accept(Component.translatable(TranslationKeys.HOLD_SHIFT_FOR_DETAILS).withStyle(ChatFormatting.GRAY));
		}
	}
}
