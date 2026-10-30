package forestry.core.platform.client;

import forestry.core.engine.genetics.root.ClientBreedingHandler;
import forestry.core.engine.genetics.root.ServerBreedingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.Optional;

// Rule of thumb for safely calling client code: client classes must be in the body of a static method
// in another class (like this one), guarded by an if statement checking FMLEnvironment.getDist() == Dist.CLIENT
// Calls to this class must be guarded by an if statement.
// DistExecutor will be deprecated for removal in 1.20, and it doesn't work anyway.
public class ClientsideCode {
	public static ServerBreedingHandler newBreedingHandler() {
		return new ClientBreedingHandler();
	}

	@Nullable
	private static RecipeMap recipeMap;

	/** The Forestry recipe types the server synced (see {@code ModuleCore#onDatapackSync}), or null before the first sync. */
	@Nullable
	public static RecipeMap getRecipeMap() {
		return Minecraft.getInstance().getConnection() != null ? recipeMap : null;
	}

	public static void setRecipeMap(RecipeMap map) {
		recipeMap = map;
	}

	public static Registry<Fluid> getFluidRegistry() {
		return Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.FLUID);
	}

	public static void markForUpdate(BlockPos pos) {
		// 26.x: LevelRenderer has no setBlocksDirty; go through the level (an air old state forces the re-render)
		var level = Minecraft.getInstance().level;
		if (level != null) {
			level.setBlocksDirty(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), level.getBlockState(pos));
		}
	}

	public static RegistryAccess getRegistryAccess() {
		return Minecraft.getInstance().level.registryAccess();
	}

	public static Optional<Resource> getResource(Identifier path) {
		return Minecraft.getInstance().getResourceManager().getResource(path);
	}
}
