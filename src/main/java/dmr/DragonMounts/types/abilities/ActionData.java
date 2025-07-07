package dmr.DragonMounts.types.abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.*;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Represents an action that can be performed by an ability.
 * Supports effects, attribute modifiers, healing, damage, and item giving.
 * Uses simple, user-friendly JSON format.
 */
@Getter
public class ActionData {
    public enum ActionType {
        EFFECT,
        ATTRIBUTE,
        HEAL,
        DAMAGE,
        GIVE_ITEM,
        SET_FIRE
    }

    public enum TargetType {
        DRAGON,
        OWNER,
        TARGET, // Event target (for event-triggered abilities)
        MONSTERS, // Hostile mobs in range
        ALL_ENTITIES, // All entities in range
        PASSENGERS // Dragon's passengers
    }

    private final ActionType type;
    private final Map<String, Object> parameters;
    private final Set<TargetType> targets;
    private final float tierScaleFactor;

    public ActionData(ActionType type, Map<String, Object> parameters, Set<TargetType> targets, float tierScaleFactor) {
        this.type = type;
        this.parameters = parameters;
        this.targets = targets;
        this.tierScaleFactor = tierScaleFactor;
    }

    /**
     * Creates a list of ActionData objects from a direct actions list.
     * Format: [{"effect": "minecraft:speed", "targets": ["dragon", "owner"]}]
     */
    public static List<ActionData> createFromActionsList(List<Map<String, Object>> actionsList) {
        List<ActionData> actions = new ArrayList<>();

        for (Map<String, Object> actionProps : actionsList) {
            Set<TargetType> targets = parseTargets(actionProps);

            // Determine action type and create appropriate ActionData
            ActionData action = null;

            if (actionProps.containsKey("effect")) {
                action = createEffectAction(actionProps, targets);
            } else if (actionProps.containsKey("attribute")) {
                action = createAttributeAction(actionProps, targets);
            } else if (actionProps.containsKey("heal")) {
                action = createHealAction(actionProps, targets);
            } else if (actionProps.containsKey("damage")) {
                action = createDamageAction(actionProps, targets);
            } else if (actionProps.containsKey("give_item")) {
                action = createGiveItemAction(actionProps, targets);
            } else if (actionProps.containsKey("set_fire")) {
                action = createSetFireAction(actionProps, targets);
            }

            if (action != null) {
                actions.add(action);
            }
        }

        return actions;
    }

    @SuppressWarnings("unchecked")
    private static Set<TargetType> parseTargets(Map<String, Object> actionProps) {
        Set<TargetType> targets = new HashSet<>();

        if (actionProps.containsKey("targets")) {
            Object targetsObj = actionProps.get("targets");
            if (targetsObj instanceof List<?> targetsList) {
                for (Object target : targetsList) {
                    if (target instanceof String targetStr) {
                        try {
                            targets.add(TargetType.valueOf(targetStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            // Invalid target type, ignore
                        }
                    }
                }
            } else if (targetsObj instanceof String targetStr) {
                // Single target as string
                try {
                    targets.add(TargetType.valueOf(targetStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Invalid target type, ignore
                }
            }
        }

        // Default to dragon if no targets specified
        if (targets.isEmpty()) {
            targets.add(TargetType.DRAGON);
        }

        return targets;
    }

    private static ActionData createEffectAction(Map<String, Object> props, Set<TargetType> targets) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("effect", props.get("effect"));
        parameters.put("duration", props.getOrDefault("duration", 40));
        parameters.put("amplifier", props.getOrDefault("amplifier", 0));
        parameters.put("ambient", props.getOrDefault("ambient", true));
        parameters.put("show_particles", props.getOrDefault("show_particles", false));
        parameters.put("show_icon", props.getOrDefault("show_icon", true));

        float tierScaleFactor = ((Number) props.getOrDefault("duration_scale_factor", 1.0f)).floatValue();

        return new ActionData(ActionType.EFFECT, parameters, targets, tierScaleFactor);
    }

    private static ActionData createSetFireAction(Map<String, Object> props, Set<TargetType> targets) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fire_seconds", props.getOrDefault("fire_seconds", 3));
        parameters.put("check_immunity", props.getOrDefault("check_immunity", true));

        float tierScaleFactor = ((Number) props.getOrDefault("fire_scale_factor", 1.0f)).floatValue();

        return new ActionData(ActionType.SET_FIRE, parameters, targets, tierScaleFactor);
    }

    @SuppressWarnings("unchecked")
    private static ActionData createAttributeAction(Map<String, Object> props, Set<TargetType> targets) {
        Object attributeObj = props.get("attribute");
        Map<String, Object> parameters = new HashMap<>();

        if (attributeObj instanceof String attributeId) {
            // Simple format: "attribute": "minecraft:generic.max_health"
            parameters.put("attribute", attributeId);
            parameters.put("value", props.getOrDefault("attribute_value", 1.0));
            parameters.put("operation", props.getOrDefault("attribute_operation", "ADD_VALUE"));
        } else if (attributeObj instanceof Map<?, ?> attributeMap) {
            // Complex format: "attribute": {"id": "minecraft:generic.max_health", "value": 20}
            parameters.put("attribute", attributeMap.get("id"));
            parameters.put("value", attributeMap.containsKey("value") ? attributeMap.get("value") : 1.0);
            parameters.put(
                    "operation", attributeMap.containsKey("operation") ? attributeMap.get("operation") : "ADD_VALUE");
        } else {
            return null;
        }

        float tierScaleFactor = ((Number) props.getOrDefault("attribute_scale_factor", 1.0f)).floatValue();

        return new ActionData(ActionType.ATTRIBUTE, parameters, targets, tierScaleFactor);
    }

    private static ActionData createHealAction(Map<String, Object> props, Set<TargetType> targets) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("amount", props.get("heal"));

        float tierScaleFactor = ((Number) props.getOrDefault("heal_scale_factor", 1.0f)).floatValue();

        return new ActionData(ActionType.HEAL, parameters, targets, tierScaleFactor);
    }

    private static ActionData createDamageAction(Map<String, Object> props, Set<TargetType> targets) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("amount", props.get("damage"));

        float tierScaleFactor = ((Number) props.getOrDefault("damage_scale_factor", 1.0f)).floatValue();

        return new ActionData(ActionType.DAMAGE, parameters, targets, tierScaleFactor);
    }

