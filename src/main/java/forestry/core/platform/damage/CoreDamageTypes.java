package forestry.core.platform.damage;

import forestry.api.ForestryConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

public class CoreDamageTypes {
	public static final ResourceKey<DamageType> AGGRESSIVE = ResourceKey.create(Registries.DAMAGE_TYPE, ForestryConstants.forestry("aggressive"));
	public static final ResourceKey<DamageType> HEROIC = ResourceKey.create(Registries.DAMAGE_TYPE, ForestryConstants.forestry("heroic"));
	public static final ResourceKey<DamageType> MISANTHROPE = ResourceKey.create(Registries.DAMAGE_TYPE, ForestryConstants.forestry("misanthrope"));
	public static final ResourceKey<DamageType> RADIOACTIVE = ResourceKey.create(Registries.DAMAGE_TYPE, ForestryConstants.forestry("radioactive"));
	public static final ResourceKey<DamageType> HIVE = ResourceKey.create(Registries.DAMAGE_TYPE, ForestryConstants.forestry("hive"));
	public static final ResourceKey<DamageType> CLOCKWORK = ResourceKey.create(Registries.DAMAGE_TYPE, ForestryConstants.forestry("clockwork"));

	public static DamageSource source(LevelReader level, ResourceKey<DamageType> type) {
		return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type));
	}

	/** A damage source located at {@code position}, so vanilla knockback pushes the victim away from it (ForestryCE#415). */
	public static DamageSource source(LevelReader level, ResourceKey<DamageType> type, Vec3 position) {
		return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type), position);
	}
}
