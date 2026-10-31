package forestry.lepidopterology.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import forestry.lepidopterology.butterflies.ItemButterflyGE;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * {@code forestry:cocoon_age}: selects the GE cocoon item model by its age. Replaces the 1.21.1
 * {@code ItemProperties} float property {@code forestry:age} (the generated {@code items/cocoon_ge.json} switches on it).
 */
public record CocoonAgeProperty() implements SelectItemModelProperty<Integer> {

	public static final MapCodec<CocoonAgeProperty> CODEC = MapCodec.unit(CocoonAgeProperty::new);
	public static final Type<CocoonAgeProperty, Integer> TYPE = Type.create(CODEC, Codec.INT);

	@Override
	public Integer get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		return ItemButterflyGE.getAge(stack);
	}

	@Override
	public Codec<Integer> valueCodec() {
		return Codec.INT;
	}

	@Override
	public Type<? extends SelectItemModelProperty<Integer>, Integer> type() {
		return TYPE;
	}
}
