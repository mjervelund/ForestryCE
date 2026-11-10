package forestry.gametest;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import forestry.api.ForestryTags;
import forestry.core.content.backpacks.BackpackFilter;

// https://github.com/thedarkcolour/ForestryCE/issues/454
public class BackpackFilterTest {
	private static final TagKey<Item> MISSING = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forestry", "gametest/missing_backpack_tag"));

	// A tag that failed to load (one bad entry drops the whole tag) is not bound; the filter must treat it as empty
	// instead of throwing on every container click.
	@ForestryGameTest
	public static void unboundTagsDoNotThrow(GameTestHelper helper) {
		BackpackFilter missingAccept = new BackpackFilter(MISSING, ForestryTags.Items.MINER_REJECT);
		helper.assertFalse(missingAccept.test(new ItemStack(Items.AMETHYST_SHARD)), "missing accept tag accepted an item");

		BackpackFilter missingReject = new BackpackFilter(ForestryTags.Items.MINER_ALLOW, MISSING);
		ItemStack allowed = new ItemStack(Items.COAL);
		helper.assertTrue(allowed.is(ForestryTags.Items.MINER_ALLOW), "coal is no longer in the miner allow tag, pick another item");
		helper.assertTrue(missingReject.test(allowed), "missing reject tag rejected an allowed item");
		helper.succeed();
	}

	@ForestryGameTest
	public static void minerAcceptsAllowedAndRejectsOthers(GameTestHelper helper) {
		BackpackFilter miner = new BackpackFilter(ForestryTags.Items.MINER_ALLOW, ForestryTags.Items.MINER_REJECT);
		helper.assertTrue(miner.test(new ItemStack(Items.COAL)), "miner backpack refused coal");
		helper.assertFalse(miner.test(new ItemStack(Items.OAK_SAPLING)), "miner backpack accepted a sapling");
		helper.succeed();
	}
}
