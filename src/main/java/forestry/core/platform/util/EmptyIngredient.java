package forestry.core.platform.util;

import forestry.core.platform.util.EmptyIngredient;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/**
 * 26.x removed {@code EmptyIngredient.EMPTY} (vanilla ingredients can no longer be empty). Forestry recipes use an empty
 * ingredient for "no item" (fluid-only fermenter recipes, no fabricator plan, no carpenter box), so this custom
 * ingredient matches nothing and reports itself empty. Registered as {@code forestry:empty} in {@code CoreIngredientTypes}.
 */
public final class EmptyIngredient implements ICustomIngredient {
	public static final EmptyIngredient INSTANCE = new EmptyIngredient();
	public static final IngredientType<EmptyIngredient> TYPE = new IngredientType<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));
	public static final Ingredient EMPTY = new Ingredient(INSTANCE);

	private EmptyIngredient() {
	}

	@Override
	public boolean test(ItemStack stack) {
		return false;
	}

	@Override
	public Stream<Holder<Item>> items() {
		return Stream.empty();
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public IngredientType<?> getType() {
		return TYPE;
	}
}
