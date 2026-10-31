package forestry.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;

import forestry.arboriculture.features.ArboricultureBlocks;

/**
 * 26.x signs are block models: every Forestry sign block needs a blockstate whose models (and their sprites)
 * exist in the jar and parse. Guards the output of scripts/forestry-signs.py.
 */
public class SignModelsTest {
	@ForestryGameTest
	public static void everySignBlockHasBlockstateAndModels(GameTestHelper helper) {
		List<Block> signs = new ArrayList<>();
		signs.addAll(ArboricultureBlocks.SIGN.getList());
		signs.addAll(ArboricultureBlocks.WALL_SIGN.getList());
		signs.addAll(ArboricultureBlocks.HANGING_SIGN.getList());
		signs.addAll(ArboricultureBlocks.WALL_HANGING_SIGN.getList());
		helper.assertTrue(signs.size() == 4 * 43, "expected 172 sign blocks, got " + signs.size());

		List<String> problems = new ArrayList<>();
		for (Block block : signs) {
			String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
			JsonObject state = json("assets/forestry/blockstates/" + id + ".json", problems);
			if (state == null) {
				continue;
			}
			JsonObject variants = state.getAsJsonObject("variants");
			if (variants == null || variants.isEmpty()) {
				problems.add(id + ": no variants");
				continue;
			}
			// the properties a sign blockstate must cover
			int expected = id.endsWith("wall_sign") || id.endsWith("wall_hanging_sign") ? 4 : id.endsWith("hanging_sign") ? 32 : 16;
			if (variants.size() != expected) {
				problems.add(id + ": " + variants.size() + " variants, expected " + expected);
			}
			for (var entry : variants.entrySet()) {
				String model = entry.getValue().getAsJsonObject().get("model").getAsString();
				if (!model.startsWith("forestry:block/")) {
					problems.add(id + ": model " + model + " is not a forestry block model");
					continue;
				}
				JsonObject modelJson = json("assets/forestry/models/block/" + model.substring("forestry:block/".length()) + ".json", problems);
				if (modelJson == null) {
					continue;
				}
				for (var tex : modelJson.getAsJsonObject("textures").entrySet()) {
					String sprite = tex.getValue().getAsString();
					if (sprite.startsWith("forestry:") && resource("assets/forestry/textures/" + sprite.substring(9) + ".png") == null) {
						problems.add(id + ": missing texture " + sprite);
					}
				}
			}
		}
		helper.assertTrue(problems.isEmpty(), "sign model problems (" + problems.size() + "): " + problems.stream().limit(10).toList());
		helper.succeed();
	}

	private static InputStream resource(String path) {
		return SignModelsTest.class.getClassLoader().getResourceAsStream(path);
	}

	private static JsonObject json(String path, List<String> problems) {
		try (InputStream in = resource(path)) {
			if (in == null) {
				problems.add("missing " + path);
				return null;
			}
			JsonElement parsed = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			return parsed.getAsJsonObject();
		} catch (Exception e) {
			problems.add(path + ": " + e);
			return null;
		}
	}
}
