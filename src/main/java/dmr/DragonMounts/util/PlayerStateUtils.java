package dmr.DragonMounts.util;

import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.registry.DMRCapability;
import net.minecraft.world.entity.player.Player;

public class PlayerStateUtils {
	public static DragonOwnerCapability getHandler(Player player)
	{
		if (player == null) {
			return new DragonOwnerCapability();
		}
		
		var handler = player.getData(DMRCapability.PLAYER_CAPABILITY);
		
		if (handler == null) {
			return new DragonOwnerCapability();
		}
		
		return handler;
	}
}
