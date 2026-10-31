package forestry.core.platform.registration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class FeatureCreativeTab extends ModFeature {
	private final DeferredHolder<CreativeModeTab, CreativeModeTab> creativeTabObject;

	public FeatureCreativeTab(IFeatureRegistry registry, Identifier moduleId, String name, Consumer<CreativeModeTab.Builder> builder) {
		super(moduleId, name);

		this.creativeTabObject = registry.getRegistry(Registries.CREATIVE_MODE_TAB).register(name, () -> {
			CreativeModeTab.Builder tab = CreativeModeTab.builder().title(Component.translatable("itemGroup." + name));
			builder.accept(tab);
			return tab.build();
		});
	}

	/**
	 * Wraps a tab's item generator so an item listed twice (shared helpers such as genetic basics, the same gear
	 * listed under two headings, a wood kind without a fireproof variant) is added once: the tab output throws on
	 * a repeated equal stack, which crashes every creative tab rebuild.
	 */
	public static CreativeModeTab.DisplayItemsGenerator unique(CreativeModeTab.DisplayItemsGenerator generator) {
		return (params, output) -> {
			Set<ItemStack> seen = ItemStackLinkedSet.createTypeAndComponentsSet();
			generator.accept(params, (stack, visibility) -> {
				if (seen.add(stack)) {
					output.accept(stack, visibility);
				}
			});
		};
	}

	public CreativeModeTab creativeTab() {
		return this.creativeTabObject.get();
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.CREATIVE_MODE_TAB;
	}

	public ResourceKey<CreativeModeTab> getKey() {
		return Objects.requireNonNull(this.creativeTabObject.getKey());
	}
}
