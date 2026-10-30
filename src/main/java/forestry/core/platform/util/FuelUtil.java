package forestry.core.platform.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;

import java.util.Optional;

// 26.x: fuel is the data-driven COOKING_FUEL item component, resolved against a loot context like a furnace's
public class FuelUtil {
	public static boolean isFuel(ItemStack stack) {
		return stack.has(DataComponents.COOKING_FUEL);
	}

	public static int getBurnTime(BlockEntity tile, ItemStack stack) {
		if (!isFuel(stack) || !(tile.getLevel() instanceof ServerLevel level) || !(tile instanceof Container container)) {
			return 0;
		}
		LootContext context = new LootContext.Builder(
			new LootParams.Builder(level)
				.withParameter(LootContextParams.BLOCK_STATE, tile.getBlockState())
				.withParameter(LootContextParams.BLOCK_ENTITY, tile)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(tile.getBlockPos()))
				.withParameter(LootContextParams.CONTAINER, container)
				.withOptionalParameter(NeoForgeLootContextParams.QUERIED_STACK, stack)
				.create(LootContextParamSets.CONTAINER_PROCESS)
		).create(Optional.empty());
		return ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
	}
}
