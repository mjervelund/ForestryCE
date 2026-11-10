package forestry.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

import forestry.core.content.energy.blocks.EngineBlockType;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.tiles.ClockworkEngineBlockEntity;
import forestry.core.platform.damage.CoreDamageTypes;

/**
 * ForestryCE#415: over-winding a clockwork engine hurt the player with a position-less damage source, so vanilla's
 * knockback (LivingEntity.dealDefaultKnockback) had no direction to push from and the player went a random way. The
 * damage source must carry the engine's centre so the knockback pushes away from the engine.
 */
public class ClockworkOverwindDamageTest {
	private static final BlockPos ENGINE_POS = new BlockPos(4, 2, 4);
	private static final int MAX_WINDS = 500;

	@ForestryGameTest
	public static void overwindDamageComesFromEngine(GameTestHelper helper) {
		helper.setBlock(ENGINE_POS, EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK).defaultState());
		BlockPos enginePos = helper.absolutePos(ENGINE_POS);
		if (!(helper.getLevel().getBlockEntity(enginePos) instanceof ClockworkEngineBlockEntity engine)) {
			helper.fail("Expected a ClockworkEngineBlockEntity at " + ENGINE_POS);
			return;
		}

		DamageSource[] captured = new DamageSource[1];
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "clockwork-winder"), ClientInformation.createDefault()) {
			@Override
			public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
				captured[0] = source;
				return false;
			}
		};
		player.setPos(enginePos.getX() + 2.5, enginePos.getY(), enginePos.getZ() + 0.5);

		for (int i = 0; i < MAX_WINDS && captured[0] == null; i++) {
			engine.openGui(player, InteractionHand.MAIN_HAND, enginePos);
		}

		DamageSource source = captured[0];
		helper.assertTrue(source != null, "over-winding never damaged the player");
		helper.assertTrue(source.is(CoreDamageTypes.CLOCKWORK), "damage was not clockwork: " + source);
		Vec3 expected = Vec3.atCenterOf(enginePos);
		helper.assertTrue(expected.equals(source.getSourcePosition()), "clockwork damage source position is " + source.getSourcePosition() + ", expected the engine centre " + expected);
		// vanilla knockback pushes along (player - sourcePosition): the player stands east of the engine, so it must go east
		helper.assertTrue(player.getX() - source.getSourcePosition().x() > 0, "knockback would not push away from the engine");
		helper.succeed();
	}
}
