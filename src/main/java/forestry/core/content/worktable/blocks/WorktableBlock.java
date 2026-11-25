package forestry.core.content.worktable.blocks;

import net.minecraft.world.level.material.MapColor;
import forestry.core.platform.block.ITooltipBlock;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import forestry.core.platform.block.BlockBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class WorktableBlock extends BlockBase<WorktableBlockType> implements ITooltipBlock {
	public WorktableBlock(BlockBehaviour.Properties properties, WorktableBlockType blockType) {
		super(properties.mapColor(MapColor.WOOD).sound(SoundType.WOOD), blockType);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag) {
		if (!stack.getComponents().isEmpty()) {
			tooltip.accept(Component.translatable("block.forestry.worktable_tooltip"));
		}
	}
}
