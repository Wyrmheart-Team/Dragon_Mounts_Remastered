package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.ActionData;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

/**
 * A generic ability that applies actions to the dragon and/or its owner
 * when a specific condition is met. Uses the new ActionData system.
 * Handles proper cleanup of attribute modifiers when conditions change.
 */
public class GenericConditionalEffectAbility extends GenericActionAbility {
    private String conditionType = "none";
    private int natureCheckRadius = 2;
    private boolean wasConditionMet = false;
    private final Set<LivingEntity> appliedTargets = new HashSet<>();

    public GenericConditionalEffectAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("condition_type")) {
            conditionType = (String) props.get("condition_type");
        }
        if (props.containsKey("nature_check_radius")) {
            natureCheckRadius = ((Number) props.get("nature_check_radius")).intValue();
        }
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        if (actions.isEmpty() || dragon.level.isClientSide) {
            return;
        }

        boolean conditionMet = checkCondition(dragon, owner);

        if (conditionMet && !wasConditionMet) {
            // Condition just became true - apply effects
            executeActions(dragon, owner);
            appliedTargets.add(dragon);
            if (owner != null) {
                appliedTargets.add(owner);
            }
        } else if (!conditionMet && wasConditionMet) {
            // Condition just became false - cleanup attribute modifiers
            cleanupAttributeModifiers();
        }

        wasConditionMet = conditionMet;
    }

    private void cleanupAttributeModifiers() {
        for (LivingEntity target : appliedTargets) {
            if (target != null && target.isAlive()) {
                for (ActionData action : actions) {
                    action.removeAttributeModifiers(target);
                }
            }
        }
        appliedTargets.clear();
    }

    public void onDragonDeath(TameableDragonEntity dragon) {
        cleanupAttributeModifiers();
    }

    private boolean checkCondition(TameableDragonEntity dragon, Player owner) {
        return switch (conditionType) {
            case "in_water" -> dragon.isInWater();
            case "near_nature" -> checkNearNature(dragon);
            case "on_ground" -> dragon.onGround();
            case "flying" -> !dragon.onGround() && !dragon.isInWater();
            case "day" -> dragon.level.isDay();
            case "night" -> !dragon.level.isDay();
            case "health_low" -> dragon.getHealth() / dragon.getMaxHealth() < 0.25f;
            case "health_full" -> dragon.getHealth() >= dragon.getMaxHealth();
            case "alone" -> owner == null || dragon.distanceTo(owner) > range;
            case "raining" -> dragon.level.isRaining();
            case "thundering" -> dragon.level.isThundering();
            default -> true;
        };
    }

    private boolean checkNearNature(TameableDragonEntity dragon) {
        var level = dragon.level;
        var basePos = dragon.blockPosition();
        var blocks = BlockPos.betweenClosedStream(
                        basePos.offset(natureCheckRadius, natureCheckRadius, natureCheckRadius),
                        basePos.offset(-natureCheckRadius, -natureCheckRadius, -natureCheckRadius))
                .map(level::getBlockState)
                .filter(state ->
                        state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS));

        return blocks.findAny().isPresent();
    }
}
