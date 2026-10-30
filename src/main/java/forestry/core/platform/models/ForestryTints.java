package forestry.core.platform.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.core.platform.block.IColoredBlock;
import forestry.core.platform.item.ITintedItem;
import forestry.core.platform.render.TankRenderInfo;
import forestry.core.platform.tile.IRenderableTile;
import forestry.core.platform.util.RenderUtil;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 26.x tints: blocks register a list of {@link BlockTintSource}s (one per tint index) and items name an
 * {@link ItemTintSource} type in their client item definition. These adapt Forestry's {@link IColoredBlock} and
 * {@link ITintedItem}, which 1.21.1 reached through a shared BlockColor/ItemColor (ClientManager).
 */
public final class ForestryTints {
	public static final int TINT_INDICES = 3;
	public static final List<BlockTintSource> BLOCK = List.of(new BlockTint(0), new BlockTint(1), new BlockTint(2));

	private ForestryTints() {
	}

	private record BlockTint(int tintIndex) implements BlockTintSource {
		@Override
		public int color(BlockState state) {
			return -1;
		}

		@Override
		public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
			if (state.getBlock() instanceof IColoredBlock coloredBlock) {
				return opaque(coloredBlock.colorMultiplier(state, level, pos, this.tintIndex));
			}

			// Packaging machine tank slices. tintIndex 0 is the resource tank, tintIndex 1 the product tank
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof IRenderableTile tile) {
				TankRenderInfo tankInfo = this.tintIndex == 0 ? tile.getResourceTankInfo() : tile.getProductTankInfo();
				FluidStack fluidStack = tankInfo.getFluidStack();
				if (!fluidStack.isEmpty()) {
					return opaque(RenderUtil.getFluidColor(fluidStack.getFluid()));
				}
				return 0xFF000000;
			}
			return -1;
		}
	}

	/**
	 * {@code forestry:item_color}: the colour {@link ITintedItem#getColorFromItemStack} gives for one tint index.
	 */
	public record ItemTint(int tintIndex) implements ItemTintSource {
		public static final MapCodec<ItemTint> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.optionalFieldOf("index", 0).forGetter(ItemTint::tintIndex)
		).apply(instance, ItemTint::new));

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			if (stack.getItem() instanceof ITintedItem tintedItem) {
				return opaque(tintedItem.getColorFromItemStack(stack, this.tintIndex));
			}
			return -1;
		}

		@Override
		public MapCodec<ItemTint> type() {
			return CODEC;
		}
	}

	// IColoredBlock and ITintedItem return 0xRRGGBB; 26.x tints are ARGB
	private static int opaque(int color) {
		return color | 0xFF000000;
	}
}
