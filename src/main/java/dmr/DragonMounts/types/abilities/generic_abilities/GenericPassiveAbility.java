package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.ActionData;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * A generic ability that continuously applies actions while active.
 * Handles proper cleanup of attribute modifiers and other temporary effects.
 * Perfect for auras, passive bonuses that should be toggleable, etc.
 */
public class GenericPassiveAbility extends GenericActionAbility {
    private int tickInterval = 20; // Apply every second by default
    private int lastTickCount = 0;
    private final Set<LivingEntity> appliedTargets = new HashSet<>();

    public GenericPassiveAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("tick_interval")) {
            tickInterval = ((Number) props.get("tick_interval")).intValue();
        }
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        if (dragon.level.isClientSide || dragon.tickCount - lastTickCount < tickInterval) {
            return;
        }

        lastTickCount = dragon.tickCount;

        if (!isNearbyAbility()) {
            executeActions(dragon, null);
            appliedTargets.add(dragon);
        }
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        if (dragon.level.isClientSide || dragon.tickCount - lastTickCount < tickInterval) {
            return;
        }

        lastTickCount = dragon.tickCount;
        executeActions(dragon, owner);
        appliedTargets.add(dragon);
        if (owner != null) {
            appliedTargets.add(owner);
        }
    }

    /**
     * Clean up any applied effects when the ability is disabled or dragon dies.
     */
    public void cleanup() {
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
        cleanup();
    }
}
