package dmr.DragonMounts.common.capability;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class DragonOwnerCapability implements INBTSerializable<CompoundTag> {

	@Setter
	@Getter
	private Player player;

	public Long lastCall;

	public int dragonsHatched;

	public ConcurrentHashMap<Integer, Integer> respawnDelays = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Integer, UUID> dragonUUIDs = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Integer, UUID> summonInstances = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Integer, CompoundTag> dragonNBTs = new ConcurrentHashMap<>();

	public boolean shouldDismount;

	public DMRDragonEntity createDragonEntity(Player player, Level world, int index) {
		setPlayer(player);

		var nbt = dragonNBTs.get(index);

		if (nbt != null) {
			Optional<EntityType<?>> type = EntityType.by(nbt);

			if (type.isPresent()) {
				Entity entity = type.get().create(world);
				if (entity instanceof DMRDragonEntity dragon) {
					dragon.load(nbt);
					dragon.setUUID(UUID.randomUUID());
					dragon.clearFire();
					dragon.hurtTime = 0;

					dragon.setOrderedToSit(false);
					dragon.setWanderTarget(null);

					setDragon(dragon, index);
					dragon.setHealth(Math.max(1, dragon.getHealth()));

					return dragon;
				}
			}
		}
		return null;
	}

	public void setDragon(DMRDragonEntity dragon, int index) {
		dragon.setTame(true, true);
		dragon.setOwnerUUID(player.getGameProfile().getId());

		var summonInstance = UUID.randomUUID();
		summonInstances.put(index, summonInstance);
		dragon.setSummonInstance(summonInstance);

		dragonUUIDs.put(index, dragon.getDragonUUID());

		var wanderPos = dragon.getWanderTarget();
		var sit = dragon.isOrderedToSit();

		dragon.setWanderTarget(null);
		dragon.setOrderedToSit(false);

		//noinspection removal
		var nbtData = dragon.serializeNBT(dragon.level.registryAccess());
		dragonNBTs.put(index, nbtData);

		dragon.setWanderTarget(wanderPos);
		dragon.setOrderedToSit(sit);
	}

	public boolean isSelectedDragon(DMRDragonEntity dragon) {
		if (dragon.getDragonUUID() != null) {
			for (var uuid : dragonUUIDs.values()) {
				if (dragon.getDragonUUID().equals(uuid)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag tag = new CompoundTag();

		tag.putBoolean("shouldDismount", shouldDismount);
		tag.putInt("dragonsHatched", dragonsHatched);

		for (DyeColor color : DyeColor.values()) {
			if (respawnDelays.containsKey(color.getId())) {
				tag.putInt("respawnDelay_" + color.getId(), respawnDelays.get(color.getId()));
			}

			if (dragonUUIDs.containsKey(color.getId())) {
				tag.putUUID("dragonUUID_" + color.getId(), dragonUUIDs.get(color.getId()));
			}

			if (summonInstances.containsKey(color.getId())) {
				tag.putUUID("summonInstance_" + color.getId(), summonInstances.get(color.getId()));
			}

			if (dragonNBTs.containsKey(color.getId())) {
				tag.put("dragonNBT_" + color.getId(), dragonNBTs.get(color.getId()));
			}
		}

		return tag;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag base) {
		if (base.contains("shouldDismount")) {
			shouldDismount = base.getBoolean("shouldDismount");
		}

		dragonsHatched = base.getInt("dragonsHatched");

		respawnDelays.clear();
		dragonUUIDs.clear();
		summonInstances.clear();
		dragonNBTs.clear();

		for (DyeColor color : DyeColor.values()) {
			if (base.contains("respawnDelay_" + color.getId())) {
				respawnDelays.put(color.getId(), base.getInt("respawnDelay_" + color.getId()));
			}

			if (base.contains("dragonUUID_" + color.getId())) {
				dragonUUIDs.put(color.getId(), base.getUUID("dragonUUID_" + color.getId()));
			}

			if (base.contains("summonInstance_" + color.getId())) {
				summonInstances.put(color.getId(), base.getUUID("summonInstance_" + color.getId()));
			}

			if (base.contains("dragonNBT_" + color.getId())) {
				dragonNBTs.put(color.getId(), base.getCompound("dragonNBT_" + color.getId()));
			}
		}
	}
}
