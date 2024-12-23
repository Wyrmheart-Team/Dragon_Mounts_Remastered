package dmr.DragonMounts.server.worlddata;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import java.util.*;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class DragonWorldData extends SavedData {

	private static final String name = DMR.MOD_ID + "_dragon_world_data";

	public Map<UUID, Integer> deathDelay = new HashMap<>();
	public Map<UUID, String> deathMessages = new HashMap<>();
	public List<UUID> deadDragons = new ArrayList<>();
	public Map<UUID, DragonHistory> dragonHistory = new LinkedHashMap<>() {
		@Override
		public boolean removeEldestEntry(Map.Entry<UUID, DragonHistory> eldest) {
			return size() > ServerConfig.DRAGON_HISTORY_SIZE.get();
		}
	};

	public record DragonHistory(UUID id, Long time, String playerName, Component dragonName, CompoundTag compoundTag) {}

	public DragonWorldData() {}

	public static DragonWorldData load(CompoundTag nbt, Provider provider) {
		DragonWorldData data = new DragonWorldData();

		var num = nbt.getInt("num");
		ListTag listtag = nbt.getList("deadDragons", 10);

		for (int i = 0; i < num; i++) {
			CompoundTag compoundtag = listtag.getCompound(i);

			if (compoundtag == null || compoundtag.isEmpty()) {
				continue;
			}

			UUID uuid = compoundtag.getUUID("uuid");
			int delay = compoundtag.getInt("delay");
			String message = compoundtag.getString("message");
			data.deadDragons.add(uuid);
			data.deathDelay.put(uuid, delay);
			data.deathMessages.put(uuid, message);
		}

		var dragonHistory = nbt.getCompound("dragonHistory");
		for (String key : dragonHistory.getAllKeys()) {
			CompoundTag historyTag = dragonHistory.getCompound(key);
			UUID uuid = UUID.fromString(key);
			long time = historyTag.getLong("time");
			String playerName = historyTag.getString("playerName");
			Component dragonName = Component.Serializer.fromJson(historyTag.getString("dragonName"), provider);
			CompoundTag compoundTag = historyTag.getCompound("compoundTag");
			data.dragonHistory.put(uuid, new DragonHistory(uuid, time, playerName, dragonName, compoundTag));
		}

		return data;
	}

	@Override
	public CompoundTag save(CompoundTag pCompoundTag, Provider provider) {
		CompoundTag tag = new CompoundTag();

		tag.putInt("num", deathDelay.size());

		ListTag listtag = new ListTag();

		for (UUID uuid : deadDragons) {
			CompoundTag compoundtag = new CompoundTag();
			tag.putUUID("uuid", uuid);
			tag.putInt("delay", deathDelay.getOrDefault(uuid, 0));
			tag.putString("message", deathMessages.getOrDefault(uuid, ""));
			listtag.add(compoundtag);
		}
		tag.put("deadDragons", listtag);

		CompoundTag dragonHistoryTag = new CompoundTag();
		for (Map.Entry<UUID, DragonHistory> entry : dragonHistory.entrySet()) {
			UUID uuid = entry.getKey();
			DragonHistory history = entry.getValue();
			CompoundTag historyTag = new CompoundTag();
			historyTag.putLong("time", history.time());
			historyTag.putString("playerName", history.playerName());
			historyTag.putString("dragonName", Component.Serializer.toJson(history.dragonName(), provider));
			historyTag.put("compoundTag", history.compoundTag());
			dragonHistoryTag.put(uuid.toString(), historyTag);
		}
		tag.put("dragonHistory", dragonHistoryTag);

		return tag;
	}

	public static DragonWorldData getInstance(Level level) {
		if (!(level instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}

		DimensionDataStorage storage = ((ServerLevel) level).getDataStorage();
		return storage.computeIfAbsent(factory(), name);
	}

	public static SavedData.Factory<DragonWorldData> factory() {
		return new SavedData.Factory<>(DragonWorldData::new, DragonWorldData::load, DataFixTypes.LEVEL);
	}
}
