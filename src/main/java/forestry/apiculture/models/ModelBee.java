package forestry.apiculture.models;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.apiculture.IBeeClientManager;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.core.platform.models.SpeciesItemModel;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/**
 * The item model for a bee, which supports custom bee textures.
 * Only supports the four life stages used in base Forestry (DRONE, LARVAE, PRINCESS, QUEEN).
 * <p>
 * 26.x: a {@link SpeciesItemModel} kind per life stage, {@code forestry:bee_<stage>} (was a geometry loader
 * with ItemOverrides).
 */
public final class ModelBee {
	private ModelBee() {
	}

	public static Identifier typeId(BeeLifeStage stage) {
		return Identifier.fromNamespaceAndPath("forestry", "bee_" + stage.name().toLowerCase(Locale.ENGLISH));
	}

	public static SpeciesItemModel.Kind kind(BeeLifeStage stage) {
		return new SpeciesItemModel.Kind(
			() -> manager().getAllModelLocations(stage),
			stack -> {
				IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
				return individual == null ? manager().getDefaultModelLocation(stage) : manager().getModelLocation(stage, individual.getSpecies().id());
			},
			() -> manager().getDefaultModelLocation(stage)
		);
	}

	private static IBeeClientManager manager() {
		return IForestryClientApi.INSTANCE.getBeeManager();
	}
}
