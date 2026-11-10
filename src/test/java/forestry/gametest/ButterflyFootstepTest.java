package forestry.gametest;

import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;

import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.core.platform.util.SpeciesUtil;
import forestry.lepidopterology.entities.EntityButterfly;

/**
 * Butterflies fly; landing and walking on blocks must not play footstep sounds (ForestryCE#445). Vanilla's {@code Bat}
 * does this by returning {@link Entity.MovementEmission#EVENTS} from {@code getMovementEmission}, which suppresses step,
 * muffled-step, combination-step and amethyst sounds in {@code Entity.move} while keeping game events.
 */
public class ButterflyFootstepTest {
	@ForestryGameTest
	public static void butterflyMovementEmitsNoSounds(GameTestHelper helper) {
		IButterflySpeciesType type = SpeciesUtil.BUTTERFLY_TYPE.get();
		BlockPos abs = helper.absolutePos(new BlockPos(2, 2, 2));
		EntityButterfly butterfly = (EntityButterfly) type.spawnButterflyInWorld(helper.getLevel(),
			type.getDefaultSpecies().createIndividual(), abs.getX(), abs.getY(), abs.getZ());
		try {
			Method method = Entity.class.getDeclaredMethod("getMovementEmission");
			method.setAccessible(true);
			Entity.MovementEmission emission = (Entity.MovementEmission) method.invoke(butterfly);
			helper.assertTrue(!emission.emitsSounds(), "butterfly movement emission " + emission + " plays step sounds");
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		} finally {
			butterfly.discard();
		}
		helper.succeed();
	}
}
