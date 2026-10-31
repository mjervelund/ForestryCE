package forestry.api.core;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/**
 * Default implementation of {@link IFluidProduct}: a fixed fluid output. Used by all of Forestry's own squeezer
 * recipes. Wraps a {@link FluidStackTemplate} so fluid, amount, and data components are captured together; a template
 * because squeezer recipes decode at datapack load, before fluid components are bound.
 *
 * @param template The fluid this product always produces.
 */
public record FluidProduct(FluidStackTemplate template) implements IFluidProduct {
	public FluidProduct {
		Preconditions.checkNotNull(template);
	}

	public FluidProduct(FluidStack stack) {
		this(FluidStackTemplate.fromNonEmptyStack(stack));
	}

	public static final MapCodec<FluidProduct> MAP_CODEC = FluidStackTemplate.CODEC.fieldOf("stack").xmap(FluidProduct::new, FluidProduct::template);
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidProduct> STREAM_CODEC = FluidStackTemplate.STREAM_CODEC.map(FluidProduct::new, FluidProduct::template);
	/**
	 * The default product type. The dispatch codec {@link IFluidProduct#CODEC} treats this type
	 * specially: products of this type serialize without a {@code "type"} key, and a missing {@code "type"} key on
	 * decode resolves back to it.
	 */
	public static final FluidProductType<FluidProduct> TYPE = new FluidProductType<>(MAP_CODEC, STREAM_CODEC);

	public FluidStack stack() {
		return this.template.create();
	}

	@Override
	public FluidStack createFluidStack() {
		return this.template.create();
	}

	@Override
	public FluidStack createRandomFluidStack(RandomSource random) {
		return this.template.create();
	}

	@Override
	public FluidProductType<?> type() {
		return TYPE;
	}

	public static FluidProduct of(FluidStack stack) {
		return new FluidProduct(stack);
	}

	public static FluidProduct of(Fluid fluid, int amount) {
		return new FluidProduct(new FluidStackTemplate(fluid, amount));
	}
}
