package forestry.core.platform.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import forestry.api.ForestryConstants;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

public class CommandHelpers {
	public static final Predicate<CommandSourceStack> ADMIN = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);

	public static void sendLocalizedChatMessage(CommandSourceStack sender, String locTag, Object... args) {
		sender.sendSuccess(() -> Component.translatable(locTag, args), false);
	}

	public static void sendLocalizedChatMessage(CommandSourceStack sender, Style chatStyle, String locTag, Object... args) {
		sender.sendSuccess(() -> Component.translatable(locTag, args).setStyle(chatStyle), false);
	}

	/**
	 * Avoid using this function if at all possible. Commands are processed on the server,
	 * which has no localization information.
	 * <p>
	 * StringUtil.localize() is NOT a valid alternative for sendLocalizedChatMessage().
	 * Messages will not be localized properly if you use StringUtil.localize().
	 */
	public static void sendChatMessage(CommandSourceStack sender, Component message) {
		sender.sendSuccess(() -> message, false);
	}

	/**
	 * Reads an id like {@link Identifier#read}, but an id typed without a namespace resolves in {@code forestry:} instead
	 * of {@code minecraft:} (ForestryCE#392): every species/chromosome the /forestry commands accept is Forestry's.
	 * An explicit namespace is kept.
	 */
	public static Identifier readForestryId(StringReader reader) throws CommandSyntaxException {
		int start = reader.getCursor();
		Identifier id = Identifier.read(reader);
		if (reader.getString().substring(start, reader.getCursor()).indexOf(':') < 0) {
			return ForestryConstants.forestry(id.getPath());
		}
		return id;
	}
}
