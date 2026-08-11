package com.breakinblocks.nautec.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class SparkParticle extends SingleQuadParticle {
    private final float startSize;

    protected SparkParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za,
                            TextureAtlasSprite sprite, float size, int lifetime) {
        super(level, x, y, z, xa, ya, za, sprite);
        this.startSize = size;
        this.quadSize = size;
        this.lifetime = lifetime;
        this.hasPhysics = false;
        this.friction = 0.88F;
        this.gravity = 0.12F;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        float remaining = 1F - (float) this.age / this.lifetime;
        this.quadSize = this.startSize * remaining;
        this.alpha = remaining;
    }

    public record Provider(SpriteSet sprites, float red, float green, float blue,
                           float spread, float size, int minLifetime, int maxLifetime) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                                       double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            int lifetime = minLifetime + random.nextInt(Math.max(1, maxLifetime - minLifetime));
            SparkParticle particle = new SparkParticle(level, x, y, z,
                    xa + (random.nextFloat() - 0.5F) * spread,
                    ya + (random.nextFloat() - 0.5F) * spread,
                    za + (random.nextFloat() - 0.5F) * spread,
                    sprites.get(random), size, lifetime);
            particle.setColor(red, green, blue);
            return particle;
        }
    }
}
