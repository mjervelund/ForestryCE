package forestry.core.platform.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.core.platform.legacy.fluids.FluidUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import org.joml.Matrix4fc;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * {@code forestry:fluid_container}: the capsules, cans and jars, showing the fluid the stack holds.
 * <p>
 * 26.x: NeoForge's {@link DynamicFluidContainerModel} names one fixed fluid, so this bakes it once per source fluid
 * and picks the one the stack contains (1.21.1 had its own geometry loader with an override cache). The fluid now
 * fills the shape of the "fluid" mask texture, as NeoForge's buckets do, instead of a fixed rectangle.
 */
public final class FluidContainerModel implements ItemModel {
	private final Map<Fluid, ItemModel> models;
	private final ItemModel empty;

	private FluidContainerModel(Map<Fluid, ItemModel> models, ItemModel empty) {
		this.models = models;
		this.empty = empty;
	}

	@Override
	public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		ItemModel model = FluidUtil.getFluidContained(item)
			.map(fluidStack -> this.models.getOrDefault(fluidStack.getFluid(), this.empty))
			.orElse(this.empty);
		model.update(output, item, resolver, displayContext, level, owner, seed);
	}

	public record Unbaked(DynamicFluidContainerModel.Textures textures, boolean coverIsMask, boolean applyFluidLuminosity) implements ItemModel.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			DynamicFluidContainerModel.Textures.CODEC.fieldOf("textures").forGetter(Unbaked::textures),
			Codec.BOOL.optionalFieldOf("cover_is_mask", true).forGetter(Unbaked::coverIsMask),
			Codec.BOOL.optionalFieldOf("apply_fluid_luminosity", true).forGetter(Unbaked::applyFluidLuminosity)
		).apply(instance, Unbaked::new));

		private ItemModel.Unbaked forFluid(Fluid fluid) {
			return new DynamicFluidContainerModel.Unbaked(this.textures, fluid, false, this.coverIsMask, this.applyFluidLuminosity);
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
			forFluid(Fluids.EMPTY).resolveDependencies(resolver);
		}

		@Override
		public ItemModel bake(BakingContext context, Matrix4fc transformation) {
			Map<Fluid, ItemModel> models = new IdentityHashMap<>();
			for (Fluid fluid : BuiltInRegistries.FLUID) {
				if (fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource()) {
					models.put(fluid, forFluid(fluid).bake(context, transformation));
				}
			}
			return new FluidContainerModel(models, forFluid(Fluids.EMPTY).bake(context, transformation));
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
