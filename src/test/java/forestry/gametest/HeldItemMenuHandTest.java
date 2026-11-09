package forestry.gametest;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayer;

import forestry.apiculture.features.ApicultureItems;
import forestry.core.content.analyzer.PortableAnalyzerMenu;
import forestry.core.content.backpacks.features.BackpackItems;
import forestry.core.content.backpacks.gui.ContainerNaturalistBackpack;
import forestry.core.engine.circuits.ContainerSolderingIron;
import forestry.core.features.CoreItems;
import forestry.core.platform.gui.ContainerItemInventory;

/**
 * Held-item menus (portable analyzer, soldering iron, naturalist backpacks) build their client-side menu from the hand the
 * server writes into the open-screen extra data. That hand used to come from {@code getUsedItemHand()}, the hand of the
 * last <em>use-duration</em> item (food, shield, bee smoker), not the hand that was right-clicked; the naturalist backpack
 * always used the selected hotbar slot. The client then bound the menu to the other hand's stack, or, when that hand was
 * empty, threw "Parent cannot be empty" and NeoForge disconnected the player
 * (https://github.com/thedarkcolour/ForestryCE/issues/260). These tests run the server's extra-data writer and the
 * client's menu factory against the same player and check the menu is bound to the item that was used.
 */
public class HeldItemMenuHandTest {
	@ForestryGameTest
	public static void analyzerInOffHandWithEmptyMainHand(GameTestHelper helper) {
		CapturingPlayer player = new CapturingPlayer(helper.getLevel());
		ItemStack analyzer = CoreItems.PORTABLE_ANALYZER.stack();
		player.setItemInHand(InteractionHand.OFF_HAND, analyzer);

		assertClientMenuBoundTo(helper, player, InteractionHand.OFF_HAND, buf -> PortableAnalyzerMenu.fromNetwork(1, player.getInventory(), buf));
		helper.succeed();
	}

	@ForestryGameTest
	public static void analyzerInOffHandWithBlockInMainHand(GameTestHelper helper) {
		CapturingPlayer player = new CapturingPlayer(helper.getLevel());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT, 16));
		player.setItemInHand(InteractionHand.OFF_HAND, CoreItems.PORTABLE_ANALYZER.stack());

		assertClientMenuBoundTo(helper, player, InteractionHand.OFF_HAND, buf -> PortableAnalyzerMenu.fromNetwork(1, player.getInventory(), buf));
		helper.succeed();
	}

	/** The issue's reproduction: after using the bee smoker in the off hand, the main-hand analyzer opens on the smoker. */
	@ForestryGameTest
	public static void analyzerInMainHandAfterOffHandSmoker(GameTestHelper helper) {
		CapturingPlayer player = new CapturingPlayer(helper.getLevel());
		player.setItemInHand(InteractionHand.OFF_HAND, ApicultureItems.SMOKER.stack());
		player.startUsingItem(InteractionHand.OFF_HAND);
		helper.assertTrue(player.isUsingItem(), "the smoker did not start being used, so this test covers nothing");
		player.stopUsingItem();
		player.setItemInHand(InteractionHand.MAIN_HAND, CoreItems.PORTABLE_ANALYZER.stack());

		assertClientMenuBoundTo(helper, player, InteractionHand.MAIN_HAND, buf -> PortableAnalyzerMenu.fromNetwork(1, player.getInventory(), buf));
		helper.succeed();
	}

	@ForestryGameTest
	public static void solderingIronInOffHand(GameTestHelper helper) {
		CapturingPlayer player = new CapturingPlayer(helper.getLevel());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT, 16));
		player.setItemInHand(InteractionHand.OFF_HAND, CoreItems.SOLDERING_IRON.stack());

		assertClientMenuBoundTo(helper, player, InteractionHand.OFF_HAND, buf -> ContainerSolderingIron.fromNetwork(1, player.getInventory(), buf));
		helper.succeed();
	}

	@ForestryGameTest
	public static void naturalistBackpackInOffHand(GameTestHelper helper) {
		CapturingPlayer player = new CapturingPlayer(helper.getLevel());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT, 16));
		player.setItemInHand(InteractionHand.OFF_HAND, BackpackItems.APIARIST_BACKPACK.stack());

		assertClientMenuBoundTo(helper, player, InteractionHand.OFF_HAND, buf -> ContainerNaturalistBackpack.fromNetwork(1, player.getInventory(), buf));
		helper.succeed();
	}

	private static void assertClientMenuBoundTo(GameTestHelper helper, CapturingPlayer player, InteractionHand hand,
			Function<RegistryFriendlyByteBuf, ? extends ContainerItemInventory<?>> clientFactory) {
		ItemStack used = player.getItemInHand(hand);
		String name = used.getItem().toString();
		used.getItem().use(helper.getLevel(), player, hand);
		helper.assertTrue(player.extraData != null, name + " did not open a menu");

		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		ContainerItemInventory<?> menu;
		try {
			player.extraData.accept(buf);
			menu = clientFactory.apply(buf);
		} catch (RuntimeException e) {
			// on a real client NeoForge disconnects the player with this exception
			throw helper.assertionException(Component.literal(name + " in " + hand + ": the client menu factory threw " + e));
		} finally {
			buf.release();
		}
		helper.assertTrue(menu.getItemInventory().isParentItemInventory(player.getItemInHand(hand)),
				name + " in " + hand + ": the client menu is bound to " + otherHandName(player, hand) + " instead of the used item");
	}

	private static String otherHandName(CapturingPlayer player, InteractionHand hand) {
		InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		return "the " + other + " stack (" + player.getItemInHand(other) + ")";
	}

	/** Records the extra-data writer of the last opened menu instead of sending it to a client. */
	private static class CapturingPlayer extends FakePlayer {
		@Nullable
		Consumer<RegistryFriendlyByteBuf> extraData;

		CapturingPlayer(ServerLevel level) {
			super(level, new GameProfile(UUID.randomUUID(), "forestry_hand_test"));
		}

		@Override
		public OptionalInt openMenu(@Nullable MenuProvider menuProvider, @Nullable Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
			this.extraData = extraDataWriter;
			return OptionalInt.of(1);
		}
	}
}
