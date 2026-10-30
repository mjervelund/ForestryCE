package forestry.arboriculture.wood;

import forestry.api.ForestryConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import java.util.Optional;
import forestry.core.platform.registration.RegistrationIds;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.features.ArboricultureBlocks;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ForestryWallHangingSignBlock extends WallHangingSignBlock implements IWoodTyped {
	private final ForestryWoodType type;

	// 26.x: lootFrom is gone; drop the standing sign through its loot table id
	public ForestryWallHangingSignBlock(ForestryWoodType type) {
		super(type.getWoodType(), RegistrationIds.block().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, ForestryConstants.forestry("blocks/" + type.getSerializedName() + "_hanging_sign")))).ignitedByLava());

		this.type = type;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.WALL_HANGING_SIGN;
	}

	@Override
	public boolean isFireproof() {
		return false;
	}

	@Override
	public IWoodType getWoodType() {
		return this.type;
	}

	// newBlockEntity / getTicker are inherited from WallHangingSignBlock and use vanilla
	// BlockEntityType.HANGING_SIGN. Forestry registers this block as a valid block for that
	// BE type via BlockEntityTypeAddBlocksEvent in ModuleArboriculture.
}
