package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import forestry.core.content.energy.blocks.EngineBlockType;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.tiles.ClockworkEngineBlockEntity;
import forestry.core.platform.block.TileStreamUpdateTracker;
import forestry.core.platform.tile.TileForestry;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

// https://github.com/thedarkcolour/ForestryCE/issues/264: ConcurrentModificationException in
// TileStreamUpdateTracker.syncVisualUpdates. The sync called level.getBlockEntity for every queued position, which
// loads (or generates) the chunk when it is not loaded. Loading a chunk on the server thread runs ChunkEvent.Load and
// block entity callbacks in the middle of the sync; anything there that queues a visual update mutated the map being
// iterated (CME), or, if it was the last entry, the update was cleared unsent
public class TileStreamUpdateTrackerTest {
	private static final BlockPos ENGINE_POS = new BlockPos(1, 2, 1);

	// armed by the test for one sync; registered once
	@Nullable
	private static volatile Reentry reentry;
	private static final AtomicBoolean LISTENER_REGISTERED = new AtomicBoolean();

	private record Reentry(ServerLevel level, ChunkPos chunk, TileForestry requeue, AtomicBoolean fired) {
	}

	private static void onChunkLoad(ChunkEvent.Load event) {
		Reentry r = reentry;
		if (r != null && event.getLevel() == r.level() && event.getChunk().getPos().equals(r.chunk())) {
			r.fired().set(true);
			// a chunk-load callback queueing a visual update while the sync is running
			TileStreamUpdateTracker.sendVisualUpdate(r.requeue());
		}
	}

	@ForestryGameTest
	public static void syncSurvivesUpdatesQueuedWhileSyncing(GameTestHelper helper) {
		if (LISTENER_REGISTERED.compareAndSet(false, true)) {
			NeoForge.EVENT_BUS.addListener(TileStreamUpdateTrackerTest::onChunkLoad);
		}
		ServerLevel level = helper.getLevel();

		helper.setBlock(ENGINE_POS, EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK).defaultState());
		if (!(level.getBlockEntity(helper.absolutePos(ENGINE_POS)) instanceof ClockworkEngineBlockEntity engine)) {
			helper.fail("Expected a ClockworkEngineBlockEntity at " + ENGINE_POS);
			return;
		}

		// an update queued for a block entity whose chunk is not loaded (it was unloaded after queueing)
		ChunkPos farChunk = ChunkLifecycle.isolatedSite(helper);
		helper.assertFalse(ChunkLifecycle.isFull(level, farChunk), "the far chunk is already loaded, so this test covers nothing");
		BlockPos farPos = new BlockPos(farChunk.getMinBlockX(), helper.absolutePos(ENGINE_POS).getY(), farChunk.getMinBlockZ());
		ClockworkEngineBlockEntity farEngine = new ClockworkEngineBlockEntity(farPos, EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK).defaultState());
		farEngine.setLevel(level);
		TileStreamUpdateTracker.sendVisualUpdate(farEngine);

		AtomicBoolean fired = new AtomicBoolean();
		reentry = new Reentry(level, farChunk, engine, fired);
		try {
			TileStreamUpdateTracker.syncVisualUpdates(level.getServer());
		} catch (RuntimeException e) {
			helper.fail("syncVisualUpdates threw " + e);
			return;
		} finally {
			reentry = null;
		}

		if (fired.get()) {
			helper.assertTrue(TileStreamUpdateTracker.isPending(level.dimension(), engine.getBlockPos()), "the update queued during the sync was lost");
		}
		helper.assertFalse(ChunkLifecycle.isFull(level, farChunk), "syncVisualUpdates loaded the unloaded chunk " + farChunk);
		helper.assertFalse(TileStreamUpdateTracker.isPending(level.dimension(), farPos), "the update in the unloaded chunk is still queued");

		// an update queued after a sync is kept for the next one, which sends and clears it
		TileStreamUpdateTracker.sendVisualUpdate(engine);
		helper.assertTrue(TileStreamUpdateTracker.isPending(level.dimension(), engine.getBlockPos()), "queued update missing");
		TileStreamUpdateTracker.syncVisualUpdates(level.getServer());
		helper.assertFalse(TileStreamUpdateTracker.isPending(level.dimension(), engine.getBlockPos()), "the next sync did not take the queued update");
		helper.succeed();
	}
}
