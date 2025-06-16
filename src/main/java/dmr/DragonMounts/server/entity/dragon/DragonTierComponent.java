package dmr.DragonMounts.server.entity.dragon;

import static net.minecraft.world.entity.ai.attributes.Attributes.*;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.DragonTier;
import dmr.DragonMounts.util.MiscUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.Level;

abstract class DragonTierComponent extends DragonAttributeComponent {
    private static final ResourceLocation TIER_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "tier_attribute");

    protected DragonTierComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    private int tierLevel = 0;

    public DragonTier getTier() {
        return DragonTier.fromLevel(tierLevel);
    }

    public void setTier(DragonTier tier) {
        this.tierLevel = tier.getLevel();
    }

    @Override
    public Component getDisplayName() {
        var name = super.getDisplayName();
        if (!hasCustomName() && ServerConfig.ENABLE_DRAGON_TIERS) {
            var tier = getTier();
            // Add the tier name with its color
            return Component.empty()
                    .append(tier.getDisplayName())
                    .append(" ")
                    .append(name)
                    .withStyle(tier.getColor());
        }
        return name;
    }

    /**
     * Applies a tier-based modifier to the specified attribute.
     *
     * @param attribute The attribute to modify
     * @param amount The amount of the modifier
     */
    private void applyTierModifier(Holder<Attribute> attribute, double amount) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) return;

        // Remove existing tier modifier if present
        if (instance.hasModifier(TIER_MODIFIER)) {
            instance.removeModifier(TIER_MODIFIER);
        }

        // Only add modifier if amount is non-zero
        if (amount != 0) {
            AttributeModifier modifier = new AttributeModifier(TIER_MODIFIER, amount, Operation.ADD_MULTIPLIED_BASE);
            instance.addTransientModifier(modifier);
        }
    }

    @Override
    public void finalizeDragon(TameableDragonEntity parent1, TameableDragonEntity parent2) {
        super.finalizeDragon(parent1, parent2);
        if (!ServerConfig.ENABLE_DRAGON_TIERS) return;

        DragonTier tier;
        if (parent1 != null && parent2 != null) {
            // Tier level can be avg tier - 1 or highest tier
            // Will not allow breeding of higher tier than either parent
            // But if both parents are same tier, at most it can go one tier lower
            var avgTier = (parent1.getTier().getLevel() + parent2.getTier().getLevel()) / 2;
            var lowest = Math.max(0, avgTier - 1);
            var highest =
                    Math.max(parent1.getTier().getLevel(), parent2.getTier().getLevel());
            tier = DragonTier.fromLevel(MiscUtils.randomUpperLower(lowest, highest));
        } else {
            tier = DragonTier.getRandomTier();
        }

        setTier(tier);

        // Calculate scaling factors based on tier level
        // Each tier level provides progressively stronger bonuses
        float healthBonus = tierLevel * 0.1f; // +10% health per tier level
        float damageBonus = tierLevel * 0.1f; // +10% damage per tier level
        float sizeBonus = tierLevel * 0.10f; // +10% size per tier level

        // Apply modifiers to health, damage, and size
        applyTierModifier(MAX_HEALTH, healthBonus);
        applyTierModifier(ATTACK_DAMAGE, damageBonus);
        applyTierModifier(SCALE, sizeBonus);

        // If the entity already exists and has health, scale current health with max health
        if (isAlive()) {
            float healthRatio = getHealth() / getMaxHealth();
            setHealth(getMaxHealth() * healthRatio);
        }
    }
}
