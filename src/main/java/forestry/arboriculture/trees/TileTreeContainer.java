package forestry.arboriculture.trees;

import forestry.core.platform.util.NbtValueIO;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.core.platform.client.ClientsideCode;
import forestry.core.platform.network.IStreamable;
import forestry.core.platform.util.NBTUtilForestry;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * This is the base TE class for any block that needs to contain tree genome information.
 */
public abstract class TileTreeContainer extends BlockEntity implements IStreamable {
	@Nullable
	private ITree containedTree;

	public TileTreeContainer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/* SAVING & LOADING */
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		CompoundTag data = new CompoundTag();
		saveAdditional(data, NbtValueIO.registries(this.level));
		output.store(data);
	}

	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {

		if (this.containedTree != null) {
			Tag serialized = SpeciesUtil.serializeIndividual(this.containedTree);
			if (serialized != null) {
				nbt.put("ContainedTree", serialized);
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		loadAdditional(NbtValueIO.read(input), input.lookup());
	}

	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {

		Tag treeNbt = nbt.get("ContainedTree");

		if (treeNbt != null) {
			this.containedTree = SpeciesUtil.deserializeIndividual(SpeciesUtil.TREE_TYPE.get(), treeNbt);
		}
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		ITree tree = getTree();
		if (tree != null) {
			data.writeBoolean(true);
			Identifier speciesId = tree.getSpecies().id();
			data.writeIdentifier(speciesId);
		} else {
			data.writeBoolean(false);
		}
	}

	@Override
	public void readData(RegistryFriendlyByteBuf data) {
		if (data.readBoolean()) {
			Identifier speciesId = data.readIdentifier();
			// Sync payload written server-side from a live tree's species id; fall back to the default species
			// instead of throwing if a datapack has since removed it (e.g. reload racing a client packet).
			ITreeSpeciesType type = SpeciesUtil.TREE_TYPE.get();
			ITreeSpecies species = type.getSpeciesSafe(speciesId);
			ITree tree = (species != null ? species : type.getDefaultSpecies()).createIndividual();
			setTree(tree);
		}
	}

	/* CONTAINED TREE */
	public void setTree(ITree tree) {
		this.containedTree = tree;

		if (this.level != null && this.level.isClientSide()) {
			// NeoForge's ModelDataManager only refreshes cached ModelData when requestModelDataUpdate() is
			// called; setBlocksDirty alone re-meshes the section with stale (EMPTY) data, leaving freshly
			// placed saplings rendering as Oak until the BE is reloaded. See TileLeaves#setTree for the same.
			requestModelDataUpdate();
			ClientsideCode.markForUpdate(this.worldPosition);
        }
	}

	@Nullable
	public ITree getTree() {
		return this.containedTree;
	}

	/* UPDATING */

	/**
	 * Leaves and saplings will implement their logic here.
	 */
	public abstract void onBlockTick(Level worldIn, BlockPos pos, BlockState state, RandomSource rand);

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ValueInput input) {
		super.onDataPacket(net, input);
		HolderLookup.Provider registries = input.lookup();
		CompoundTag nbt = NbtValueIO.read(input);
		handleUpdateTag(nbt, registries);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		return NBTUtilForestry.writeStreamableToNbt(this, tag, registries);
	}

	@Override
	public void handleUpdateTag(ValueInput input) {
		super.handleUpdateTag(input);
		handleUpdateTag(NbtValueIO.read(input), input.lookup());
	}

	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		NBTUtilForestry.readStreamableFromNbt(this, tag, registries);
	}
}
