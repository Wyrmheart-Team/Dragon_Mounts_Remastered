package dmr.DragonMounts.client.particle.particletypes;

import com.mojang.serialization.MapCodec;
import dmr.DragonMounts.client.particle.particleoptions.DragonBreathParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class DragonBreathParticleType extends ParticleType<DragonBreathParticleOptions> {
    public DragonBreathParticleType(boolean overrideLimitter) {
        super(overrideLimitter);
    }

    @Override
    public MapCodec<DragonBreathParticleOptions> codec() {
        return DragonBreathParticleOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonBreathParticleOptions> streamCodec() {
        return DragonBreathParticleOptions.STREAM_CODEC;
    }
}
