package dmr.DragonMounts.server.worlddata;

import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DragonWorldDataManager {
	public static DragonWorldData getInstance(Level level)
	{
		return DragonWorldData.getInstance(level);
	}
	
	public static void clearDragonData(Level level, UUID uuid)
	{
		DragonWorldData data = DragonWorldData.getInstance(level);
		data.deadDragons.remove(uuid);
		data.deathDelay.remove(uuid);
		data.deathMessages.remove(uuid);
		data.setDirty();
	}
	
	public static boolean isDragonDead(Level level, UUID uuid)
	{
		DragonWorldData data = DragonWorldData.getInstance(level);
		return data.deadDragons.contains(uuid);
	}
	
	public static int getDeathDelay(Level level, UUID uuid)
	{
		DragonWorldData data = DragonWorldData.getInstance(level);
		return data.deathDelay.get(uuid);
	}
	
	public static String getDeathMessage(Level level, UUID uuid)
	{
		DragonWorldData data = DragonWorldData.getInstance(level);
		return data.deathMessages.get(uuid);
	}
	
	public static void setDragonDead(DMRDragonEntity dragon, String message)
	{
		var level = dragon.level;
		DragonWorldData data = DragonWorldData.getInstance(level);
		data.deadDragons.add(dragon.getDragonUUID());
		data.deathDelay.put(dragon.getDragonUUID(), DMRConfig.RESPAWN_TIME.get() * 20);
		data.deathMessages.put(dragon.getDragonUUID(), message);
		data.setDirty();
	}
}
