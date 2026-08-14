package com.breakinblocks.nautec.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ShockwaveRingParticle extends SingleQuadParticle {
    private static final SingleQuadParticle.FacingCameraMode FLAT =
            (target, camera, partialTick) -> target.rotationX(Mth.HALF_PI);

    private final float startSize;
    private final float endSize;

    protected ShockwaveRingParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite,
                                    float startSize, float endSize, int lifetime) {
        super(level, x, y, z, 0D, 0D, 0D, sprite);
        this.startSize = startSize;
        this.endSize = endSize;
        this.quadSize = startSize;
        this.lifetime = lifetime;
        this.hasPhysics = false;
        this.friction = 1F;
    }

    @Override
    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return FLAT;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float) this.age / this.lifetime;
        this.quadSize = Mth.lerp(progress, this.startSize, this.endSize);
        this.alpha = 1F - progress * progress;
    }

    public record Provider(SpriteSet sprites, float red, float green, float blue,
                           float startSize, float endSize, int lifetime) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                                       double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            ShockwaveRingParticle particle = new ShockwaveRingParticle(level, x, y, z, sprites.get(random),
                    startSize, endSize, lifetime);
            particle.setColor(red, green, blue);
            return particle;
        }
    }
}
