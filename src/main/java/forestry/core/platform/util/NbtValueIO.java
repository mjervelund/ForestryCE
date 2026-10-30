package forestry.core.platform.util;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Bridges 26.x {@link ValueInput}/{@code ValueOutput} block entity serialization to Forestry's
 * CompoundTag-based read/write code, keeping the 1.21.1 save layout (same keys at the top level).
 * Write side: build a CompoundTag and pass it to {@code ValueOutput#store(CompoundTag)}.
 */
public final class NbtValueIO {
	private NbtValueIO() {
	}

	public static CompoundTag read(ValueInput input) {
		CompoundTag tag = new CompoundTag();
		for (String key : input.keySet()) {
			input.read(key, ExtraCodecs.NBT).ifPresent(value -> tag.put(key, value));
		}
		return tag;
	}

	/** ValueOutput carries no registry lookup, so writers use the level's. */
	public static HolderLookup.Provider registries(@Nullable Level level) {
		return level != null ? level.registryAccess() : RegistryAccess.EMPTY;
	}
}
