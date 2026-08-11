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

public class SwirlParticle extends SingleQuadParticle {
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final double startRadius;
    private final double rise;
    private final float spin;
    private float angle;

    protected SwirlParticle(ClientLevel level, double centerX, double centerY, double centerZ,
                            TextureAtlasSprite sprite, double startRadius, double rise,
                            float angle, float spin, float size, int lifetime) {
        super(level, centerX + startRadius * Mth.cos(angle), centerY, centerZ + startRadius * Mth.sin(angle), 0D, 0D, 0D, sprite);
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.startRadius = startRadius;
        this.rise = rise;
        this.angle = angle;
        this.spin = spin;
        this.quadSize = size;
        this.lifetime = lifetime;
        this.hasPhysics = false;
        this.friction = 1F;
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
        this.angle += this.spin;
        double radius = this.startRadius * (1D - progress);

        this.setPos(
                this.centerX + radius * Mth.cos(this.angle),
                this.centerY + this.rise * this.age,
                this.centerZ + radius * Mth.sin(this.angle));

        this.alpha = 1F - progress * progress;
    }

    public record Provider(SpriteSet sprites, float red, float green, float blue,
                           double radius, double rise, float spin, float size,
                           int minLifetime, int maxLifetime) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                                       double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            int lifetime = minLifetime + random.nextInt(Math.max(1, maxLifetime - minLifetime));
            float angle = random.nextFloat() * Mth.TWO_PI;
            SwirlParticle particle = new SwirlParticle(level, x, y, z, sprites.get(random),
                    radius, rise, angle, spin, size, lifetime);
            particle.setColor(red, green, blue);
            return particle;
        }
    }
}
