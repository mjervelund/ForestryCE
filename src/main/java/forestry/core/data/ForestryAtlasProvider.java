package forestry.core.data;

import forestry.api.ForestryConstants;
import forestry.core.platform.render.ForestrySpriteUploader;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;

public class ForestryAtlasProvider extends SpriteSourceProvider {
	public ForestryAtlasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
		super(output, lookupProvider, ForestryConstants.MOD_ID, fileHelper);
	}

	@Override
	protected void gather() {
		SourceList icons = atlas(ForestrySpriteUploader.ATLAS_PATH);

		icons.addSource(new DirectoryLister("forestry/atlas/gui", ""));
		// 26.x: slot backgrounds (Slot#getNoItemIcon) are drawn from the gui atlas, so the slot icons are no longer
		// also added to the blocks atlas (sprite ids are global on 26.x; the copy was a duplicate sprite)
	}
}