    @SuppressWarnings("unchecked")
    private static ActionData createGiveItemAction(Map<String, Object> props, Set<TargetType> targets) {
        Object giveItemObj = props.get("give_item");
        Map<String, Object> parameters = new HashMap<>();

        if (giveItemObj instanceof String itemId) {
            // Simple format: "give_item": "minecraft:diamond"
            parameters.put("item", itemId);
            parameters.put("count", props.getOrDefault("item_count", 1));
        } else if (giveItemObj instanceof Map<?, ?> itemMap) {
            // Complex format: "give_item": {"item": "minecraft:diamond", "count": 1}
            parameters.put("item", itemMap.get("item"));
            parameters.put("count", itemMap.containsKey("count") ? itemMap.get("count") : 1);
        } else {
            return null;
        }

        float tierScaleFactor = ((Number) props.getOrDefault("item_scale_factor", 1.0f)).floatValue();

        return new ActionData(ActionType.GIVE_ITEM, parameters, targets, tierScaleFactor);
    }

    /**
     * Execute this action on the appropriate targets.
     */
    public void execute(TameableDragonEntity dragon, Player owner, Object eventTarget, int tier, double range) {
        for (TargetType targetType : targets) {
            switch (targetType) {
                case DRAGON -> executeOnTarget(dragon, tier, dragon);
                case OWNER -> {
                    if (owner != null) executeOnTarget(owner, tier, dragon);
                }
                case TARGET -> {
                    if (eventTarget instanceof LivingEntity target) executeOnTarget(target, tier, dragon);
                }
                case MONSTERS -> {
                    // Find and apply to hostile mobs in range
                    dragon.level()
                            .getEntitiesOfClass(
                                    Mob.class,
                                    dragon.getBoundingBox().inflate(range),
                                    mob -> mob.isAlive()
                                            && !mob.isAlliedTo(dragon)
                                            && mob.getType().getCategory().isFriendly() == false)
                            .forEach(mob -> executeOnTarget(mob, tier, dragon));
                }
                case ALL_ENTITIES -> {
                    // Apply to all living entities in range
                    dragon.level()
                            .getEntitiesOfClass(
                                    LivingEntity.class,
                                    dragon.getBoundingBox().inflate(range),
                                    entity -> entity.isAlive() && entity != dragon)
                            .forEach(entity -> executeOnTarget(entity, tier, dragon));
                }
                case PASSENGERS -> {
                    // Apply to all passengers
                    dragon.getPassengers().stream()
                            .filter(entity -> entity instanceof LivingEntity)
                            .map(entity -> (LivingEntity) entity)
                            .forEach(entity -> executeOnTarget(entity, tier, dragon));
                }
            }
        }
    }

    private void executeOnTarget(LivingEntity target, int tier, TameableDragonEntity dragon) {
        switch (type) {
            case EFFECT -> executeEffect(target, tier, dragon);
            case ATTRIBUTE -> executeAttribute(target, tier);
            case HEAL -> executeHeal(target, tier);
            case DAMAGE -> executeDamage(target, tier);
            case GIVE_ITEM -> executeGiveItem(target, tier);
            case SET_FIRE -> executeSetFire(target, tier);
        }
    }

