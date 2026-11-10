package forestry.core.platform.block;

import forestry.core.platform.network.packets.PacketTileStream;
import forestry.core.platform.tile.TileForestry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Based on https://github.com/thedarkcolour/ExDeorum/blob/1.21.1/src/main/java/thedarkcolour/exdeorum/network/VisualUpdateTracker.java
public class TileStreamUpdateTracker {
	// guarded by itself: sendVisualUpdate may be reached from code running while a sync is in progress (a chunk loaded
	// by the sync fires ChunkEvent.Load and block entity callbacks) or from another thread
	private static final Map<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> UPDATES = new HashMap<>();

	public static void sendVisualUpdate(TileForestry tile) {
		var level = tile.getLevel();

		if (level != null && !level.isClientSide()) {
			BlockPos pos = tile.getBlockPos().immutable();

			synchronized (UPDATES) {
				UPDATES.computeIfAbsent(level.dimension(), key -> new HashMap<>())
					.computeIfAbsent(ChunkPos.containing(pos), key -> new HashSet<>())
					.add(pos);
			}
		}
	}

	public static void syncVisualUpdates(MinecraftServer server) {
		// take the pending updates out before sending: anything queued while they are sent is kept for the next sync
		// instead of breaking the iteration (ConcurrentModificationException) or being cleared unsent
		Map<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> pending;
		synchronized (UPDATES) {
			if (UPDATES.isEmpty()) {
				return;
			}
			pending = new HashMap<>(UPDATES);
			UPDATES.clear();
		}

		for (var levelUpdates : pending.entrySet()) {
			ServerLevel level = server.getLevel(levelUpdates.getKey());

			// a dimension that no longer exists: drop its updates instead of keeping them forever
			if (level == null) {
				continue;
			}

			for (var entry : levelUpdates.getValue().entrySet()) {
				var chunkPos = entry.getKey();

				// no player can be tracking an unloaded chunk, and getBlockEntity would load (or generate) it
				if (level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z()) == null) {
					continue;
				}

				for (var updatePos : entry.getValue()) {
					if (level.getBlockEntity(updatePos) instanceof TileForestry blockEntity) {
						PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new PacketTileStream(blockEntity));
					}
				}
			}
		}
	}

	/** True if an update for {@code pos} in {@code dimension} is waiting for the next sync. */
	public static boolean isPending(ResourceKey<Level> dimension, BlockPos pos) {
		synchronized (UPDATES) {
			var chunkUpdates = UPDATES.get(dimension);
			if (chunkUpdates == null) {
				return false;
			}
			var positions = chunkUpdates.get(ChunkPos.containing(pos));
			return positions != null && positions.contains(pos);
		}
	}
}
