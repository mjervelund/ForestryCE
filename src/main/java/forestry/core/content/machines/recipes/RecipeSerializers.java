package forestry.core.content.machines.recipes;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class RecipeSerializers {
	static <E> void write(FriendlyByteBuf buffer, List<E> list, BiConsumer<FriendlyByteBuf, E> consumer) {
		buffer.writeVarInt(list.size());

		for (E e : list) {
			consumer.accept(buffer, e);
		}
	}

	static <E> List<E> read(FriendlyByteBuf buffer, Function<FriendlyByteBuf, E> reader) {
		int size = buffer.readVarInt();

		ArrayList<E> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(reader.apply(buffer));
		}

		return list;
	}
}
