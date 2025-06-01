package dmr.DragonMounts.server.worlddata;

import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.server.worlddata.DragonWorldData.DragonHistory;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class DragonWorldDataManager {

    public static DragonWorldData getInstance(Level level) {
        return DragonWorldData.getInstance(level);
    }

    public static void clearDragonData(Level level, UUID uuid) {
        DragonWorldData data = DragonWorldData.getInstance(level);
        data.deadDragons.remove(uuid);
        data.deathDelay.remove(uuid);
        data.deathMessages.remove(uuid);
        data.setDirty();
    }

    public static boolean isDragonDead(Level level, UUID uuid) {
        DragonWorldData data = DragonWorldData.getInstance(level);
        return data.deadDragons.contains(uuid);
    }

    public static int getDeathDelay(Level level, UUID uuid) {
        DragonWorldData data = DragonWorldData.getInstance(level);
        return data.deathDelay.get(uuid);
    }

    public static String getDeathMessage(Level level, UUID uuid) {
        DragonWorldData data = DragonWorldData.getInstance(level);
        return data.deathMessages.get(uuid);
    }

    public static void setDragonDead(TameableDragonEntity dragon, String message) {
        var level = dragon.level;
        DragonWorldData data = DragonWorldData.getInstance(level);
        data.deadDragons.add(dragon.getDragonUUID());
        data.deathDelay.put(dragon.getDragonUUID(), ServerConfig.RESPAWN_TIME * 20);
        data.deathMessages.put(dragon.getDragonUUID(), message);
        data.setDirty();
    }

    public static void addDragonHistory(TameableDragonEntity dragon) {
        var level = dragon.level;
        DragonWorldData data = DragonWorldData.getInstance(level);
        var compound = new CompoundTag();
        dragon.save(compound);
        var owner = dragon.getOwner();
        var playerName = owner != null ? owner.getName().getString() : "Unknown";
        var name = dragon.getName();
        data.dragonHistory.put(
                dragon.getDragonUUID(),
                new DragonWorldData.DragonHistory(
                        dragon.getDragonUUID(), System.currentTimeMillis(), playerName, name, compound));
        data.setDirty();
    }

    public static DragonHistory getDragonHistory(Level level, UUID uuid) {
        DragonWorldData data = DragonWorldData.getInstance(level);
        return data.dragonHistory.get(uuid);
    }
}
