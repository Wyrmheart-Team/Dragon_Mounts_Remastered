package dmr.DragonMounts.types.abilities.types;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.world.entity.player.Player;

public interface NearbyAbility extends Ability {
    @Override
    default void tick(TameableDragonEntity dragon) {
        if (dragon.getOwner() instanceof Player player) {
            if (dragon.distanceTo(player) <= getRange() || dragon.getControllingPassenger() == player) {
                tick(dragon, player);
            }
        }
    }

    default int getRange() {
        return 10;
    }

    void tick(TameableDragonEntity dragon, Player owner);
}
