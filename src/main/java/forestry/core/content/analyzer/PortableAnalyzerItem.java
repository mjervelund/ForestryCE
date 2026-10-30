package forestry.core.content.analyzer;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import forestry.core.features.CoreDataComponents;
import forestry.core.content.analyzer.PortableAnalyzerMenu;
import forestry.core.platform.inventory.PortableAnalyzerInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import forestry.core.platform.item.WithScreenItem;

public class PortableAnalyzerItem extends WithScreenItem {
	public PortableAnalyzerItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, context, display, tooltip, advanced);
		int charges = stack.getOrDefault(CoreDataComponents.ALYZER_CHARGES, 0);
		tooltip.accept(Component.translatable(stack.getItem().getDescriptionId() + ".charges", charges).withStyle(ChatFormatting.GOLD));
	}

	@Override
	public AbstractContainerMenu getContainer(int containerId, Player player, ItemStack heldItem) {
		return new PortableAnalyzerMenu(containerId, new PortableAnalyzerInventory(player, heldItem), player);
	}
}
