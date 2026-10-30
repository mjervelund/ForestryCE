package forestry.core.content.tools;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorMaterial;
import forestry.core.platform.registration.RegistrationIds;
import forestry.api.ForestryConstants;
import forestry.api.core.ISpectacleVision;
import forestry.core.platform.capabilities.SpectacleVision;
import forestry.core.platform.config.Constants;
import forestry.core.platform.util.ItemTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import java.util.List;

public class ItemSpectacles extends Item {
	public static final String TEXTURE_NATURALIST_ARMOR_PRIMARY = ForestryConstants.MOD_ID + ":" + Constants.TEXTURE_PATH_ITEM + "/naturalist_armor_1.png";
	public static final ISpectacleVision SPECTACLE_VISION = SpectacleVision.INSTANCE;

	// leather helmet stats, drawn with the naturalist texture (assets/forestry/equipment/naturalist.json)
	private static final ArmorMaterial NATURALIST = new ArmorMaterial(5, ArmorMaterials.LEATHER.defense(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
		ArmorMaterials.LEATHER.repairIngredient(), ResourceKey.create(EquipmentAssets.ROOT_ID, ForestryConstants.forestry("naturalist")));

	public ItemSpectacles() {
		super(RegistrationIds.item().humanoidArmor(NATURALIST, ArmorType.HELMET).durability(100));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag advanced) {
		ItemTooltipUtil.addInformation(stack, tooltip);
	}
}
