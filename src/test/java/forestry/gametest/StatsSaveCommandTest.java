package forestry.gametest;

import java.io.File;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.permissions.PermissionSet;

import forestry.arboriculture.commands.TreeStatsSaveHelper;
import forestry.core.platform.commands.CommandSaveStats;

/**
 * Security guard: {@code /forestry tree|bee save <player>} had no permission check and wrote
 * {@code config/forestry/stats/<display name>-….log}, so a display name (or offline-mode name) with {@code ../}
 * wrote files outside that folder.
 */
public class StatsSaveCommandTest {
	@ForestryGameTest
	public static void saveNeedsGamemaster(GameTestHelper helper) {
		var node = CommandSaveStats.register(new TreeStatsSaveHelper()).build();
		CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
		helper.assertFalse(node.canUse(source.withPermission(PermissionSet.NO_PERMISSIONS)), "a player without permissions can save stats files");
		helper.assertTrue(node.canUse(source.withPermission(PermissionSet.ALL_PERMISSIONS)), "an operator cannot save stats files");
		helper.succeed();
	}

	@ForestryGameTest
	public static void statsFileStaysInStatsFolder(GameTestHelper helper) {
		File folder = new File("config/forestry/stats").getAbsoluteFile();
		for (String name : new String[]{"../../evil", "..\\evil", "Steve"}) {
			File file = CommandSaveStats.getStatsFile(new GameProfile(UUID.randomUUID(), name), "trees").getAbsoluteFile();
			helper.assertTrue(folder.equals(file.getParentFile()), "the stats file for " + name + " is " + file);
		}
		helper.assertTrue(CommandSaveStats.getStatsFile(new GameProfile(UUID.randomUUID(), "Steve"), "trees").getName().equals("Steve-trees.log"), "a plain name was changed");
		helper.succeed();
	}
}
