package forestry.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;

/**
 * Asserts that every api manager is installed by the time a world is running. Farming is the one that
 * can legitimately be a no-op, so it is asked directly; see NoOpManagerTest for that side of the
 * contract. Hives and trees come from base and are installed during species type registration, so the
 * getter throwing here is the initialization-order failure the no-ops used to hide.
 */
public class ManagerLoadedTest {
	@ForestryGameTest
	public static void serverManagersAreLoaded(GameTestHelper helper) {
		if (!IForestryApi.INSTANCE.getFarmingManager().isLoaded()) {
			helper.fail("IFarmingManager reports not loaded in a full install");
			return;
		}
		try {
			IForestryApi.INSTANCE.getHiveManager();
			IForestryApi.INSTANCE.getTreeManager();
		} catch (IllegalStateException e) {
			helper.fail("A base manager was never installed: " + e.getMessage());
			return;
		}
		helper.succeed();
	}
}
