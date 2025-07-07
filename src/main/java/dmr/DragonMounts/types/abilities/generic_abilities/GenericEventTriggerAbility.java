package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.EventType;
import java.util.Map;
import net.minecraft.world.entity.player.Player;

/**
 * A generic ability that triggers actions based on specific events.
 * Supports on_attack, on_damage, on_kill, on_critical, on_mount, on_dismount, on_feed events.
 */
public class GenericEventTriggerAbility extends GenericActionAbility {
    private EventType eventType = EventType.ON_ATTACK;
    private float triggerChance = 1.0f;
    private int cooldownTicks = 0;

    private int lastTriggerTick = 0;

    public GenericEventTriggerAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("event_type")) {
            String eventTypeStr = (String) props.get("event_type");
            EventType parsed = EventType.fromJsonKey(eventTypeStr);
            if (parsed != null) {
                eventType = parsed;
            }
        }

        if (props.containsKey("trigger_chance")) {
            triggerChance = ((Number) props.get("trigger_chance")).floatValue();
        }

        if (props.containsKey("cooldown_ticks")) {
            cooldownTicks = ((Number) props.get("cooldown_ticks")).intValue();
        }
    }

    public boolean canTrigger(TameableDragonEntity dragon) {
        if (cooldownTicks > 0) {
            int currentTick = dragon.tickCount;
            if (currentTick - lastTriggerTick < cooldownTicks) {
                return false;
            }
        }

        return dragon.getRandom().nextFloat() <= triggerChance;
    }

    public void triggerEffects(TameableDragonEntity dragon, Player owner, Object target) {
        if (!canTrigger(dragon)) return;

        lastTriggerTick = dragon.tickCount;
        executeActions(dragon, owner, target);
    }

    public EventType getEventType() {
        return eventType;
    }
}
