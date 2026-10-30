package forestry.apiculture.apiarist;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;
import forestry.core.platform.registration.RegistrationIds;
import forestry.api.ForestryConstants;
import forestry.api.apiculture.IBeeProtection;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.core.content.resources.EnumCraftingMaterial;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemArmorApiarist extends Item {
	// 1.21.1 apiarist armor had no durability; 26.x armor always has one, so use leather's
	private static final ArmorMaterial APIARIST_ARMOR_MATERIAL = new ArmorMaterial(
		5,
		Map.of(
			ArmorType.BOOTS, 1,
			ArmorType.LEGGINGS, 2,
			ArmorType.CHESTPLATE, 3,
			ArmorType.HELMET, 1,
			ArmorType.BODY, 3
		),
		15,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		0.0F,
		0.0F,
		TagKey.create(Registries.ITEM, ForestryConstants.forestry("repairs_apiarist_armor")),
		ResourceKey.create(EquipmentAssets.ROOT_ID, ForestryConstants.forestry("apiarist_armor"))
	);

	public enum BeeProtection implements IBeeProtection {
		INSTANCE;

		@Override
		public boolean protectEntity(LivingEntity entity, ItemStack armor, @Nullable IBeeEffect cause, boolean doProtect) {
			return true;
		}
	}

	public ItemArmorApiarist(ArmorType type) {
		super(RegistrationIds.item().humanoidArmor(APIARIST_ARMOR_MATERIAL, type));
	}
}
