package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.server.entity.DragonConstants;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.core.Holder;
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

import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.world.entity.ai.attributes.Attributes.*;
import static net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED;

abstract class DragonAttributeComponent extends DragonSpawnComponent {
    protected DragonAttributeComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }
    
    public static final EntityDataAccessor<Float> healthAttribute = SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> speedAttribute = SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> damageAttribute = SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> maxScaleAttribute = SynchedEntityData.defineId(DragonAttributeComponent.class, EntityDataSerializers.FLOAT);
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(healthAttribute, (float)Math.random());
        builder.define(speedAttribute, (float)Math.random());
        builder.define(damageAttribute, (float)Math.random());
        builder.define(maxScaleAttribute, (float)Math.random());
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(MOVEMENT_SPEED, DragonConstants.BASE_SPEED_GROUND)
                .add(MAX_HEALTH, DragonConstants.BASE_HEALTH)
                .add(FOLLOW_RANGE, DragonConstants.BASE_FOLLOW_RANGE)
                .add(KNOCKBACK_RESISTANCE, DragonConstants.BASE_KB_RESISTANCE)
                .add(ATTACK_DAMAGE, DragonConstants.BASE_DAMAGE)
                .add(FLYING_SPEED, DragonConstants.BASE_SPEED_FLYING)
                .add(SWIM_SPEED, DragonConstants.BASE_SPEED_WATER);
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

        setRandomStats();
    }

    public void setRandomStats() {
        {
            var randomStatsHealth = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(entityData.get(healthAttribute), ServerConfig.LOWER_MAX_HEALTH, ServerConfig.UPPER_MAX_HEALTH),
                    Operation.ADD_VALUE);
            var healthInstance = getAttribute(MAX_HEALTH);

            if(!healthInstance.hasModifier(RANDOM_STATS_MODIFIER) || healthInstance.getModifier(RANDOM_STATS_MODIFIER).amount() != randomStatsHealth.amount()) {
                if (healthInstance.hasModifier(RANDOM_STATS_MODIFIER)) healthInstance.removeModifier(RANDOM_STATS_MODIFIER);
                healthInstance.addTransientModifier(randomStatsHealth);
            }
        }

        {
            var randomStatsDamage = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(entityData.get(damageAttribute), ServerConfig.LOWER_DAMAGE, ServerConfig.UPPER_DAMAGE),
                    Operation.ADD_VALUE);
            var damageInstance = getAttribute(ATTACK_DAMAGE);
            
            if(!damageInstance.hasModifier(RANDOM_STATS_MODIFIER) || damageInstance.getModifier(RANDOM_STATS_MODIFIER).amount() != randomStatsDamage.amount()) {
                if (damageInstance.hasModifier(RANDOM_STATS_MODIFIER)) damageInstance.removeModifier(RANDOM_STATS_MODIFIER);
                damageInstance.addTransientModifier(randomStatsDamage);
            }
        }

        {
            var randomStatsSpeed = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(entityData.get(speedAttribute), ServerConfig.LOWER_SPEED, ServerConfig.UPPER_SPEED),
                    Operation.ADD_VALUE);
            var speedInstance = getAttribute(MOVEMENT_SPEED);
            
            if(!speedInstance.hasModifier(RANDOM_STATS_MODIFIER) || speedInstance.getModifier(RANDOM_STATS_MODIFIER).amount() != randomStatsSpeed.amount()) {
                if (speedInstance.hasModifier(RANDOM_STATS_MODIFIER)) speedInstance.removeModifier(RANDOM_STATS_MODIFIER);
                speedInstance.addTransientModifier(randomStatsSpeed);
            }
        }
    }

    public void updateAgeAttributes() {
        setBaseValue(STEP_HEIGHT, Math.max(2 * getAgeProgress(), 1));

        var mod = new AttributeModifier(SCALE_MODIFIER, getScale(), Operation.ADD_VALUE);
        var attributes = List.of(MAX_HEALTH, ATTACK_DAMAGE);

        for (var attribute : attributes) {
            AttributeInstance instance = getAttribute(attribute);
            if (instance == null) continue;

            if(!instance.hasModifier(SCALE_MODIFIER) || instance.getModifier(SCALE_MODIFIER).amount() != mod.amount()) {
                if(instance.hasModifier(SCALE_MODIFIER)) instance.removeModifier(SCALE_MODIFIER);
                instance.addTransientModifier(mod);
            }
        }
    }

    private void setBaseValue(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) return;
        if(instance.getBaseValue() != value) instance.setBaseValue(value);
    }

    public void setEggBreedAttributes(TameableDragonEntity mate, Supplier<DMREggBlockEntity> eggBlockEntitySupplier) {
        var lowestHealth = Math.min(entityData.get(healthAttribute), mate.entityData.get(healthAttribute));
        var highestHealth = Math.max(entityData.get(healthAttribute), mate.entityData.get(healthAttribute));
        eggBlockEntitySupplier.get().setHealthAttribute(randomUpperLower(lowestHealth, highestHealth));

        var lowestSpeed = Math.min(entityData.get(speedAttribute), mate.entityData.get(speedAttribute));
        var highestSpeed = Math.max(entityData.get(speedAttribute), mate.entityData.get(speedAttribute));
        eggBlockEntitySupplier.get().setSpeedAttribute(randomUpperLower(lowestSpeed, highestSpeed));

        var lowestDamage = Math.min(entityData.get(damageAttribute), mate.entityData.get(damageAttribute));
        var highestDamage = Math.max(entityData.get(damageAttribute), mate.entityData.get(damageAttribute));
        eggBlockEntitySupplier.get().setDamageAttribute(randomUpperLower(lowestDamage, highestDamage));
    }
    
    public void setHatchedAttributes(DMREggBlockEntity eggBlockEntity) {
        entityData.set(healthAttribute, (float)eggBlockEntity.getHealthAttribute());
        entityData.set(speedAttribute, (float)eggBlockEntity.getSpeedAttribute());
        entityData.set(damageAttribute, (float)eggBlockEntity.getDamageAttribute());
        entityData.set(maxScaleAttribute, (float)eggBlockEntity.getMaxScaleAttribute());
    }
    
    private int upperLower(double value, int lower, int upper) {
        return (int)Math.round((value * ((double)upper - (double)lower)) + (double)lower);
    }
    
    private double randomUpperLower(int lower, int upper) {
        return upperLower(Math.random(), lower, upper);
    }
    
    private double upperLower(double value, double lower, double upper) {
        return (value * (upper - lower)) + lower;
    }

    private double randomUpperLower(double lower, double upper) {
        return upperLower(Math.random(), lower, upper);
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

        entityData.set(healthAttribute, compound.getFloat("healthAttribute"));
        entityData.set(speedAttribute, compound.getFloat("speedAttribute"));
        entityData.set(damageAttribute, compound.getFloat("damageAttribute"));
        entityData.set(maxScaleAttribute, compound.getFloat("maxScaleAttribute"));
    }
}
