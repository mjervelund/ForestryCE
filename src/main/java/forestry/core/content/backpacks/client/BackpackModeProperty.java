package forestry.core.content.backpacks.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import forestry.core.content.backpacks.BackpackMode;
import forestry.core.content.backpacks.items.BackpackItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * {@code forestry:backpack_mode}: selects a backpack item model by its mode. Replaces the 1.21.1
 * {@code ItemProperties} float property {@code minecraft:mode} (generated {@code items/*.json} switch on it).
 */
public record BackpackModeProperty() implements SelectItemModelProperty<BackpackMode> {

	public static final Codec<BackpackMode> MODE_CODEC = StringRepresentable.fromEnum(() -> BackpackMode.VALUES);
	public static final MapCodec<BackpackModeProperty> CODEC = MapCodec.unit(BackpackModeProperty::new);
	public static final Type<BackpackModeProperty, BackpackMode> TYPE = Type.create(CODEC, MODE_CODEC);

	@Override
	public BackpackMode get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		return BackpackItem.getMode(stack);
	}

	@Override
	public Codec<BackpackMode> valueCodec() {
		return MODE_CODEC;
	}

	@Override
	public Type<? extends SelectItemModelProperty<BackpackMode>, BackpackMode> type() {
		return TYPE;
	}
}
