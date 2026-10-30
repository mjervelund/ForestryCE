package forestry.arboriculture.entities;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import forestry.arboriculture.wood.ForestryWoodType;

/** A Forestry boat or chest boat: one entity type for every Forestry wood, the wood is synced data. */
public interface IForestryBoat {
	ForestryWoodType getWoodType();

	void setWoodType(ForestryWoodType woodType);

	default AbstractBoat asBoat() {
		return (AbstractBoat) this;
	}
}
