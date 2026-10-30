package forestry.core.platform.models.baker;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The quads made by a {@link ModelBaker}.
 * <p>
 * 26.x: a block state model part (was a BakedModel); items use {@link #getAllQuads()}.
 */
@OnlyIn(Dist.CLIENT)
public final class ModelBakerModel implements BlockStateModelPart {
	private final QuadCollection quads;
	private final boolean ambientOcclusion;
	private final Material.Baked particleMaterial;

	ModelBakerModel(QuadCollection quads, boolean ambientOcclusion, Material.Baked particleMaterial) {
		this.quads = quads;
		this.ambientOcclusion = ambientOcclusion;
		this.particleMaterial = particleMaterial;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable Direction direction) {
		return this.quads.getQuads(direction);
	}

	public List<BakedQuad> getAllQuads() {
		return this.quads.getAll();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.ambientOcclusion;
	}

	@Override
	public Material.Baked particleMaterial() {
		return this.particleMaterial;
	}

	@Override
	public int materialFlags() {
		return this.quads.materialFlags();
	}
}
