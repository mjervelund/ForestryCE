package forestry.core.content.machines.recipes;

import forestry.core.platform.util.EmptyIngredient;
import forestry.api.core.machines.IForestryRecipe;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.core.machines.IFermenterRecipe;
import forestry.core.content.machines.features.FactoryRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class FermenterRecipe implements IFermenterRecipe {
	private static final MapCodec<FermenterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Identifier.CODEC.fieldOf("id").forGetter(FermenterRecipe::getId),
		Ingredient.CODEC.fieldOf("resource").forGetter(FermenterRecipe::getInputItem),
		Codec.INT.fieldOf("fermentationValue").forGetter(FermenterRecipe::getFermentationValue),
		Codec.FLOAT.fieldOf("modifier").forGetter(FermenterRecipe::getModifier),
		BuiltInRegistries.FLUID.byNameCodec().fieldOf("output").forGetter(FermenterRecipe::getOutput),
		FluidStackTemplate.CODEC.fieldOf("fluidResource").forGetter(r -> r.fluidResource)
	).apply(instance, (id, resource, value, mod, out, fluidRes) -> new FermenterRecipe(id, resource, value, mod, out, fluidRes)));
	private static final StreamCodec<RegistryFriendlyByteBuf, FermenterRecipe> STREAM_CODEC = StreamCodec.of(
		Serializer::toNetwork,
		Serializer::fromNetwork
	);

	private final Identifier id;
	private final Ingredient resource;
	private final int fermentationValue;
	private final float modifier;
	private final Fluid output;
	private final FluidStackTemplate fluidResource;

	// Decoded at datapack load, before fluid components are bound, so the fluid is kept as a template
	public FermenterRecipe(Identifier id, Ingredient resource, int fermentationValue, float modifier, Fluid output, FluidStackTemplate fluidResource) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(resource, "Fermenter Resource cannot be null!");
		// Not resource.isEmpty(): that reads tag contents, which are unbound while recipes decode
		Preconditions.checkArgument(resource != EmptyIngredient.EMPTY, "Fermenter Resource item cannot be empty!");
		Preconditions.checkNotNull(output, "Fermenter Output cannot be null!");
		Preconditions.checkNotNull(fluidResource, "Fermenter Liquid cannot be null!");

		this.id = id;
		this.resource = resource;
		this.fermentationValue = fermentationValue;
		this.modifier = modifier;
		this.output = output;
		this.fluidResource = fluidResource;
	}

	public FermenterRecipe(Identifier id, int fermentationValue, float modifier, Fluid output, FluidStackTemplate fluidResource) {
		Preconditions.checkNotNull(id, "Recipe identifier cannot be null");
		Preconditions.checkNotNull(output, "Fermenter output cannot be null!");
		Preconditions.checkNotNull(fluidResource, "Fermenter liquid cannot be null!");

		this.id = id;
		this.resource = EmptyIngredient.EMPTY;
		this.fermentationValue = fermentationValue;
		this.modifier = modifier;
		this.output = output;
		this.fluidResource = fluidResource;
	}

	@Override
	public Ingredient getInputItem() {
		return this.resource;
	}

	@Override
	public FluidStack getInputFluid() {
		return this.fluidResource.create();
	}

	@Override
	public int getFermentationValue() {
		return this.fermentationValue;
	}

	@Override
	public float getModifier() {
		return this.modifier;
	}

	@Override
	public Fluid getOutput() {
		return this.output;
	}

	@Override
	public boolean matches(ItemStack inputItem, FluidStack inputFluid) {
		return this.resource.test(inputItem) && FluidStack.isSameFluidSameComponents(inputFluid, this.fluidResource);
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
		return FactoryRecipeTypes.FERMENTER.serializer();
	}

	@Override
	public RecipeType<? extends IForestryRecipe> getType() {
		return FactoryRecipeTypes.FERMENTER.type();
	}

	public static final RecipeSerializer<FermenterRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private static class Serializer {
		private static FermenterRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			Identifier recipeId = Identifier.STREAM_CODEC.decode(buffer);
			Ingredient resource = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			int fermentationValue = ByteBufCodecs.VAR_INT.decode(buffer);
			float modifier = ByteBufCodecs.FLOAT.decode(buffer);
			Fluid output = ByteBufCodecs.registry(Registries.FLUID).decode(buffer);
			FluidStackTemplate fluidResource = FluidStackTemplate.STREAM_CODEC.decode(buffer);

			return new FermenterRecipe(recipeId, resource, fermentationValue, modifier, output, fluidResource);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, FermenterRecipe recipe) {
			Identifier.STREAM_CODEC.encode(buffer, recipe.id);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.resource);
			ByteBufCodecs.VAR_INT.encode(buffer, recipe.fermentationValue);
			ByteBufCodecs.FLOAT.encode(buffer, recipe.modifier);
			ByteBufCodecs.registry(Registries.FLUID).encode(buffer, recipe.output);
			FluidStackTemplate.STREAM_CODEC.encode(buffer, recipe.fluidResource);
		}
	}
}
