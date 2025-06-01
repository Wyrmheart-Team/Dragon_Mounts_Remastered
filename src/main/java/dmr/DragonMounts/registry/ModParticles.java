package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.client.particle.particletypes.DragonBreathParticleType;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, DMR.MOD_ID);

    public static final Supplier<DragonBreathParticleType> DRAGON_BREATH_PARTICLE =
            PARTICLE_TYPES.register("dragon_breath", () -> new DragonBreathParticleType(false));
}