    private void executeEffect(LivingEntity target, int tier, TameableDragonEntity dragon) {
        String effectId = (String) parameters.get("effect");
        var effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectId));
        if (effectHolder.isEmpty()) return;

        int duration = ((Number) parameters.get("duration")).intValue();
        int amplifier = ((Number) parameters.get("amplifier")).intValue();
        boolean ambient = (Boolean) parameters.get("ambient");
        boolean showParticles = (Boolean) parameters.get("show_particles");
        boolean showIcon = (Boolean) parameters.get("show_icon");

        // Scale with tier
        int scaledDuration = (int) (duration * (1.0f + (tier - 1) * tierScaleFactor));
        int scaledAmplifier = amplifier + (int) ((tier - 1) * tierScaleFactor * 0.5f);

        MobEffectInstance effectInstance = new MobEffectInstance(
                effectHolder.get(), scaledDuration, scaledAmplifier, ambient, showParticles, showIcon);

        target.addEffect(effectInstance, dragon);
    }

    private void executeAttribute(LivingEntity target, int tier) {
        String attributeId = (String) parameters.get("attribute");
        double value = ((Number) parameters.get("value")).doubleValue();
        String operationStr = (String) parameters.get("operation");
        boolean isPermanent = (Boolean) parameters.getOrDefault("permanent", false);

        var attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(attributeId));
        if (attributeHolder.isEmpty()) return;

        // Scale with tier
        double scaledValue = value * (1.0 + (tier - 1) * tierScaleFactor);

        AttributeModifier.Operation operation;
        try {
            operation = AttributeModifier.Operation.valueOf(operationStr);
        } catch (IllegalArgumentException e) {
            operation = AttributeModifier.Operation.ADD_VALUE;
        }

        ResourceLocation modifierId = ResourceLocation.parse("dmr:ability_" + attributeId.replace(":", "_"));
        AttributeModifier modifier = new AttributeModifier(modifierId, scaledValue, operation);

        var attribute = target.getAttribute(attributeHolder.get());
        if (attribute != null) {
            // Remove any existing modifier with the same ID first
            attribute.removeModifier(modifierId);

            if (isPermanent) {
                attribute.addPermanentModifier(modifier);
            } else {
                attribute.addTransientModifier(modifier);
            }
        }
    }

    /**
     * Remove attribute modifiers from a target (for cleanup when conditions end).
     */
    public void removeAttributeModifiers(LivingEntity target) {
        if (type != ActionType.ATTRIBUTE) return;

        String attributeId = (String) parameters.get("attribute");
        var attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(attributeId));
        if (attributeHolder.isEmpty()) return;

        var attribute = target.getAttribute(attributeHolder.get());
        if (attribute != null) {
            ResourceLocation modifierId = ResourceLocation.parse("dmr:ability_" + attributeId.replace(":", "_"));
            attribute.removeModifier(modifierId);
        }
    }

    private void executeHeal(LivingEntity target, int tier) {
        float amount = ((Number) parameters.get("amount")).floatValue();

        // Scale with tier
        float scaledAmount = amount * (1.0f + (tier - 1) * tierScaleFactor);

        target.heal(scaledAmount);
    }

    private void executeDamage(LivingEntity target, int tier) {
        float amount = ((Number) parameters.get("amount")).floatValue();

        // Scale with tier
        float scaledAmount = amount * (1.0f + (tier - 1) * tierScaleFactor);

        target.hurt(target.damageSources().generic(), scaledAmount);
    }

    private void executeGiveItem(LivingEntity target, int tier) {
        if (!(target instanceof Player player)) return;

        String itemId = (String) parameters.get("item");
        int count = ((Number) parameters.get("count")).intValue();

        var itemHolder = BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse(itemId));
        if (itemHolder.isEmpty()) return;

        // Scale with tier
        int scaledCount = Math.max(1, (int) (count * (1.0 + (tier - 1) * tierScaleFactor)));

        ItemStack stack = new ItemStack(itemHolder.get(), scaledCount);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void executeSetFire(LivingEntity target, int tier) {
        int fireSeconds = ((Number) parameters.get("fire_seconds")).intValue();
        boolean checkImmunity = (Boolean) parameters.getOrDefault("check_immunity", true);

        // Scale with tier
        int scaledSeconds = (int) (fireSeconds * (1.0f + (tier - 1) * tierScaleFactor));

        if (!checkImmunity || !target.fireImmune()) {
            target.setRemainingFireTicks(scaledSeconds * 20);
        }
    }
}
