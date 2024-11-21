package dmr.DragonMounts.types.abilities.types;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.player.Player;

public interface NearbyAbility extends Ability
{
	@Override
	default void tick(DMRDragonEntity dragon)
	{
		if (dragon.getOwner() instanceof Player player) {
			if (dragon.distanceTo(player) <= getRange() || dragon.getControllingPassenger() == player) {
				tick(dragon, player);
			}
		}
	}
	
	default int getRange()
	{
		return 5;
	}
	
	void tick(DMRDragonEntity dragon, Player owner);
}
