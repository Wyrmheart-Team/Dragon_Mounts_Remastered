package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.EffectData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;

/**
 * A generic ability that applies status effects to the dragon's owner.
 * Supports multiple effects and tier-based scaling.
 */
public class GenericEffectAbility extends Ability {
    private final List<EffectData> effects = new ArrayList<>();
    private int range = 10;

    public GenericEffectAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("range")) {
            range = ((Number) props.get("range")).intValue();
        }

        // Get effects from properties using the factory method
        effects.addAll(EffectData.createFromProperties(props));
    }

    @Override
    public boolean isNearbyAbility() {
        return true;
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        if (effects.isEmpty() || dragon.level.isClientSide) {
            return;
        }

        int tier = getLevel();
        for (EffectData effectData : effects) {
            effectData.apply(owner, tier);
        }
    }
}
