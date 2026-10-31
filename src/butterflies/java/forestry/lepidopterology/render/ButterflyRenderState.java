package forestry.lepidopterology.render;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class ButterflyRenderState extends LivingEntityRenderState {
	@Nullable
	public Identifier texture;
	public float size = 1f;
	public float wingFlap;
	public boolean renderable;
}
