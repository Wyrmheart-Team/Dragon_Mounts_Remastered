package dmr.DragonMounts.util;

import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.registry.ModCapabilities;
import net.minecraft.world.entity.player.Player;

public class PlayerStateUtils {

	public static DragonOwnerCapability getHandler(Player player) {
		if (player == null) {
			return new DragonOwnerCapability();
		}

		var handler = player.getData(ModCapabilities.PLAYER_CAPABILITY);
		if (handler.getPlayer() == null) {
			handler.setPlayer(player);
		}
		return handler;
	}
}
