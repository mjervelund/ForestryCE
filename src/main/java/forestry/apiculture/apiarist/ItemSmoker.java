package forestry.apiculture.apiarist;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;
import forestry.core.platform.registration.RegistrationIds;
import forestry.api.apiculture.hives.IHiveTile;
import forestry.core.platform.item.ItemForestry;
import forestry.core.platform.render.ParticleRender;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ItemSmoker extends ItemForestry {
	public ItemSmoker() {
		super(RegistrationIds.item().stacksTo(1));
	}

	// 26.x: inventoryTick only runs on the server, so the idle puff while held is sent as vanilla smoke
	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND && level.getRandom().nextInt(40) == 0) {
			Vec3 smokePos = getSmokePos(entity, 1);
			level.sendParticles(ParticleTypes.SMOKE, smokePos.x, smokePos.y + 1, smokePos.z, 1, 0, 0, 0, 0);
		}
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
		addSmoke(level, entity, (count % 5) + 1);
	}

	private static HumanoidArm getHandSide(Entity entity) {
		if (entity instanceof LivingEntity LivingEntity) {
			InteractionHand activeHand = LivingEntity.getUsedItemHand();
			HumanoidArm handSide = LivingEntity.getMainArm();
			if (activeHand == InteractionHand.OFF_HAND) {
				handSide = handSide.getOpposite();
			}
			return handSide;
		}
		return HumanoidArm.RIGHT;
	}

	private static void addSmoke(Level level, Entity entity, int distance) {
		if (distance <= 0) {
			return;
		}
		Vec3 smokePos = getSmokePos(entity, distance);

		if (level.isClientSide()) {
			ParticleRender.addEntitySmokeFX(level, smokePos.x, smokePos.y + 1, smokePos.z);
		}

		BlockPos blockPos = BlockPos.containing(smokePos.x, smokePos.y + 1, smokePos.z);
		TileUtil.actOnTile(level, blockPos, IHiveTile.class, IHiveTile::calmBees);
	}

	private static Vec3 getSmokePos(Entity entity, int distance) {
		Vec3 look = entity.getLookAngle();
		HumanoidArm handSide = getHandSide(entity);

		Vec3 handOffset;
		if (handSide == HumanoidArm.RIGHT) {
			handOffset = look.cross(new Vec3(0, 1, 0));
		} else {
			handOffset = look.cross(new Vec3(0, -1, 0));
		}

		Vec3 lookDistance = new Vec3(look.x * distance, look.y * distance, look.z * distance);
		Vec3 scaledOffset = handOffset.scale(1.0 / distance);
		return lookDistance.add(entity.position()).add(scaledOffset);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		ItemStack itemStack = player.getItemInHand(hand);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		TileUtil.actOnTile(context.getLevel(), context.getClickedPos(), IHiveTile.class, IHiveTile::calmBees);
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 32;
	}
}
