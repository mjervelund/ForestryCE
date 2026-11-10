package forestry.gametest;

import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

import forestry.core.platform.fluids.FluidTagFilter;
import forestry.core.platform.fluids.ForestryFluids;

/**
 * ForestryCE#32: Forestry's honey/experience/wax fluids were only in the 1.20 {@code forge:} fluid tags, so on 1.21+ other
 * mods' honey (tagged {@code c:honey}, NeoForge's common tag) was rejected by the Analyzer's honey filter and Forestry's own
 * fluids were invisible to other mods. The fluids must be in the {@code c:} tags and the honey filter must accept every
 * {@code c:honey} fluid.
 */
public class CommonFluidTagsTest {
	private static final TagKey<Fluid> C_WAX = FluidTags.create(Identifier.fromNamespaceAndPath("c", "wax"));

	@ForestryGameTest
	public static void forestryFluidsAreInCommonTags(GameTestHelper helper) {
		assertTagged(helper, ForestryFluids.HONEY, Tags.Fluids.HONEY);
		assertTagged(helper, ForestryFluids.EXPERIENCE, Tags.Fluids.EXPERIENCE);
		assertTagged(helper, ForestryFluids.WAX, C_WAX);
		helper.succeed();
	}

	@ForestryGameTest
	public static void honeyFilterAcceptsCommonHoneyTag(GameTestHelper helper) {
		Registry<Fluid> fluids = helper.getLevel().registryAccess().lookupOrThrow(Registries.FLUID);
		HolderSet.Named<Fluid> honey = fluids.get(Tags.Fluids.HONEY).orElse(null);
		helper.assertTrue(honey != null && honey.size() > 0, "c:honey fluid tag is empty");
		Set<Identifier> accepted = FluidTagFilter.HONEY.get();
		for (Holder<Fluid> holder : honey) {
			Identifier id = holder.unwrapKey().orElseThrow().identifier();
			helper.assertTrue(accepted.contains(id), "Honey filter rejects c:honey fluid " + id + " (accepts " + accepted + ")");
		}
		helper.succeed();
	}

	private static void assertTagged(GameTestHelper helper, ForestryFluids fluid, TagKey<Fluid> tag) {
		helper.assertTrue(fluid.getFluid().defaultFluidState().is(tag), fluid.getFluid() + " (source) is not in " + tag);
		helper.assertTrue(fluid.getFlowing().defaultFluidState().is(tag), fluid.getFlowing() + " (flowing) is not in " + tag);
	}
}
