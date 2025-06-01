package dmr.DragonMounts.client.particle.providers;

import dmr.DragonMounts.client.particle.DragonBreathParticle;
import dmr.DragonMounts.client.particle.particleoptions.DragonBreathParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class DragonBreathParticleProvider implements ParticleProvider<DragonBreathParticleOptions> {
    private final SpriteSet sprites;

    public DragonBreathParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(
            DragonBreathParticleOptions options,
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed) {
        DragonBreathParticle particle =
                new DragonBreathParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options.getGradientColors());
        particle.pickSprite(this.sprites);
        return particle;
    }
}
