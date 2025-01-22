package dmr.DragonMounts.abilities.scripting.wrappers;

import net.minecraft.world.entity.player.Player;

public class PlayerLuaWrapper extends EntityLuaWrapper<Player> {

	public PlayerLuaWrapper(Player entity) {
		super(entity);
	}

	public String getName() {
		return entity.getName().getString();
	}

	public boolean isRiding() {
		return entity.isPassenger();
	}
}
