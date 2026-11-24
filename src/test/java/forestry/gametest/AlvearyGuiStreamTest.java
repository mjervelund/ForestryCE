package forestry.gametest;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import forestry.core.platform.network.packets.PacketGuiStream;

/**
 * ForestryCE#442 "the alveary unforms and if you click on it you get kicked for corrupt packets". While a player has
 * the alveary GUI open, {@code AlvearyMenu.broadcastChanges} streams the server controller's GUI data. An unformed
 * alveary's controller is a {@code FakeAlvearyController}, which writes nothing; the client, whose copy of the alveary
 * is still formed (block updates arrive later), let its real {@code AlvearyController.readGuiData} read a varint, the
 * climate and two bytes from that empty payload: IndexOutOfBoundsException in the packet handler, and the client is
 * disconnected. An empty GUI stream must be ignored.
 */
public class AlvearyGuiStreamTest {
	private static final BlockPos BASE = new BlockPos(6, 1, 6);
	private static final int TIMEOUT = 2_000_000;

	@ForestryGameTest(timeoutTicks = TIMEOUT)
	public static void emptyGuiStreamIsIgnored(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ChunkPos site = ChunkLifecycle.isolatedSite(helper);
		BlockPos corner = ChunkLifecycle.siteBlock(helper, site, 0, 0).below();
		List<BlockPos> members = new ArrayList<>();

		ChunkLifecycle.steps(helper)
			.run(() -> ChunkLifecycle.hold(level, site, 1))
			.await(() -> ChunkLifecycle.isTicking(level, site), "site did not start ticking")
			.run(() -> {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						helper.setBlock(corner.offset(x, 0, z), Blocks.STONE);
					}
				}
				members.addAll(MultiblockTestSupport.buildAlveary(helper, corner.offset(BASE)));
			})
			.await(() -> MultiblockTestSupport.isAssembled(level, members.get(0)), "Expected an assembled alveary")
			.run(() -> {
				// what the client receives from a server whose alveary has just unformed: an empty payload
				RegistryFriendlyByteBuf empty = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
				PacketGuiStream packet = new PacketGuiStream(members.get(0), null, empty);
				RuntimeException thrown = null;
				try {
					PacketGuiStream.handle(packet, FakePlayerFactory.getMinecraft(level));
				} catch (RuntimeException e) {
					thrown = e;
				} finally {
					ChunkLifecycle.release(level, site, 1);
				}
				helper.assertTrue(thrown == null, "an empty GUI stream threw " + thrown + " (the client is disconnected)");
			})
			.succeed();
	}
}
