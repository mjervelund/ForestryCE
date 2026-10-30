package forestry.core.platform.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.item.ToolMaterial;
import javax.annotation.Nullable;

public interface HasRemnants {
	/**
	 * A tool that leaves remnants when it breaks. 26.x tools are plain items configured through
	 * {@code Item.Properties#pickaxe/axe/shovel/hoe/sword}, so one class covers every tool kind.
	 */
	class Tool extends Item implements HasRemnants {
		private final Supplier<ItemStack> remnants;

		public Tool(Properties properties, Supplier<ItemStack> remnants) {
			super(properties);
			this.remnants = remnants;
		}

		@Override
		public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<ItemStack> onBroken) {
			if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
				if (entity instanceof Player player) {
					// make sure it's really broken
					stack.shrink(1);
					player.getInventory().add(this.remnants.get());
				}
			}
			return super.damageItem(stack, amount, entity, onBroken);
		}
	}
}
