package forestry.core.engine.genetics.root;

import java.util.Locale;
import forestry.api.ForestryConstants;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.nbt.CompoundTag;
import com.mojang.serialization.Codec;
import com.mojang.authlib.GameProfile;
import forestry.api.core.genetics.IBreedingTracker;
import forestry.api.core.genetics.ISpeciesType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;

public class ServerBreedingHandler implements BreedingTrackerManager.SidedHandler {
	@Override
	@SuppressWarnings("unchecked")
	public <T extends IBreedingTracker> T getTracker(ISpeciesType<?, ?> type, LevelAccessor level, @Nullable GameProfile profile) {
		String filename = type.getBreedingTrackerFile(profile);
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
		// 26.x: saved data is identified by an Identifier (lowercase path, under data/forestry/) and stored through a codec
		Codec<SavedData> codec = CompoundTag.CODEC.xmap(
			tag -> (SavedData) type.createBreedingTracker(tag),
			data -> {
				CompoundTag tag = new CompoundTag();
				((IBreedingTracker) data).save(tag);
				return tag;
			}
		);
		SavedDataType<SavedData> dataType = new SavedDataType<>(ForestryConstants.forestry(filename.toLowerCase(Locale.ROOT)),
			() -> (SavedData) type.createBreedingTracker(), codec);
		T tracker = (T) overworld.getDataStorage().computeIfAbsent(dataType);
		type.initializeBreedingTracker(tracker, overworld, profile);
		return tracker;
	}
}
