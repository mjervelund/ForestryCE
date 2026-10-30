package forestry.core.platform.models.baker;

import forestry.core.platform.util.ResourceUtil;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker.Interner;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.core.Direction;
import com.mojang.math.Quadrant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * A model baker to make custom block models with more than one texture layer.
 */
@OnlyIn(Dist.CLIENT)
public final class ModelBaker {
	private static final Vector3fc POS_FROM = new Vector3f(0.0F, 0.0F, 0.0F);
	private static final Vector3fc POS_TO = new Vector3f(16.0F, 16.0F, 16.0F);
	private static final CuboidFace.UVs FULL_FACE = new CuboidFace.UVs(0.0F, 0.0F, 16.0F, 16.0F);
	// 26.x: FaceBakery interns vectors and material infos while models load; these quads are baked at render
	// time on chunk workers, so they are not interned
	private static final Interner NO_INTERNING = new Interner() {
		@Override
		public Vector3fc vector(Vector3fc vector) {
			return vector;
		}

		@Override
		public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo material) {
			return material;
		}
	};

	private final List<ModelBakerFace> faces = new ArrayList<>();
	private TextureAtlasSprite particleSprite = ResourceUtil.getMissingTexture();
	private boolean ambientOcclusion = true;

	private int colorIndex = -1;

	public ModelBaker addBlockModel(TextureAtlasSprite[] textures, int colorIndex) {
		this.colorIndex = colorIndex;

		for (Direction facing : Direction.VALUES) {
			addFace(facing, textures[facing.ordinal()]);
		}
		return this;
	}

	public ModelBaker addBlockModel(TextureAtlasSprite texture, int colorIndex) {
		return addBlockModel(new TextureAtlasSprite[]{texture, texture, texture, texture, texture, texture}, colorIndex);
	}

	public ModelBaker addFace(Direction facing, TextureAtlasSprite sprite) {
		if (sprite != ResourceUtil.getMissingTexture()) {
			this.faces.add(new ModelBakerFace(facing, this.colorIndex, sprite));
		}
		return this;
	}

	public void setAmbientOcclusion(boolean ambientOcclusion) {
		this.ambientOcclusion = ambientOcclusion;
	}

	public ModelBakerModel bake() {
		QuadCollection.Builder quads = new QuadCollection.Builder();
		for (ModelBakerFace face : this.faces) {
			Material.Baked material = new Material.Baked(face.spite, false);
			// the chunk layer follows the sprite (cutout leaves, opaque wood), as for a JSON model
			BakedQuad.MaterialInfo materialInfo = BakedQuad.MaterialInfo.of(material,
				face.spite.contents().computeTransparency(0.0F, 0.0F, 1.0F, 1.0F), face.colorIndex, null, 0, true);
			BakedQuad quad = FaceBakery.bakeQuad(NO_INTERNING, POS_FROM, POS_TO, FULL_FACE, Quadrant.R0, materialInfo,
				face.face, BlockModelRotation.IDENTITY, null, ExtraFaceData.DEFAULT);
			quads.addCulledFace(face.face, quad);
		}
		return new ModelBakerModel(quads.build(), this.ambientOcclusion, new Material.Baked(this.particleSprite, false));
	}

	public void setParticleSprite(TextureAtlasSprite particleSprite) {
		this.particleSprite = particleSprite;
	}
}
