package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.NeoForge;

import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.core.ForestryEvent;
import forestry.api.core.TemperatureType;
import forestry.apiculture.apiary.ApicultureBlockType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.core.platform.util.SpeciesUtil;

// https://github.com/thedarkcolour/ForestryCE/issues/316: an embittered princess mated outside the nether becomes
// zombified, but she must stay the same bee otherwise (ignoble stays ignoble, generation and mate are kept)
public final class ZombifiedPrincessMateTest {
	private static final BlockPos HOUSING = new BlockPos(2, 1, 2);

	private ZombifiedPrincessMateTest() {
	}

	@ForestryGameTest
	public static void zombifiedPrincessKeepsMatePristineAndGeneration(GameTestHelper helper) {
		helper.setBlock(HOUSING, ApicultureBlocks.BASE.get(ApicultureBlockType.APIARY).defaultState());
		if (!(helper.getLevel().getBlockEntity(helper.absolutePos(HOUSING)) instanceof IBeeHousing housing)) {
			helper.fail("Expected an IBeeHousing at " + HOUSING);
			return;
		}
		helper.assertTrue(housing.temperature() != TemperatureType.HELLISH, "the test biome is hellish, so this test covers nothing");

		IBee drone = SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.FOREST).createIndividual();
		IBee princess = SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.EMBITTERED).createIndividual();
		princess.setPristine(false);
		// an ignoble princess several generations down the line, as the beekeeping logic produces them
		for (int i = 0; i < 5; i++) {
			princess.setMate(princess.getGenome());
			princess = princess.spawnPrincess(housing);
			helper.assertTrue(princess != null, "the embittered queen produced no princess");
		}
		int generation = princess.getGeneration();
		helper.assertTrue(generation > 0, "the bred princess has generation 0, so this test covers nothing");

		// as BeeHousingBeekeepingLogic does it: mate first, then post the event and keep its princess
		princess.setMate(drone.getGenome());
		ForestryEvent.BeeMatingEvent event = new ForestryEvent.BeeMatingEvent(housing, princess, drone);
		NeoForge.EVENT_BUS.post(event);
		IBee queen = event.getPrincess();

		helper.assertTrue(queen.getSpecies().id().equals(ForestryBeeSpecies.ZOMBIFIED), "the embittered princess was not zombified: " + queen.getSpecies().id());
		helper.assertFalse(queen.isPristine(), "the ignoble princess became pristine when she was zombified");
		helper.assertTrue(queen.getGeneration() == generation, "the zombified princess's generation is " + queen.getGeneration() + ", expected " + generation);
		helper.assertTrue(queen.getMate() != null, "the zombified princess lost her mate");
		helper.assertTrue(queen.getMate().isSameAlleles(drone.getGenome()), "the zombified princess's mate is not the drone");
		helper.succeed();
	}
}
