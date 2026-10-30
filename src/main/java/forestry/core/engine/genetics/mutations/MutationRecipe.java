package forestry.core.engine.genetics.mutations;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import forestry.api.IForestryApi;
import forestry.api.core.genetics.IMutationCondition;
import forestry.api.core.genetics.ISpecies;
import forestry.api.core.genetics.ISpeciesType;
import forestry.api.core.genetics.alleles.Allele;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.api.core.genetics.alleles.IKaryotype;
import forestry.api.core.machines.IForestryRecipe;
import forestry.core.features.GeneticsRecipeTypes;
import forestry.core.engine.genetics.GenomeCodecs;

/**
 * A data-holder recipe representing a single mutation, loaded from a datapack. It is not a crafting recipe;
 * it piggy-backs on Minecraft's recipe sync to load and network mutation data per species type.
 * <p>
 * Each recipe is bound to a species type (via {@link #speciesTypeId}) so that {@link #getType()} and
 * {@link #getSerializer()} resolve to the matching registered {@link RecipeType}/{@link RecipeSerializer}.
 * Use {@link #toMutation} to build the runtime {@link Mutation} once species have been registered.
 */
public class MutationRecipe implements IForestryRecipe {
	private final Identifier speciesTypeId;
	private final Identifier id;
	private final Identifier firstParentId;
	private final Identifier secondParentId;
	private final Identifier resultId;
	private final float chance;
	private final List<IMutationCondition> conditions;
	private final Map<Identifier, Allele<?>> resultAlleles;

	public MutationRecipe(Identifier speciesTypeId, Identifier id, Identifier firstParentId, Identifier secondParentId, Identifier resultId, float chance, List<IMutationCondition> conditions, Map<Identifier, Allele<?>> resultAlleles) {
		this.speciesTypeId = speciesTypeId;
		this.id = id;
		this.firstParentId = firstParentId;
		this.secondParentId = secondParentId;
		this.resultId = resultId;
		this.chance = chance;
		this.conditions = conditions;
		this.resultAlleles = resultAlleles;
	}

