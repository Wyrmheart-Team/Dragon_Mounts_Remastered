package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.ActionData;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.Map;
import net.minecraft.world.entity.player.Player;

/**
 * A generic ability that applies permanent actions when the dragon is born/tamed.
 * Perfect for passive attribute bonuses, permanent effects, etc.
 * Actions are applied once and remain until the dragon dies.
 */
public class GenericPermanentAbility extends GenericActionAbility {

    public GenericPermanentAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        // Mark all attribute actions as permanent
        for (ActionData action : actions) {
            if (action.getType() == ActionData.ActionType.ATTRIBUTE) {
                action.getParameters().put("permanent", true);
            }
        }
    }

    public void onDragonTame(TameableDragonEntity dragon, Player owner) {
        // Apply permanent actions when dragon is tamed
        executeActions(dragon, owner);
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        // Do nothing - permanent abilities don't need ticking
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        // Do nothing - permanent abilities don't need ticking
    }
}
