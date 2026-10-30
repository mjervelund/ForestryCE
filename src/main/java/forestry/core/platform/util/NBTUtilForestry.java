package forestry.core.platform.util;

import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.UUIDUtil;
import com.mojang.authlib.GameProfile;
import forestry.core.platform.network.IStreamable;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.connection.ConnectionType;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class NBTUtilForestry {
	private static final String GAME_PROFILE_ID = "Id";
	private static final String GAME_PROFILE_NAME = "Name";

	public static CompoundTag writeStreamableToNbt(IStreamable streamable, CompoundTag nbt, HolderLookup.Provider registries) {
		return writeStreamableToNbt(streamable, nbt, (RegistryAccess) registries);
	}

	public static CompoundTag writeStreamableToNbt(IStreamable streamable, CompoundTag nbt, RegistryAccess registries) {
		RegistryFriendlyByteBuf data = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, ConnectionType.NEOFORGE);
		streamable.writeData(data);

		byte[] bytes = new byte[data.readableBytes()];
		data.getBytes(0, bytes);
		nbt.putByteArray("dataBytes", bytes);
		return nbt;
	}

	public static void readStreamableFromNbt(IStreamable streamable, CompoundTag nbt, HolderLookup.Provider registries) {
		readStreamableFromNbt(streamable, nbt, (RegistryAccess) registries);
	}

	public static void readStreamableFromNbt(IStreamable streamable, CompoundTag nbt, RegistryAccess registries) {
		if (nbt.contains("dataBytes")) {
			byte[] bytes = nbt.getByteArray("dataBytes").orElse(new byte[0]);
			RegistryFriendlyByteBuf data = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), registries, ConnectionType.NEOFORGE);
			streamable.readData(data);
		}
	}

	@Nullable
	public static GameProfile readGameProfile(CompoundTag nbt) {
		if (nbt.isEmpty()) {
			return null;
		}

		UUID id = nbt.read(GAME_PROFILE_ID, UUIDUtil.CODEC).orElse(null);
		String name = nbt.getString(GAME_PROFILE_NAME).orElse(null);
		if (id == null && (name == null || name.isEmpty())) {
			return null;
		}
		// authlib rejects a null id or name. A half-written profile is a name-only trade station address
		// or an id-only owner, so fill the missing half with the value the rest of the mod reads as absent
		return new GameProfile(id == null ? Util.NIL_UUID : id, name == null ? "" : name);
	}

	public static CompoundTag writeGameProfile(CompoundTag nbt, GameProfile profile) {
		if (profile.id() != null) {
			nbt.store(GAME_PROFILE_ID, UUIDUtil.CODEC, profile.id());
		}
		if (profile.name() != null) {
			nbt.putString(GAME_PROFILE_NAME, profile.name());
		}
		return nbt;
	}

	@Nullable
	public static CompoundTag getItemStackTag(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		return customData == null || customData.isEmpty() ? null : customData.copyTag();
	}

	public static void setItemStackTag(ItemStack stack, CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
	}

	// 26.x: ItemStack#save/saveOptional/parse and FluidStack#save/parseOptional are gone; these write the same
	// {id, count, components} compound through the codecs (an empty stack is an empty compound, as saveOptional wrote)
	public static CompoundTag saveStack(HolderLookup.Provider registries, ItemStack stack) {
		return (CompoundTag) ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
	}

	public static ItemStack parseStack(HolderLookup.Provider registries, Tag tag) {
		return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
	}

	public static CompoundTag saveFluid(HolderLookup.Provider registries, FluidStack fluid) {
		return (CompoundTag) FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), fluid).getOrThrow();
	}

	public static FluidStack parseFluid(HolderLookup.Provider registries, Tag tag) {
		return FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(FluidStack.EMPTY);
	}
}
