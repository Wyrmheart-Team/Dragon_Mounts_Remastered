package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;

/**
 * A generic ability that applies effects to monsters in an area around the dragon.
 * Can be configured to apply a potion effect or set monsters on fire.
 */
public class GenericMonsterAuraAbility extends Ability {
    private Holder<MobEffect> effect;
    private int duration = 40;
    private int amplifier = 0;
    private double range = 10.0;
    private boolean ignoreLineOfSight = true;
    private int fireTicks = 0;
    private boolean requiresNoFire = false;
    private boolean requiresNotFireImmune = false;
    private boolean requiresNotInWaterRainOrBubble = false;

    public GenericMonsterAuraAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        Map<String, Object> props = definition.getProperties();

        // Get effect from properties
        if (props.containsKey("effect")) {
            String effectId = (String) props.get("effect");
            var effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectId));
            effectHolder.ifPresent(mobEffectReference -> this.effect = mobEffectReference);
        }

        // Get other properties
        if (props.containsKey("duration")) {
            duration = ((Number) props.get("duration")).intValue();
        }
        if (props.containsKey("amplifier")) {
            amplifier = ((Number) props.get("amplifier")).intValue();
        }
        if (props.containsKey("range")) {
            range = ((Number) props.get("range")).doubleValue();
        }
        if (props.containsKey("ignore_line_of_sight")) {
            ignoreLineOfSight = (Boolean) props.get("ignore_line_of_sight");
        }
        if (props.containsKey("fire_ticks")) {
            fireTicks = ((Number) props.get("fire_ticks")).intValue();
        }
        if (props.containsKey("requires_no_fire")) {
            requiresNoFire = (Boolean) props.get("requires_no_fire");
        }
        if (props.containsKey("requires_not_fire_immune")) {
            requiresNotFireImmune = (Boolean) props.get("requires_not_fire_immune");
        }
        if (props.containsKey("requires_not_in_water_rain_or_bubble")) {
            requiresNotInWaterRainOrBubble = (Boolean) props.get("requires_not_in_water_rain_or_bubble");
        }
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        if (dragon.level.isClientSide) {
            return;
        }

        // Create targeting conditions
        TargetingConditions conditions = TargetingConditions.forCombat().range(range);
        if (ignoreLineOfSight) {
            conditions = conditions.ignoreLineOfSight();
        }

        // Get monsters in range
        List<Monster> monsters = dragon.level.getNearbyEntities(
                Monster.class, conditions, dragon, dragon.getBoundingBox().inflate(range, range, range));

        // Apply effect to monsters
        for (Monster monster : monsters) {
            // Check fire-related conditions if setting on fire
            if (fireTicks > 0) {
                if (requiresNoFire && monster.isOnFire()) {
                    continue;
                }
                if (requiresNotFireImmune && monster.fireImmune()) {
                    continue;
                }
                if (requiresNotInWaterRainOrBubble && monster.isInWaterRainOrBubble()) {
                    continue;
                }
                monster.setRemainingFireTicks(fireTicks);
            }

            // Apply potion effect if specified
            if (effect != null) {
                monster.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
            }
        }
    }
}
