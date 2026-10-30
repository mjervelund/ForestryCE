package forestry.core.platform.util;

import net.minecraft.network.chat.Component;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 26.x tooltips are built through a {@code Consumer<Component>}; this list passes every added line on to it, so
 * the API methods that take a {@code List<Component>} (species, circuits, mail) keep their signatures.
 */
public final class TooltipLines extends AbstractList<Component> {
	private final Consumer<Component> sink;
	private final List<Component> added = new ArrayList<>();

	public TooltipLines(Consumer<Component> sink) {
		this.sink = sink;
	}

	@Override
	public void add(int index, Component line) {
		if (index != this.added.size()) {
			throw new UnsupportedOperationException("Tooltip lines can only be appended");
		}
		this.added.add(line);
		this.sink.accept(line);
	}

	@Override
	public Component get(int index) {
		return this.added.get(index);
	}

	@Override
	public int size() {
		return this.added.size();
	}
}
