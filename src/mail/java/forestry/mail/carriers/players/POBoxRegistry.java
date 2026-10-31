package forestry.mail.carriers.players;

import forestry.api.mail.IMailAddress;
import forestry.api.mail.IWatchable;
import forestry.mail.letters.MailAddress;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class POBoxRegistry extends SavedData implements IWatchable.Watcher {
	private static final String SAVE_NAME = "forestry_poboxes";

	public final Map<IMailAddress, POBox> cachedPOBoxes = new HashMap<>();

	/**
	 * @param address the potential address of the PO box
	 * @return true if the passed address is valid for PO Boxes.
	 */
	public boolean isValidPOBox(IMailAddress address) {
		return address.getCarrier().equals(PostalCarriers.PLAYER.value()) && address.getName().matches("^[a-zA-Z0-9]+$");
	}

	private void registerPOBOx(IMailAddress address, POBox box) {
        this.cachedPOBoxes.put(address, box);
		box.registerUpdateWatcher(this);
		setDirty();
	}

	@Nullable
	public POBox getPOBox(IMailAddress address) {
		return this.cachedPOBoxes.get(address);
	}

	public POBox getOrCreatePOBox(IMailAddress address) {
		POBox pobox = getPOBox(address);

		if (pobox == null) {
			pobox = new POBox(address);
			registerPOBOx(address, pobox);
			pobox.setDirty();
		}

		return pobox;
	}

	@Override
	public void onWatchableUpdate() {
		setDirty();
	}

	private static POBoxRegistry create() {
		return new POBoxRegistry();
	}

	private static POBoxRegistry load(CompoundTag compoundTag, HolderLookup.Provider registries) {
		POBoxRegistry registry = new POBoxRegistry();
		ListTag tradeStations = compoundTag.getListOrEmpty("poboxes");
		for (int i = 0; i < tradeStations.size(); ++i) {
			CompoundTag stationTag = tradeStations.getCompoundOrEmpty(i);

			IMailAddress address = new MailAddress(stationTag.getCompoundOrEmpty("address"));
			POBox pobox = new POBox(stationTag.getCompoundOrEmpty("pobox"), registries);
			registry.registerPOBOx(address, pobox);
		}
		return registry;
	}

	public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider registries) {
		ListTag poboxes = new ListTag();
		for (Map.Entry<IMailAddress, POBox> entry : this.cachedPOBoxes.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.put("address", entry.getKey().write(new CompoundTag(), registries));
			entryTag.put("pobox", entry.getValue().write(new CompoundTag(), registries));
			poboxes.add(entryTag);
		}
		compoundTag.put("poboxes", poboxes);
		return compoundTag;
	}

	// 26.x: saved data is codec-based (the file moves to data/forestry/forestry_poboxes.dat)
	private static final net.minecraft.world.level.saveddata.SavedDataType<POBoxRegistry> TYPE = new net.minecraft.world.level.saveddata.SavedDataType<>(
		forestry.api.ForestryConstants.forestry(SAVE_NAME),
		level -> create(),
		level -> CompoundTag.CODEC.xmap(
			tag -> load(tag, level.registryAccess()),
			registry -> registry.save(new CompoundTag(), level.registryAccess())
		)
	);

	public static POBoxRegistry getOrCreate(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}
}
