package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.ActionData;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;

/**
 * Base class for abilities that use the new ActionData system.
 * This provides a foundation for abilities that execute actions on various targets.
 */
public abstract class GenericActionAbility extends Ability {
    protected final List<ActionData> actions = new ArrayList<>();
    protected int range = 10;
    protected boolean requiresOwnerNearby = false;

    public GenericActionAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("range")) {
            range = ((Number) props.get("range")).intValue();
        }

        if (props.containsKey("requires_owner_nearby")) {
            requiresOwnerNearby = (Boolean) props.get("requires_owner_nearby");
        }

        // Parse actions from top-level actions list
        actions.addAll(ActionData.createFromActionsList(definition.getActions()));
    }

    @Override
    public boolean isNearbyAbility() {
        return requiresOwnerNearby;
    }

    @Override
    public int getRange() {
        return range;
    }

    /**
     * Execute all actions configured for this ability.
     */
    protected void executeActions(TameableDragonEntity dragon, Player owner, Object eventTarget) {
        if (actions.isEmpty()) return;

        int tier = getLevel();
        for (ActionData action : actions) {
            action.execute(dragon, owner, eventTarget, tier, range);
        }
    }

    /**
     * Execute actions without an event target.
     */
    protected void executeActions(TameableDragonEntity dragon, Player owner) {
        executeActions(dragon, owner, null);
    }
}
