package forestry.arboriculture.entities;

import java.util.function.Supplier;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.wood.ForestryWoodType;

public class ForestryChestBoat extends ChestBoat implements IForestryBoat {
	private static final EntityDataAccessor<Integer> DATA_ID_WOOD_TYPE = SynchedEntityData.defineId(ForestryChestBoat.class, EntityDataSerializers.INT);

	public ForestryChestBoat(EntityType<? extends ChestBoat> type, Level level) {
		this(type, level, new DropItem());
	}

	// 26.x boats take their drop item as a constructor supplier; ours depends on the synced wood type
	private ForestryChestBoat(EntityType<? extends ChestBoat> type, Level level, DropItem dropItem) {
		super(type, level, dropItem);
		dropItem.boat = this;
	}

	public ForestryChestBoat(Level level, double x, double y, double z) {
		this(ArboricultureEntities.CHEST_BOAT.entityType(), level);

		setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ID_WOOD_TYPE, ForestryWoodType.CAMELTHORN.ordinal());
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putString("type", getWoodType().name());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		input.getString("type").ifPresent(name -> {
			try {
				setWoodType(ForestryWoodType.valueOf(name));
			} catch (IllegalArgumentException e) {
				setWoodType(ForestryWoodType.CAMELTHORN);
			}
		});
	}

	@Override
	public void setWoodType(ForestryWoodType woodType) {
		this.entityData.set(DATA_ID_WOOD_TYPE, woodType.ordinal());
	}

	@Override
	public ForestryWoodType getWoodType() {
		return ForestryWoodType.VALUES[this.entityData.get(DATA_ID_WOOD_TYPE)];
	}

	private static final class DropItem implements Supplier<Item> {
		private ForestryChestBoat boat;

		@Override
		public Item get() {
			return ArboricultureItems.CHEST_BOAT.item(this.boat.getWoodType());
		}
	}
}
