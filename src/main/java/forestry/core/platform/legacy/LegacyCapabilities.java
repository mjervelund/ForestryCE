package forestry.core.platform.legacy;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

import forestry.core.platform.legacy.energy.IEnergyStorage;
import forestry.core.platform.multiblock.MultiblockTileEntityForestry;
import forestry.core.platform.legacy.fluids.capability.IFluidHandler;
import forestry.core.platform.legacy.items.IItemHandler;
import forestry.core.platform.legacy.items.wrapper.InvWrapper;
import forestry.core.platform.legacy.items.wrapper.SidedInvWrapper;

/**
 * Registers and looks up Forestry's legacy handlers on the 26.x capabilities
 * ({@link Capabilities.Item}, {@link Capabilities.Fluid}, {@link Capabilities.Energy}).
 */
public final class LegacyCapabilities {
	private LegacyCapabilities() {
	}

	public static ResourceHandler<ItemResource> items(IItemHandler handler) {
		if (handler instanceof SidedInvWrapper sided) {
			if (sided.getInv() instanceof MultiblockTileEntityForestry<?>) {
				// delegating container: its size changes with the multiblock state, see LiveContainerHandler
				return new LiveContainerHandler(() -> new WorldlyContainerWrapper(sided.getInv(), sided.getSide()));
			}
			return new WorldlyContainerWrapper(sided.getInv(), sided.getSide());
		}
		if (handler instanceof InvWrapper wrapper) {
			if (wrapper.getInv() instanceof MultiblockTileEntityForestry<?>) {
				return new LiveContainerHandler(() -> VanillaContainerWrapper.of(wrapper.getInv()));
			}
			return VanillaContainerWrapper.of(wrapper.getInv());
		}
		return new DeferredResourceHandlers.Items(handler);
	}

	public static ResourceHandler<FluidResource> fluids(IFluidHandler handler) {
		return new DeferredResourceHandlers.Fluids(handler);
	}

	public static EnergyHandler energy(IEnergyStorage storage) {
		return new DeferredResourceHandlers.Energy(storage);
	}

	public static <T extends BlockEntity> void registerItems(RegisterCapabilitiesEvent event, BlockEntityType<T> type, BiFunction<T, Direction, IItemHandler> provider) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, type, (tile, side) -> {
			IItemHandler handler = provider.apply(tile, side);
			return handler == null ? null : items(handler);
		});
	}

	public static <T extends BlockEntity> void registerFluids(RegisterCapabilitiesEvent event, BlockEntityType<T> type, BiFunction<T, Direction, IFluidHandler> provider) {
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, (tile, side) -> {
			IFluidHandler handler = provider.apply(tile, side);
			return handler == null ? null : fluids(handler);
		});
	}

	public static <T extends BlockEntity> void registerEnergy(RegisterCapabilitiesEvent event, BlockEntityType<T> type, BiFunction<T, Direction, IEnergyStorage> provider) {
		event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (tile, side) -> {
			IEnergyStorage storage = provider.apply(tile, side);
			return storage == null ? null : energy(storage);
		});
	}

	@Nullable
	public static IItemHandler getItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
		ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
		if (handler == null) {
			return null;
		}
		return handler instanceof DeferredResourceHandlers.Items deferred ? deferred.handler() : IItemHandler.of(handler);
	}

	@Nullable
	public static IFluidHandler getFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
		ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
		if (handler == null) {
			return null;
		}
		return handler instanceof DeferredResourceHandlers.Fluids deferred ? deferred.handler() : IFluidHandler.of(handler);
	}

	@Nullable
	public static IEnergyStorage getEnergyStorage(Level level, BlockPos pos, @Nullable Direction side) {
		EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
		if (handler == null) {
			return null;
		}
		return handler instanceof DeferredResourceHandlers.Energy deferred ? deferred.storage() : IEnergyStorage.of(handler);
	}
}
