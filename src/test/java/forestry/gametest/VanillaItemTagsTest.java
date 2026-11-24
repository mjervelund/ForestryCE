package forestry.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import forestry.api.ForestryConstants;
import forestry.core.features.CoreItems;

/**
 * ForestryCE#315: Forestry's wooden buttons and trapdoors were only in the block tags, so the item tags
 * (minecraft:wooden_buttons/buttons, wooden_trapdoors/trapdoors) recipes and other mods use missed them; the
 * Survivalist's axe, sword and hoe were in no tool tag (pickaxe and shovel were).
 */
public class VanillaItemTagsTest {
	@ForestryGameTest
	public static void buttonsTrapdoorsAndToolsAreTagged(GameTestHelper helper) {
		check(helper, item("larch_button"), ItemTags.WOODEN_BUTTONS);
		check(helper, item("larch_button"), tag("buttons"));
		check(helper, item("larch_trapdoor"), ItemTags.WOODEN_TRAPDOORS);
		check(helper, item("larch_trapdoor"), tag("trapdoors"));
		check(helper, CoreItems.SURVIVALISTS_AXE.item(), ItemTags.AXES);
		check(helper, CoreItems.SURVIVALISTS_SWORD.item(), ItemTags.SWORDS);
		check(helper, CoreItems.SURVIVALISTS_HOE.item(), ItemTags.HOES);
		// still there (the vanilla tags would drop every entry if one of ours failed to resolve)
		check(helper, CoreItems.SURVIVALISTS_PICKAXE.item(), ItemTags.PICKAXES);
		helper.succeed();
	}

	// no ItemTags constant for these in 26.3
	private static TagKey<Item> tag(String path) {
		return TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace(path));
	}

	private static Item item(String path) {
		return BuiltInRegistries.ITEM.getValue(ForestryConstants.forestry(path));
	}

	private static void check(GameTestHelper helper, Item item, TagKey<Item> tag) {
		helper.assertTrue(new ItemStack(item).is(tag), BuiltInRegistries.ITEM.getKey(item) + " is not in #" + tag.location());
	}
}