	public Identifier getSpeciesTypeId() {
		return this.speciesTypeId;
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	public Identifier getFirstParentId() {
		return this.firstParentId;
	}

	public Identifier getSecondParentId() {
		return this.secondParentId;
	}

	public Identifier getResultId() {
		return this.resultId;
	}

	public float getChance() {
		return this.chance;
	}

	public List<IMutationCondition> getConditions() {
		return this.conditions;
	}

	public Map<Identifier, Allele<?>> getResultAlleles() {
		return this.resultAlleles;
	}

	/**
	 * Builds the runtime {@link Mutation} for this recipe, resolving parent/result species via the given lookup.
	 *
	 * @param type   The species type these mutations belong to.
	 * @param lookup Resolves a species ID to its registered species, returning {@code null} if absent.
	 * @return The runtime mutation, or {@code null} if any parent/result species could not be resolved (caller logs and skips).
	 */
	@Nullable
	public <S extends ISpecies<?>> Mutation<S> toMutation(ISpeciesType<S, ?> type, Function<Identifier, S> lookup) {
		S first = lookup.apply(this.firstParentId);
		S second = lookup.apply(this.secondParentId);
		S result = lookup.apply(this.resultId);
		if (first == null || second == null || result == null) {
			return null; // caller logs + skips
		}
		IKaryotype karyotype = type.getKaryotype();
		Map<IChromosome<?>, Allele<?>> resolved = new IdentityHashMap<>();
		this.resultAlleles.forEach((chromId, allele) -> {
			IChromosome<?> chrom = karyotype.getChromosome(chromId);
			if (chrom != null) {
				resolved.put(chrom, allele);
			}
		});
		return new Mutation<>(type, first, second, result, resolved, this.chance, this.conditions);
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider lookupProvider) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeType<?> getType() {
		return Objects.requireNonNull(GeneticsRecipeTypes.forType(this.speciesTypeId), () -> "No mutation recipe type for species type " + this.speciesTypeId).type();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Objects.requireNonNull(GeneticsRecipeTypes.forType(this.speciesTypeId), () -> "No mutation recipe type for species type " + this.speciesTypeId).serializer();
	}

	/**
	 * A per-species-type serializer. The codecs are resolved lazily on first use because the species type's
	 * karyotype does not exist at {@code RegisterEvent} time (only by datapack-load/recipe-parse time).
	 */
	public static class Serializer implements RecipeSerializer<MutationRecipe> {
		private final Identifier speciesTypeId;
		@Nullable
		private MapCodec<MutationRecipe> codec;
		@Nullable
		private StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> streamCodec;

		public Serializer(Identifier speciesTypeId) {
			this.speciesTypeId = speciesTypeId;
		}

		@Override
		public MapCodec<MutationRecipe> codec() {
			MapCodec<MutationRecipe> codec = this.codec;
			if (codec == null) {
				codec = buildCodec(this.speciesTypeId);
				this.codec = codec;
			}
			return codec;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> streamCodec() {
			StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> streamCodec = this.streamCodec;
			if (streamCodec == null) {
				streamCodec = buildStreamCodec(this.speciesTypeId);
				this.streamCodec = streamCodec;
			}
			return streamCodec;
		}

		private static IKaryotype karyotype(Identifier speciesTypeId) {
			// The mutation recipe types are registered by base, but a species type only exists when the
			// jar that defines it is installed. Reporting that as a parse failure lets RecipeMap log
			// the recipe and carry on; the manager's own lookup throws IllegalStateException, which
			// RecipeMap does not catch and which takes the server down with it
			ISpeciesType<?, ?> speciesType = IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypeSafe(speciesTypeId);
			if (speciesType == null) {
				throw new JsonParseException("No species type is registered with id " + speciesTypeId + ", so its mutations cannot be read");
			}
			return speciesType.getKaryotype();
		}

		private static MapCodec<MutationRecipe> buildCodec(Identifier speciesTypeId) {
			Codec<Map<Identifier, Allele<?>>> resultAllelesCodec = GenomeCodecs.alleleMapCodec(karyotype(speciesTypeId));
			return RecordCodecBuilder.mapCodec(instance -> instance.group(
				Identifier.CODEC.fieldOf("id").forGetter(MutationRecipe::getId),
				Identifier.CODEC.fieldOf("first").forGetter(MutationRecipe::getFirstParentId),
				Identifier.CODEC.fieldOf("second").forGetter(MutationRecipe::getSecondParentId),
				Identifier.CODEC.fieldOf("result").forGetter(MutationRecipe::getResultId),
				Codec.FLOAT.fieldOf("chance").forGetter(MutationRecipe::getChance),
				IMutationCondition.LIST_CODEC.optionalFieldOf("conditions", List.of()).forGetter(MutationRecipe::getConditions),
				resultAllelesCodec.optionalFieldOf("result_alleles", Map.of()).forGetter(MutationRecipe::getResultAlleles)
			).apply(instance, (id, first, second, result, chance, conditions, resultAlleles) ->
				new MutationRecipe(speciesTypeId, id, first, second, result, chance, conditions, resultAlleles)));
		}

		private static StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> buildStreamCodec(Identifier speciesTypeId) {
			IKaryotype karyotype = karyotype(speciesTypeId);
			StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, Allele<?>>> resultAllelesStreamCodec = GenomeCodecs.alleleMapStreamCodec(karyotype);
			return StreamCodec.of(
				(buf, recipe) -> {
					Identifier.STREAM_CODEC.encode(buf, recipe.id);
					Identifier.STREAM_CODEC.encode(buf, recipe.firstParentId);
					Identifier.STREAM_CODEC.encode(buf, recipe.secondParentId);
					Identifier.STREAM_CODEC.encode(buf, recipe.resultId);
					buf.writeFloat(recipe.chance);
					IMutationCondition.LIST_STREAM_CODEC.encode(buf, recipe.conditions);
					resultAllelesStreamCodec.encode(buf, recipe.resultAlleles);
				},
				buf -> {
					Identifier id = Identifier.STREAM_CODEC.decode(buf);
					Identifier first = Identifier.STREAM_CODEC.decode(buf);
					Identifier second = Identifier.STREAM_CODEC.decode(buf);
					Identifier result = Identifier.STREAM_CODEC.decode(buf);
					float chance = buf.readFloat();
					List<IMutationCondition> conditions = IMutationCondition.LIST_STREAM_CODEC.decode(buf);
					Map<Identifier, Allele<?>> resultAlleles = resultAllelesStreamCodec.decode(buf);
					return new MutationRecipe(speciesTypeId, id, first, second, result, chance, conditions, resultAlleles);
				}
			);
		}
	}
}
