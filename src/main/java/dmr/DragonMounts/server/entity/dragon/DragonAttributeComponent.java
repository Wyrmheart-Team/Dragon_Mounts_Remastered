package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.ModAttributes;
import dmr.DragonMounts.server.entity.DragonConstants;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dmr.DragonMounts.util.MiscUtils.randomUpperLower;
import static dmr.DragonMounts.util.MiscUtils.upperLower;
import static net.minecraft.world.entity.ai.attributes.Attributes.*;
import static net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED;

abstract class DragonAttributeComponent extends DragonSpawnComponent {
    protected DragonAttributeComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        entityData.set(healthAttribute, (float) Math.random());
        entityData.set(speedAttribute, (float) Math.random());
        entityData.set(damageAttribute, (float) Math.random());
        entityData.set(maxScaleAttribute, (float) Math.random());
    }

    public static final EntityDataAccessor<Float> healthAttribute =
            SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> speedAttribute =
            SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> damageAttribute =
            SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> maxScaleAttribute =
            SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(healthAttribute, 0f);
        builder.define(speedAttribute, 0f);
        builder.define(damageAttribute, 0f);
        builder.define(maxScaleAttribute, 0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(MOVEMENT_SPEED, DragonConstants.BASE_SPEED_GROUND)
                .add(MAX_HEALTH, DragonConstants.BASE_HEALTH)
                .add(FOLLOW_RANGE, DragonConstants.BASE_FOLLOW_RANGE)
                .add(KNOCKBACK_RESISTANCE, DragonConstants.BASE_KB_RESISTANCE)
                .add(ATTACK_DAMAGE, DragonConstants.BASE_DAMAGE)
                .add(FLYING_SPEED, DragonConstants.BASE_SPEED_FLYING)
                .add(SWIM_SPEED, DragonConstants.BASE_SPEED_WATER)
                .add(ModAttributes.BREATH_DAMAGE, 1)
                .add(ModAttributes.BITE_DAMAGE, 1)
                .add(ModAttributes.BREATH_COOLDOWN, 0);
    }

    private static final ResourceLocation SCALE_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "scale_attribute");

    private static final ResourceLocation RANDOM_STATS_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "random_stats_attribute");

    public void tick() {
        super.tick();

        setBaseValue(MAX_HEALTH, ServerConfig.BASE_HEALTH);
        setBaseValue(ATTACK_DAMAGE, ServerConfig.BASE_DAMAGE);
        setBaseValue(MOVEMENT_SPEED, DragonConstants.BASE_SPEED_GROUND * ServerConfig.BASE_SPEED);
    }

    public void updateAgeAttributes() {
        setBaseValue(STEP_HEIGHT, Math.max(2 * getAgeProgress(), 1));

        var mod = new AttributeModifier(SCALE_MODIFIER, getScale(), Operation.ADD_VALUE);
        var attributes = List.of(MAX_HEALTH, ATTACK_DAMAGE);

        for (var attribute : attributes) {
            AttributeInstance instance = getAttribute(attribute);
            if (instance == null) continue;

            if (!instance.hasModifier(SCALE_MODIFIER)
                    || instance.getModifier(SCALE_MODIFIER).amount() != mod.amount()) {
                if (instance.hasModifier(SCALE_MODIFIER)) instance.removeModifier(SCALE_MODIFIER);
                instance.addTransientModifier(mod);
            }
        }
    }

    private void setBaseValue(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) return;
        if (instance.getBaseValue() != value) instance.setBaseValue(value);
    }

    @Override
    public void finalizeDragon(@Nullable TameableDragonEntity parent1, @Nullable TameableDragonEntity parent2) {
        super.finalizeDragon(parent1, parent2);

        float healthPercentile = getHealth() / getMaxHealth();

        getBreed().getAttributes().forEach((att, value) -> {
            Optional<Reference<Attribute>> attr = BuiltInRegistries.ATTRIBUTE.getHolder(att);
            attr.ifPresent(attributeReference -> setBaseValue(attributeReference, value));
        });

        setHealth(getMaxHealth() * healthPercentile); // in case we have less than max health

        if (ServerConfig.ENABLE_RANDOM_STATS) {
            float lowestHealth = 0, highestHealth = 1f;
            float lowestSpeed = 0, highestSpeed = 1f;
            float lowestDamage = 0f, highestDamage = 1f;
            float lowestScale = 0f, highestScale = 1f;

            if (parent1 != null && parent2 != null) {
                Function<EntityDataAccessor<Float>, Float> min =
                        (data) -> Math.min(parent1.entityData.get(data), parent2.entityData.get(data));
                Function<EntityDataAccessor<Float>, Float> max =
                        (data) -> Math.max(parent1.entityData.get(data), parent2.entityData.get(data));

                lowestHealth = min.apply(healthAttribute);
                highestHealth = max.apply(healthAttribute);

                lowestSpeed = min.apply(speedAttribute);
                highestSpeed = max.apply(speedAttribute);

                lowestDamage = min.apply(damageAttribute);
                highestDamage = max.apply(damageAttribute);

                lowestScale = min.apply(maxScaleAttribute);
                highestScale = max.apply(maxScaleAttribute);
            }

            var healthValue = randomUpperLower(lowestHealth, highestHealth);
            var speedValue = randomUpperLower(lowestSpeed, highestSpeed);
            var damageValue = randomUpperLower(lowestDamage, highestDamage);
            var scaleValue = randomUpperLower(lowestScale, highestScale);

            entityData.set(healthAttribute, healthValue);
            entityData.set(speedAttribute, speedValue);
            entityData.set(damageAttribute, damageValue);
            entityData.set(maxScaleAttribute, scaleValue);

            var randomStatsHealth = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(
                            entityData.get(healthAttribute),
                            ServerConfig.LOWER_MAX_HEALTH,
                            ServerConfig.UPPER_MAX_HEALTH),
                    Operation.ADD_VALUE);
            var healthInstance = getAttribute(MAX_HEALTH);
            healthInstance.addTransientModifier(randomStatsHealth);

            var randomStatsDamage = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(entityData.get(damageAttribute), ServerConfig.LOWER_DAMAGE, ServerConfig.UPPER_DAMAGE),
                    Operation.ADD_VALUE);
            var damageInstance = getAttribute(ATTACK_DAMAGE);
            damageInstance.addTransientModifier(randomStatsDamage);

            var randomStatsSpeed = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(entityData.get(speedAttribute), ServerConfig.LOWER_SPEED, ServerConfig.UPPER_SPEED),
                    Operation.ADD_VALUE);
            var speedInstance = getAttribute(MOVEMENT_SPEED);
            speedInstance.addTransientModifier(randomStatsSpeed);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putFloat("healthAttribute", entityData.get(healthAttribute));
        compound.putFloat("speedAttribute", entityData.get(speedAttribute));
        compound.putFloat("damageAttribute", entityData.get(damageAttribute));
        compound.putFloat("maxScaleAttribute", entityData.get(maxScaleAttribute));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if(compound.contains("healthAttribute")) {
            entityData.set(healthAttribute, compound.getFloat("healthAttribute"));
        }
        
        if(compound.contains("speedAttribute")) {
            entityData.set(speedAttribute, compound.getFloat("speedAttribute"));
        }
        
        if(compound.contains("damageAttribute")) {
            entityData.set(damageAttribute, compound.getFloat("damageAttribute"));
        }
        
        if(compound.contains("maxScaleAttribute")) {
            entityData.set(maxScaleAttribute, compound.getFloat("maxScaleAttribute"));
        }
    }
}
