package dmr.DragonMounts.types.abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Class to store data for a single effect.
 * Used by ability implementations to apply effects with tier-based scaling.
 */
@Getter
public class EffectData {
    // Default values for effect properties
    public static final int DEFAULT_DURATION = 40;
    public static final int DEFAULT_AMPLIFIER = 0;
    public static final boolean DEFAULT_AMBIENT = true;
    public static final boolean DEFAULT_SHOW_PARTICLES = false;
    public static final boolean DEFAULT_SHOW_ICON = true;
    public static final float DEFAULT_DURATION_SCALE_FACTOR = 1.0f;
    public static final float DEFAULT_AMPLIFIER_SCALE_FACTOR = 0.0f;

    // Getters
    private final Holder<MobEffect> effect;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean showParticles;
    private final boolean showIcon;

    // Tier scaling factors
    private final float durationScaleFactor;
    private final float amplifierScaleFactor;

    public EffectData(
            Holder<MobEffect> effect,
            int duration,
            int amplifier,
            boolean ambient,
            boolean showParticles,
            boolean showIcon) {
        this(
                effect,
                duration,
                amplifier,
                ambient,
                showParticles,
                showIcon,
                DEFAULT_DURATION_SCALE_FACTOR,
                DEFAULT_AMPLIFIER_SCALE_FACTOR);
    }

    public EffectData(
            Holder<MobEffect> effect,
            int duration,
            int amplifier,
            boolean ambient,
            boolean showParticles,
            boolean showIcon,
            float durationScaleFactor,
            float amplifierScaleFactor) {
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.showParticles = showParticles;
        this.showIcon = showIcon;
        this.durationScaleFactor = durationScaleFactor;
        this.amplifierScaleFactor = amplifierScaleFactor;
    }

    /**
     * Creates a list of EffectData objects from a map of properties.
     *
     * @param props The properties map from the ability definition
     * @return A list of EffectData objects
     */
    public static List<EffectData> createFromProperties(Map<String, Object> props) {
        List<EffectData> effects = new ArrayList<>();

        // Multiple effects
        if (props.containsKey("effects") && props.get("effects") instanceof List<?> effectsList) {
            for (Object effectObj : effectsList) {
                if (effectObj instanceof Map<?, ?> effectMap) {
                    if (effectMap.containsKey("id") && effectMap.get("id") instanceof String effectId) {
                        var effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectId));

                        if (effectHolder.isPresent()) {
                            int duration = effectMap.containsKey("duration")
                                    ? ((Number) effectMap.get("duration")).intValue()
                                    : DEFAULT_DURATION;
                            int amplifier = effectMap.containsKey("amplifier")
                                    ? ((Number) effectMap.get("amplifier")).intValue()
                                    : DEFAULT_AMPLIFIER;
                            boolean ambient = effectMap.containsKey("ambient")
                                    ? (Boolean) effectMap.get("ambient")
                                    : DEFAULT_AMBIENT;
                            boolean showParticles = effectMap.containsKey("show_particles")
                                    ? (Boolean) effectMap.get("show_particles")
                                    : DEFAULT_SHOW_PARTICLES;
                            boolean showIcon = effectMap.containsKey("show_icon")
                                    ? (Boolean) effectMap.get("show_icon")
                                    : DEFAULT_SHOW_ICON;

                            float durationScaleFactor = DEFAULT_DURATION_SCALE_FACTOR;
                            float amplifierScaleFactor = DEFAULT_AMPLIFIER_SCALE_FACTOR;

                            if (effectMap.containsKey("duration_scale_factor")) {
                                durationScaleFactor = ((Number) effectMap.get("duration_scale_factor")).floatValue();
                            }
                            if (effectMap.containsKey("amplifier_scale_factor")) {
                                amplifierScaleFactor = ((Number) effectMap.get("amplifier_scale_factor")).floatValue();
                            }

                            var effectData = new EffectData(
                                    effectHolder.get(),
                                    duration,
                                    amplifier,
                                    ambient,
                                    showParticles,
                                    showIcon,
                                    durationScaleFactor,
                                    amplifierScaleFactor);

                            effects.add(effectData);
                        }
                    }
                }
            }
        }

        return effects;
    }

    /**
     * Apply the effect to a player with the given tier.
     *
     * @param target The player to apply the effect to
     * @param tier The tier of the ability
     */
    public void apply(Player target, int tier) {
        if (effect == null) return;

        // Scale duration and amplifier based on tier
        int scaledDuration = (int) (duration * (1.0f + (tier - 1) * durationScaleFactor));
        int scaledAmplifier = amplifier + (int) ((tier - 1) * amplifierScaleFactor);

        target.addEffect(
                new MobEffectInstance(effect, scaledDuration, scaledAmplifier, ambient, showParticles, showIcon));
    }

    /**
     * Apply the effect to a dragon and/or its owner with the given tier.
     *
     * @param dragon The dragon to apply the effect to
     * @param owner The dragon's owner
     * @param applyToDragon Whether to apply the effect to the dragon
     * @param applyToOwner Whether to apply the effect to the owner
     * @param tier The tier of the ability
     */
    public void apply(
            TameableDragonEntity dragon, Player owner, boolean applyToDragon, boolean applyToOwner, int tier) {
        if (effect == null) return;

        // Scale duration and amplifier based on tier
        int scaledDuration = (int) (duration * (1.0f + (tier - 1) * durationScaleFactor));
        int scaledAmplifier = amplifier + (int) ((tier - 1) * amplifierScaleFactor);

        // Apply to dragon if enabled
        if (applyToDragon) {
            dragon.addEffect(
                    new MobEffectInstance(effect, scaledDuration, scaledAmplifier, ambient, showParticles, showIcon));
        }

        // Apply to owner if enabled
        if (applyToOwner && owner != null) {
            owner.addEffect(
                    new MobEffectInstance(effect, scaledDuration, scaledAmplifier, ambient, showParticles, showIcon));
        }
    }
}
