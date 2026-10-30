package forestry.core.content.machines.recipes;

import forestry.api.core.machines.IForestryRecipe;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.core.IFluidProduct;
import forestry.api.core.machines.ISqueezerRecipe;
import forestry.core.content.machines.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public class SqueezerRecipe implements ISqueezerRecipe {
	private static final MapCodec<SqueezerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Identifier.CODEC.fieldOf("id").forGetter(SqueezerRecipe::getId),
		Codec.INT.fieldOf("time").forGetter(SqueezerRecipe::getProcessingTime),
		Ingredient.CODEC.listOf().fieldOf("resources").forGetter(SqueezerRecipe::getInputs),
		// A fluid product nested under "output" (dispatch codec): a plain FluidProduct serializes with no "type" key;
		// addon-provided dynamic products (tag resolution, random amount, chance) declare their own "type".
		IFluidProduct.CODEC.fieldOf("output").forGetter(SqueezerRecipe::getFluidOutput),
		// "remnant" is optional: many squeezer recipes drop no remnant.
		// ItemStack.CODEC rejects EMPTY at serialization, so map EMPTY to absent.
		ItemStack.CODEC.optionalFieldOf("remnant").forGetter(r -> r.remnants.isEmpty() ? java.util.Optional.<ItemStack>empty() : java.util.Optional.of(r.remnants)),
		Codec.FLOAT.fieldOf("chance").forGetter(SqueezerRecipe::getRemnantsChance)
	).apply(instance, (id, time, resources, fluid, remnants, chance) -> new SqueezerRecipe(id, time, resources, fluid, remnants.orElse(ItemStack.EMPTY), chance)));
	private static final StreamCodec<RegistryFriendlyByteBuf, SqueezerRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final Identifier id;
	private final int processingTime;
	private final List<Ingredient> resources;
	private final IFluidProduct fluidOutput;
	private final ItemStack remnants;
	private final float remnantsChance;

	public SqueezerRecipe(Identifier id, int processingTime, List<Ingredient> resources, IFluidProduct fluidOutput, ItemStack remnants, float remnantsChance) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(resources);
		Preconditions.checkArgument(!resources.isEmpty());
		Preconditions.checkNotNull(fluidOutput);
		Preconditions.checkNotNull(remnants);

		this.id = id;
		this.processingTime = processingTime;
		this.resources = resources;
		this.fluidOutput = fluidOutput;
		this.remnants = remnants;
		this.remnantsChance = remnantsChance;
	}

	@Override
	public List<Ingredient> getInputs() {
		return this.resources;
	}

	@Override
	public ItemStack getRemnants() {
		return this.remnants;
	}

	@Override
	public float getRemnantsChance() {
		return this.remnantsChance;
	}

	@Override
	public IFluidProduct getFluidOutput() {
		return this.fluidOutput;
	}

	@Override
	public int getProcessingTime() {
		return this.processingTime;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider lookupProvider) {
		return ItemStack.EMPTY;
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<? extends IForestryRecipe> getSerializer() {
		return FactoryRecipeTypes.SQUEEZER.serializer();
	}

	@Override
	public RecipeType<? extends IForestryRecipe> getType() {
		return FactoryRecipeTypes.SQUEEZER.type();
	}

	public static final RecipeSerializer<SqueezerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private static class Serializer {
		private static SqueezerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			Identifier recipeId = Identifier.STREAM_CODEC.decode(buffer);
			int processingTime = ByteBufCodecs.VAR_INT.decode(buffer);
			List<Ingredient> resources = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
			IFluidProduct fluidOutput = IFluidProduct.STREAM_CODEC.decode(buffer);
			ItemStack remnants = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
			float remnantsChance = ByteBufCodecs.FLOAT.decode(buffer);

			return new SqueezerRecipe(recipeId, processingTime, resources, fluidOutput, remnants, remnantsChance);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, SqueezerRecipe recipe) {
			Identifier.STREAM_CODEC.encode(buffer, recipe.id);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.processingTime);
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.resources);
			IFluidProduct.STREAM_CODEC.encode(buffer, recipe.fluidOutput);
			ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.remnants);
			ByteBufCodecs.FLOAT.encode(buffer, recipe.remnantsChance);
		}
	}
}
