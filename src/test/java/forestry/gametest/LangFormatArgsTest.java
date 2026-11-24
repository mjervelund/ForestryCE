package forestry.gametest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

/**
 * Minecraft's translations only understand {@code %s}, {@code %1$s} and {@code %%}: any other format specifier makes
 * {@code TranslatableContents} give up and show the raw string, so the frame tooltips read "Production: %.2fx"
 * (ForestryCE#303), multiblock errors "at least %dx%dx%d in size", the mailbox toast "%1$d unopened letters" and
 * /forestry butterfly kill "Killed %d butterflies". Checks every Forestry lang file for Java-only specifiers.
 */
public class LangFormatArgsTest {
	private static final List<String> LANGUAGES = List.of("en_us", "de_de", "et_ee", "fr_fr", "ja_jp", "pl_pl", "pt_br", "ru_ru", "uk_ua", "zh_cn", "zh_tw");
	// a Java format specifier; "%SENDER"-style placeholders (replaced by hand) and a lone "50%" are not
	private static final Pattern SPECIFIER = Pattern.compile("%(\\d+\\$)?[-#+0,(]*\\d*(\\.\\d+)?([a-zA-Z%])([A-Z]*)");

	@ForestryGameTest
	public static void langUsesOnlyMinecraftFormatSpecifiers(GameTestHelper helper) {
		Set<String> bad = new TreeSet<>();
		int files = 0;
		try {
			for (String language : LANGUAGES) {
				Enumeration<URL> sources = LangFormatArgsTest.class.getClassLoader().getResources("assets/forestry/lang/" + language + ".json");
				while (sources.hasMoreElements()) {
					files++;
					try (InputStream in = sources.nextElement().openStream()) {
						// JsonParser reads leniently: pl_pl holds raw control characters, which the game's lang loader accepts
						JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
						for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
							if (entry.getValue().isJsonPrimitive() && !isSupported(entry.getValue().getAsString())) {
								bad.add(language + " " + entry.getKey() + " = " + entry.getValue().getAsString());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			helper.fail("Failed to read the lang files: " + e);
			return;
		}
		helper.assertTrue(files >= LANGUAGES.size(), "only " + files + " Forestry lang files on the classpath");
		helper.assertTrue(bad.isEmpty(), bad.size() + " translation(s) use format specifiers Minecraft does not support:\n  " + String.join("\n  ", bad));

		// and the frame tooltip formats its value itself
		String production = Component.translatable("item.forestry.bee.modifier.production", "2.00").getString();
		helper.assertTrue(!production.contains("%"), "frame tooltip renders as " + production);
		helper.succeed();
	}

	private static boolean isSupported(String value) {
		Matcher matcher = SPECIFIER.matcher(value);
		while (matcher.find()) {
			String conversion = matcher.group(3);
			if (!matcher.group(4).isEmpty() && !conversion.equals("s")) {
				continue; // %SENDER, %TYPE, ...
			}
			// nothing between the % (and its optional n$) and the conversion: no flags, width or precision
			boolean plain = matcher.end(3) - matcher.start() == (matcher.group(1) == null ? 2 : 2 + matcher.group(1).length());
			if (!plain || !(conversion.equals("s") || conversion.equals("%"))) {
				return false;
			}
		}
		return true;
	}
}
