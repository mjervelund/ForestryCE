package forestry.gametest;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.gametest.framework.GameTestHelper;

import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.core.genetics.ISpecies;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.core.platform.commands.ChromosomeArgument;
import forestry.core.platform.commands.SpeciesArgument;
import forestry.core.platform.util.SpeciesUtil;

/**
 * ForestryCE#392: /forestry arguments parsed ids with vanilla's minecraft: default namespace, so "meadows" or "lifespan"
 * were rejected and every id had to be typed as forestry:... . Unqualified ids now resolve in forestry:, explicit
 * namespaces are kept.
 */
public class CommandDefaultNamespaceTest {
	@ForestryGameTest
	public static void speciesArgumentDefaultsToForestry(GameTestHelper helper) throws CommandSyntaxException {
		SpeciesArgument argument = new SpeciesArgument(SpeciesUtil.BEE_TYPE.get());

		ISpecies<?> bare = parse(helper, argument::parse, "meadows");
		helper.assertTrue(bare != null && ForestryBeeSpecies.MEADOWS.equals(bare.id()), "'meadows' parsed to " + (bare == null ? null : bare.id()));

		ISpecies<?> qualified = argument.parse(new StringReader("forestry:meadows rest"));
		helper.assertTrue(ForestryBeeSpecies.MEADOWS.equals(qualified.id()), "'forestry:meadows' parsed to " + qualified.id());

		boolean rejected;
		try {
			argument.parse(new StringReader("minecraft:meadows"));
			rejected = false;
		} catch (CommandSyntaxException e) {
			rejected = true;
		}
		helper.assertTrue(rejected, "explicit 'minecraft:meadows' must not resolve to a Forestry species");
		helper.succeed();
	}

	@ForestryGameTest
	public static void chromosomeArgumentDefaultsToForestry(GameTestHelper helper) throws CommandSyntaxException {
		ChromosomeArgument argument = new ChromosomeArgument(SpeciesUtil.BEE_TYPE.get());

		IChromosome<?> bare = parse(helper, argument::parse, "lifespan");
		helper.assertTrue(bare == BeeChromosomes.LIFESPAN, "'lifespan' parsed to " + (bare == null ? null : bare.id()));

		IChromosome<?> qualified = argument.parse(new StringReader("forestry:lifespan"));
		helper.assertTrue(qualified == BeeChromosomes.LIFESPAN, "'forestry:lifespan' parsed to " + qualified.id());

		// an unknown chromosome must be a normal command syntax error, not an internal error from passing an
		// Identifier as a translation argument
		boolean rejected;
		try {
			argument.parse(new StringReader("no_such_chromosome"));
			rejected = false;
		} catch (CommandSyntaxException e) {
			rejected = e.getMessage().contains("forestry:no_such_chromosome");
		}
		helper.assertTrue(rejected, "unknown chromosome was not rejected with a syntax error naming forestry:no_such_chromosome");
		helper.succeed();
	}

	private interface Parser<T> {
		T parse(StringReader reader) throws CommandSyntaxException;
	}

	private static <T> T parse(GameTestHelper helper, Parser<T> parser, String input) {
		StringReader reader = new StringReader(input + " next");
		try {
			T value = parser.parse(reader);
			helper.assertTrue(reader.getRemaining().equals(" next"), "parser consumed '" + input + "' wrongly, remaining '" + reader.getRemaining() + "'");
			return value;
		} catch (CommandSyntaxException e) {
			helper.fail("'" + input + "' was rejected: " + e.getMessage());
			return null;
		}
	}
}
