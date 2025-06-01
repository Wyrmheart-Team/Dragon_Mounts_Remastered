package dmr.DragonMounts.client.particle.particleoptions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dmr.DragonMounts.registry.ModParticles;
import dmr.DragonMounts.types.breath.DragonBreathType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

@Getter
public class DragonBreathParticleOptions implements ParticleOptions {

    // Codec for serialization
    public static final Codec<List<Vector3f>> VECTOR3F_LIST_CODEC = ExtraCodecs.VECTOR3F.listOf();

    public static final MapCodec<DragonBreathParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(VECTOR3F_LIST_CODEC.fieldOf("gradientColors").forGetter(options -> options.gradientColors))
                    .apply(instance, DragonBreathParticleOptions::new));

    // Proper implementation of StreamCodec for network transmission
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonBreathParticleOptions> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buffer, DragonBreathParticleOptions value) {
                    // Write the size of the list
                    buffer.writeVarInt(value.gradientColors.size());
                    // Write each color
                    for (Vector3f color : value.gradientColors) {
                        buffer.writeFloat(color.x());
                        buffer.writeFloat(color.y());
                        buffer.writeFloat(color.z());
                    }
                }

                @Override
                public DragonBreathParticleOptions decode(RegistryFriendlyByteBuf buffer) {
                    int size = buffer.readVarInt();
                    List<Vector3f> colors = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        float r = buffer.readFloat();
                        float g = buffer.readFloat();
                        float b = buffer.readFloat();
                        colors.add(new Vector3f(r, g, b));
                    }
                    return new DragonBreathParticleOptions(colors);
                }
            };

    private final List<Vector3f> gradientColors;

    public DragonBreathParticleOptions(DragonBreathType breathType) {
        this.gradientColors = breathType.getGradient().stream()
                .map(c -> new Vector3f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f))
                .toList();
    }

    public DragonBreathParticleOptions(List<Vector3f> gradientColors) {
        if (gradientColors == null || gradientColors.isEmpty()) {
            throw new IllegalArgumentException("Gradient colors cannot be null or empty");
        }
        this.gradientColors = gradientColors;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.DRAGON_BREATH_PARTICLE.get();
    }
}
