package forestry.apiculture.bees.genetics;

import com.google.common.base.Suppliers;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.core.genetics.alleles.Allele;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class HiveDrop implements IHiveDrop {
	private final Identifier speciesId;
	private final double chance;
	// Resolved on first use: 26.x item stacks need bound components, which setup runs before
	private final Supplier<List<ItemStack>> bonus;
	private final double ignobleChance;
	private final Map<IChromosome<?>, Allele<?>> alleles;

	@Nullable
	private IBeeSpecies species;

	public HiveDrop(double chance, Identifier speciesId, Supplier<List<ItemStack>> bonus, float ignobleChance, Map<IChromosome<?>, Allele<?>> alleles) {
		this.speciesId = speciesId;
		this.chance = chance;
		this.bonus = Suppliers.memoize(bonus::get);
		this.ignobleChance = ignobleChance;
		this.alleles = alleles;
	}

	@Override
	public IBee createIndividual(BlockGetter level, BlockPos pos) {
		if (this.species == null) {
			this.species = SpeciesUtil.getBeeSpecies(this.speciesId);
		}
		return this.species.createIndividual(this.alleles);
	}

	@Override
	public List<ItemStack> getExtraItems(BlockGetter level, BlockPos pos, int fortune) {
		ArrayList<ItemStack> result = new ArrayList<>();
		for (ItemStack stack : this.bonus.get()) {
			result.add(stack.copy());
		}

		return result;
	}

	@Override
	public double getChance(BlockGetter level, BlockPos pos, int fortune) {
		return this.chance;
	}

	@Override
	public double getIgnobleChance(BlockGetter level, BlockPos pos, int fortune) {
		return this.ignobleChance;
	}
}
