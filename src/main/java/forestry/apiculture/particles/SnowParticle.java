package forestry.apiculture.particles;

import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SnowParticle extends SingleQuadParticle {
	public static final TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[3];

	public SnowParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z, 0, 0, 0, SPRITES[level.getRandom().nextInt(SPRITES.length)]);
		this.quadSize *= 0.5F;
		this.lifetime = (int) (40 / (this.random.nextDouble() * 0.8D + 0.2D));

		this.xd *= 0.01D;
		this.yd *= -0.4D;
		this.zd *= 0.01D;
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}
}
