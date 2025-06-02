package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.server.entity.DragonConstants;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
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

    @Getter
    @Setter
    private double healthAttribute;

    @Getter
    @Setter
    private double speedAttribute;

    @Getter
    @Setter
    private double damageAttribute;

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
                    upperLower(getHealthAttribute(), ServerConfig.LOWER_MAX_HEALTH, ServerConfig.UPPER_MAX_HEALTH),
                    Operation.ADD_VALUE);
            var healthInstance = getAttribute(MAX_HEALTH);

            if (healthInstance.hasModifier(RANDOM_STATS_MODIFIER)) healthInstance.removeModifier(RANDOM_STATS_MODIFIER);
            healthInstance.addTransientModifier(randomStatsHealth);
        }

        {
            var randomStatsDamage = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(getDamageAttribute(), ServerConfig.LOWER_DAMAGE, ServerConfig.UPPER_DAMAGE),
                    Operation.ADD_VALUE);
            var damageInstance = getAttribute(ATTACK_DAMAGE);

            if (damageInstance.hasModifier(RANDOM_STATS_MODIFIER)) damageInstance.removeModifier(RANDOM_STATS_MODIFIER);
            damageInstance.addTransientModifier(randomStatsDamage);
        }

        {
            var randomStatsSpeed = new AttributeModifier(
                    RANDOM_STATS_MODIFIER,
                    upperLower(getSpeedAttribute(), ServerConfig.LOWER_SPEED, ServerConfig.UPPER_SPEED),
                    Operation.ADD_VALUE);
            var speedInstance = getAttribute(MOVEMENT_SPEED);

            if (speedInstance.hasModifier(RANDOM_STATS_MODIFIER)) speedInstance.removeModifier(RANDOM_STATS_MODIFIER);
            speedInstance.addTransientModifier(randomStatsSpeed);
        }
    }

    public void updateAgeAttributes() {
        setBaseValue(STEP_HEIGHT, Math.max(2 * getAgeProgress(), 1));

        var mod = new AttributeModifier(SCALE_MODIFIER, getScale(), Operation.ADD_VALUE);
        var attributes = List.of(MAX_HEALTH, ATTACK_DAMAGE);

        for (var attribute : attributes) {
            AttributeInstance instance = getAttribute(attribute);
            if (instance == null) continue;

            instance.removeModifier(SCALE_MODIFIER);
            instance.addTransientModifier(mod);
        }
    }

    private void setBaseValue(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) return;
        instance.setBaseValue(value);
    }

    public void setEggBreedAttributes(TameableDragonEntity mate, Supplier<DMREggBlockEntity> eggBlockEntitySupplier) {
        var lowestHealth = Math.min(getHealthAttribute(), mate.getHealthAttribute());
        var highestHealth = Math.max(getHealthAttribute(), mate.getHealthAttribute());
        eggBlockEntitySupplier.get().setHealthAttribute(randomUpperLower(lowestHealth, highestHealth));

        var lowestSpeed = Math.min(getSpeedAttribute(), mate.getSpeedAttribute());
        var highestSpeed = Math.max(getSpeedAttribute(), mate.getSpeedAttribute());
        eggBlockEntitySupplier.get().setSpeedAttribute(randomUpperLower(lowestSpeed, highestSpeed));

        var lowestDamage = Math.min(getDamageAttribute(), mate.getDamageAttribute());
        var highestDamage = Math.max(getDamageAttribute(), mate.getDamageAttribute());
        eggBlockEntitySupplier.get().setDamageAttribute(randomUpperLower(lowestDamage, highestDamage));
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

        compound.putDouble("healthAttribute", healthAttribute);
        compound.putDouble("speedAttribute", speedAttribute);
        compound.putDouble("damageAttribute", damageAttribute);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        healthAttribute = compound.getDouble("healthAttribute");
        speedAttribute = compound.getDouble("speedAttribute");
        damageAttribute = compound.getDouble("damageAttribute");
    }
}
