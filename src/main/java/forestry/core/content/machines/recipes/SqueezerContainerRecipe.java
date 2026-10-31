package forestry.core.content.machines.recipes;

import forestry.api.core.machines.IForestryRecipe;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.core.FluidProductType;
import forestry.api.core.IFluidProduct;
import forestry.api.core.machines.ISqueezerContainerRecipe;
import forestry.core.content.machines.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class SqueezerContainerRecipe implements ISqueezerContainerRecipe {
	// Container recipes have no fluid output at all; this is never serialized (there is no "output" codec field
	// below), so createFluidStack() being EMPTY is enough to make TileSqueezer refuse a fluid-fill for these recipes.
	private static final IFluidProduct NO_FLUID_OUTPUT = new IFluidProduct() {
		@Override
		public FluidStack createFluidStack() {
			return FluidStack.EMPTY;
		}

		@Override
		public FluidProductType<?> type() {
			throw new UnsupportedOperationException("SqueezerContainerRecipe's fluid output is never serialized");
		}
	};

	private static final MapCodec<SqueezerContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Identifier.CODEC.fieldOf("id").forGetter(SqueezerContainerRecipe::getId),
		ItemStackTemplate.CODEC.fieldOf("container").forGetter(r -> r.emptyContainer),
		Codec.INT.fieldOf("time").forGetter(SqueezerContainerRecipe::getProcessingTime),
		ItemStackTemplate.CODEC.optionalFieldOf("remnants").forGetter(r -> java.util.Optional.ofNullable(r.remnants)),
		Codec.FLOAT.fieldOf("remnantsChance").forGetter(SqueezerContainerRecipe::getRemnantsChance)
	).apply(instance, (id, container, time, remnants, chance) -> new SqueezerContainerRecipe(id, container, time, remnants.orElse(null), chance)));
	private static final StreamCodec<RegistryFriendlyByteBuf, SqueezerContainerRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final Identifier id;
	private final ItemStackTemplate emptyContainer;
	private final int processingTime;
	@Nullable
	private final ItemStackTemplate remnants;
	private final float remnantsChance;

	public SqueezerContainerRecipe(Identifier id, ItemStack emptyContainer, int processingTime, ItemStack remnants, float remnantsChance) {
		this(id, ItemStackTemplate.fromNonEmptyStack(emptyContainer), processingTime, remnants.isEmpty() ? null : ItemStackTemplate.fromStack(remnants), remnantsChance);
	}

	// Decoded at datapack load, before item components are bound, so the stacks are kept as templates
	public SqueezerContainerRecipe(Identifier id, ItemStackTemplate emptyContainer, int processingTime, @Nullable ItemStackTemplate remnants, float remnantsChance) {
		this.id = id;
		Preconditions.checkNotNull(emptyContainer);

		this.emptyContainer = emptyContainer;
		this.processingTime = processingTime;
		this.remnants = remnants;
		this.remnantsChance = remnantsChance;
	}

	@Override
	public ItemStack getEmptyContainer() {
		return this.emptyContainer.create();
	}

	@Override
	public List<Ingredient> getInputs() {
		return List.of();
	}

	@Override
	public int getProcessingTime() {
		return this.processingTime;
	}

	@Override
	public ItemStack getRemnants() {
		return this.remnants == null ? ItemStack.EMPTY : this.remnants.create();
	}

	@Override
	public float getRemnantsChance() {
		return this.remnantsChance;
	}

	@Override
	public IFluidProduct getFluidOutput() {
		return NO_FLUID_OUTPUT;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider lookupProvider) {
		return getRemnants();
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<? extends IForestryRecipe> getSerializer() {
		return FactoryRecipeTypes.SQUEEZER_CONTAINER.serializer();
	}

	@Override
	public RecipeType<? extends IForestryRecipe> getType() {
		return FactoryRecipeTypes.SQUEEZER_CONTAINER.type();
	}

	public static final RecipeSerializer<SqueezerContainerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private static class Serializer {
		private static SqueezerContainerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			Identifier recipeId = Identifier.STREAM_CODEC.decode(buffer);
			ItemStackTemplate emptyContainer = ItemStackTemplate.STREAM_CODEC.decode(buffer);
			int processingTime = ByteBufCodecs.VAR_INT.decode(buffer);
			ItemStackTemplate remnants = buffer.readBoolean() ? ItemStackTemplate.STREAM_CODEC.decode(buffer) : null;
			float remnantsChance = ByteBufCodecs.FLOAT.decode(buffer);

			return new SqueezerContainerRecipe(recipeId, emptyContainer, processingTime, remnants, remnantsChance);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, SqueezerContainerRecipe recipe) {
			Identifier.STREAM_CODEC.encode(buffer, recipe.id);
			ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.emptyContainer);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.processingTime);
			buffer.writeBoolean(recipe.remnants != null);
			if (recipe.remnants != null) {
				ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.remnants);
			}
			ByteBufCodecs.FLOAT.encode(buffer, recipe.remnantsChance);
		}
	}
}
