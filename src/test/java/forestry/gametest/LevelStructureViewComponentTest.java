package forestry.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.ForestryConstants;
import forestry.apiculture.alveary.AlvearyBlock;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.core.platform.multiblock.LevelStructureView;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.features.MultifarmBlocks;

/**
 * Covers component recognition in {@link LevelStructureView}. A member that fills no reserved role
 * ({@code *_plain} or {@code farm_gearbox}) must still be recognised as part of its machine and let the
 * structure form.
 *
 * <p>This is the addon-extensibility path. Recognition uses the public {@code IAlvearyComponent} or
 * {@code IFarmComponent} interface plus the engine's {@code MultiblockTileEntityForestry} base. That is
 * the same rule for Forestry's own non-role parts (sieve, hatch) and for a third-party one, so placing a
 * sieve or hatch in an exterior cell exercises the code an addon part runs through, without a test-only
 * block entity type.
 */
public class LevelStructureViewComponentTest {
	private static final BlockPos BASE = new BlockPos(6, 1, 6);
	private static final int TIMEOUT = 240;

	/** Checks that an alveary assembles with a non-role member (a sieve) in an exterior cell. */
	@ForestryGameTest(timeoutTicks = TIMEOUT)
	public static void alvearyFormsWithNonRoleComponent(GameTestHelper helper) {
		List<BlockPos> members = MultiblockTestSupport.buildAlveary(helper, BASE);
		BlockState sieve = ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.SIEVE).defaultState();
		// exterior (x == 0), and not the plain-only top layer
		helper.setBlock(BASE.offset(0, 1, 1), sieve);

		ServerLevel level = helper.getLevel();
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, members.get(0)),
				"an alveary containing a non-role component (sieve) must still assemble");
		helper.succeed();
	}

	/** Checks that a farm assembles with a non-role member (a hatch) in an exterior cell. */
	@ForestryGameTest(timeoutTicks = TIMEOUT)
	public static void farmFormsWithNonRoleComponent(GameTestHelper helper) {
		List<BlockPos> members = MultiblockTestSupport.buildFarm(helper, BASE);
		BlockState hatch = MultifarmBlocks.FARM.get(MultifarmBlockType.HATCH, MultifarmMaterialType.STONE_BRICK).defaultState();
		// exterior, and not the level-2 plain-only band
		helper.setBlock(BASE.offset(0, 1, 1), hatch);

		ServerLevel level = helper.getLevel();
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, members.get(0)),
				"a farm containing a non-role component (hatch) must still assemble");
		helper.succeed();
	}

	/** Checks that a non-component block entity in an interior cell is not adopted as a part. */
	@ForestryGameTest(timeoutTicks = TIMEOUT)
	public static void nonComponentBlockEntityIsNotAPart(GameTestHelper helper) {
		List<BlockPos> members = MultiblockTestSupport.buildAlveary(helper, BASE);
		helper.setBlock(BASE.offset(1, 1, 1), Blocks.CHEST);

		ServerLevel level = helper.getLevel();
		helper.assertFalse(MultiblockTestSupport.isAssembled(level, members.get(0)),
				"a chest in the interior must not be recognised as an alveary component");
		helper.succeed();
	}

	/**
	 * Regression: a Mekanism transporter kept a capability handler built while the farm was unformed (hatch size 0),
	 * then indexed it after the farm formed (and the other way round): IndexOutOfBoundsException while ticking.
	 */
	@ForestryGameTest(timeoutTicks = TIMEOUT)
	public static void farmHatchHandlerSurvivesFormStateChanges(GameTestHelper helper) {
		List<BlockPos> members = MultiblockTestSupport.buildFarm(helper, BASE);
		BlockPos hatchRel = BASE.offset(0, 1, 1);
		BlockPos cornerRel = BASE.offset(2, 3, 2);
		helper.setBlock(hatchRel, MultifarmBlocks.FARM.get(MultifarmBlockType.HATCH, MultifarmMaterialType.STONE_BRICK).defaultState());
		ServerLevel level = helper.getLevel();
		BlockPos hatchAbs = helper.absolutePos(hatchRel);
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, members.get(0)), "farm must assemble");

		ResourceHandler<ItemResource> formed = level.getCapability(Capabilities.Item.BLOCK, hatchAbs, Direction.WEST);
		helper.assertTrue(formed != null && formed.size() > 0, "a formed farm hatch must expose slots");

		// unform: the handler obtained while formed must stay usable
		BlockState plain = MultifarmBlocks.FARM.get(MultifarmBlockType.PLAIN, MultifarmMaterialType.STONE_BRICK).defaultState();
		helper.setBlock(cornerRel, Blocks.AIR);
		helper.assertFalse(MultiblockTestSupport.isAssembled(level, members.get(0)), "farm must be broken");
		insertEverywhere(formed);
		ResourceHandler<ItemResource> unformed = level.getCapability(Capabilities.Item.BLOCK, hatchAbs, Direction.WEST);
		helper.assertTrue(unformed != null && unformed.size() == 0, "an unformed farm hatch must expose no slots");

		// form again: the handler obtained while unformed must now see the slots
		helper.setBlock(cornerRel, plain);
		helper.succeedWhen(() -> {
			helper.assertTrue(MultiblockTestSupport.isAssembled(level, members.get(0)), "farm must reassemble");
			helper.assertTrue(unformed.size() > 0, "the stale handler must see the formed slots");
			insertEverywhere(unformed);
		});
	}

	private static void insertEverywhere(ResourceHandler<ItemResource> handler) {
		try (Transaction tx = Transaction.openRoot()) {
			for (int i = 0; i < 40; i++) {
				handler.insert(i, ItemResource.of(new ItemStack(Items.DIRT)), 1, tx);
			}
		}
	}
}
