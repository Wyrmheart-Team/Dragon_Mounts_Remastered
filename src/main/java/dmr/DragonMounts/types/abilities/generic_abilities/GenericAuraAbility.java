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
import net.minecraft.world.entity.LivingEntity;

/**
 * A generic ability that applies effects to entities in an area around the dragon.
 */
public class GenericAuraAbility extends Ability {
    private Holder<MobEffect> effect;
    private int duration = 40;
    private int amplifier = 0;
    private int radius = 5;
    private boolean affectOwner = false;
    private boolean affectAllies = true;
    private boolean affectEnemies = false;

    public GenericAuraAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
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
        if (props.containsKey("radius")) {
            radius = ((Number) props.get("radius")).intValue();
        }
        // Only support snake_case
        if (props.containsKey("affect_owner")) {
            affectOwner = (Boolean) props.get("affect_owner");
        }
        if (props.containsKey("affect_allies")) {
            affectAllies = (Boolean) props.get("affect_allies");
        }
        if (props.containsKey("affect_enemies")) {
            affectEnemies = (Boolean) props.get("affect_enemies");
        }
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        if (effect == null || dragon.level.isClientSide) {
            return;
        }

        // Get entities in range
        List<LivingEntity> entities = dragon.level.getEntitiesOfClass(
                LivingEntity.class,
                dragon.getBoundingBox().inflate(radius),
                entity -> entity != dragon && isValidTarget(dragon, entity));

        // Apply effect to entities
        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private boolean isValidTarget(TameableDragonEntity dragon, LivingEntity entity) {
        if (entity == dragon.getOwner()) {
            return affectOwner;
        }

        if (dragon.isAlliedTo(entity)) {
            return affectAllies;
        }

        return affectEnemies;
    }
}
