package dmr.DragonMounts.types.breath;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.types.DatapackEntry;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@Getter
public class DragonBreathType extends DatapackEntry {

    @SerializedName("colors")
    private List<String> colorHexCodes = new ArrayList<>();

    @SerializedName("damage")
    private float damage = 2.0f;

    @SerializedName("fire_time")
    private int fireTime = 0;

    @SerializedName("particle_density")
    private int particleDensity = 3;

    @SerializedName("effects")
    private List<BreathEffect> effects = new ArrayList<>();

    public Component getName() {
        return Component.translatable(DMR.MOD_ID + ".dragon_breath." + getId());
    }

    public List<Color> getGradient() {
        List<Color> colors = new ArrayList<>();
        for (String hex : colorHexCodes) {
            colors.add(Color.decode("#" + hex));
        }
        return colors;
    }

    /**
     * Gets a custom damage source for this breath type.
     *
     * @param source The entity causing the damage (the dragon)
     * @return A damage source appropriate for this breath type
     */
    public DamageSource getDamageSource(LivingEntity source) {
        // TODO: Use the appropriate damage source based on the breath type
        // For now, we'll use dragonBreath for all breath types, but this could be expanded
        // to use different damage sources based on the breath type (fire, ice, etc.)
        return source.level().damageSources().dragonBreath();
    }

    @Getter
    public static class BreathEffect {
        @SerializedName("effect")
        private String effectId;

        @SerializedName("duration")
        private int duration;

        @SerializedName("amplifier")
        private int amplifier;

        @SerializedName("chance")
        private float chance = 1.0f;
    }
}
