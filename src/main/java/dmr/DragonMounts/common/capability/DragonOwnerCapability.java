package dmr.DragonMounts.common.capability;

import dmr.DragonMounts.common.handlers.DragonWhistleHandler.DragonInstance;
import dmr.DragonMounts.network.packets.DragonNBTSync;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DragonOwnerCapability implements INBTSerializable<CompoundTag> {

	@Setter
	@Getter
	private Player playerInstance;

	public Long lastCall;

	public int dragonsHatched;

	public ConcurrentHashMap<Integer, Integer> respawnDelays = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Integer, CompoundTag> dragonNBTs = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Integer, DragonInstance> dragonInstances = new ConcurrentHashMap<>();

	public boolean shouldDismount;

	//Client side synced configs
	public boolean cameraFlight = true;
	public boolean alternateDismount = true;

	public DMRDragonEntity createDragonEntity(Player player, Level world, int index) {
		setPlayerInstance(player);

		var instance = getDragonInstance(index);
		var nbt = dragonNBTs.get(index);
		var uuid = instance.getUUID();

		if (nbt != null) {
			Optional<EntityType<?>> type = EntityType.by(nbt);

			if (type.isPresent()) {
				Entity entity = type.get().create(world);
				if (entity instanceof DMRDragonEntity dragon) {
					dragon.load(nbt);
					dragon.setUUID(UUID.randomUUID());
					dragon.setDragonUUID(uuid);


					dragon.stopSitting();
					dragon.setWanderTarget(Optional.empty());

					setDragonToWhistle(dragon, index);
					
					dragon.clearFire();
					dragon.hurtTime = 0;
					dragon.setHealth(Math.max(1, dragon.getHealth()));
					
					//Cure the dragon of any effects to prevent for example poison or wither
					dragon.removeEffectsCuredBy(EffectCures.PROTECTED_BY_TOTEM);

					return dragon;
				}
			}
		}
		return null;
	}

	public DragonInstance getDragonInstance(int index) {
		return dragonInstances.get(index);
	}

	public void setDragonInstance(int index, DragonInstance instance) {
		dragonInstances.put(index, instance);
	}

	public void setDragonToWhistle(DMRDragonEntity dragon, int index) {
		dragon.setTame(true, true);
		dragon.setOwnerUUID(playerInstance.getGameProfile().getId());

		var wanderPos = dragon.getWanderTarget();
		var sit = dragon.isOrderedToSit();

		dragon.setWanderTarget(Optional.empty());
		dragon.setOrderedToSit(false);

		//noinspection removal
		var nbtData = dragon.serializeNBT(dragon.level.registryAccess());
		dragonNBTs.put(index, nbtData);

		if (!dragon.level.isClientSide && playerInstance instanceof ServerPlayer spPlayer) {
			PacketDistributor.sendToPlayer(spPlayer, new DragonNBTSync(index, nbtData));
		}

		var instance = new DragonInstance(dragon);
		dragonInstances.put(index, instance);

		dragon.setWanderTarget(wanderPos);
		dragon.setOrderedToSit(sit);
	}

	public boolean isBoundToWhistle(DMRDragonEntity dragon) {
		if (dragon.getDragonUUID() != null) {
			return dragonInstances.values().stream().anyMatch(instance -> instance.getUUID().equals(dragon.getDragonUUID()));
		}

		return false;
	}

	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag tag = new CompoundTag();

		tag.putBoolean("shouldDismount", shouldDismount);
		tag.putInt("dragonsHatched", dragonsHatched);

		tag.putBoolean("cameraFlight", cameraFlight);
		tag.putBoolean("alternateDismount", alternateDismount);

		for (DyeColor color : DyeColor.values()) {
			if (respawnDelays.containsKey(color.getId())) {
				tag.putInt("respawnDelay_" + color.getId(), respawnDelays.get(color.getId()));
			}

			if (dragonNBTs.containsKey(color.getId())) {
				tag.put("dragonNBT_" + color.getId(), dragonNBTs.get(color.getId()));
			}
		}

		// Save dragon instances
		CompoundTag instancesTag = new CompoundTag();
		for (Entry<Integer, DragonInstance> entry : dragonInstances
			.entrySet()
			.stream()
			.filter(e -> e.getKey() != null && e.getValue() != null)
			.toList()) {
			DragonInstance instance = entry.getValue();
			instancesTag.put(entry.getKey().toString(), instance.writeNBT());
		}
		tag.put("dragonInstances", instancesTag);

		return tag;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag base) {
		if (base.contains("shouldDismount")) {
			shouldDismount = base.getBoolean("shouldDismount");
		}

		dragonsHatched = base.getInt("dragonsHatched");

		respawnDelays.clear();
		dragonNBTs.clear();

		if (base.contains("cameraFlight")) {
			cameraFlight = base.getBoolean("cameraFlight");
		}

		if (base.contains("alternateDismount")) {
			alternateDismount = base.getBoolean("alternateDismount");
		}

		for (DyeColor color : DyeColor.values()) {
			if (base.contains("respawnDelay_" + color.getId())) {
				respawnDelays.put(color.getId(), base.getInt("respawnDelay_" + color.getId()));
			}

			if (base.contains("dragonNBT_" + color.getId())) {
				dragonNBTs.put(color.getId(), base.getCompound("dragonNBT_" + color.getId()));
			}

			// Legacy support for dragonUUID, remove in future versions
			if (base.contains("dragonUUID_" + color.getId())) {
				var id = base.getUUID("dragonUUID_" + color.getId());
				var instance = new DragonInstance(
					getPlayerInstance() != null ? getPlayerInstance().level.dimension().toString() : "minecraft:overworld",
					UUID.randomUUID(),
					id
				);
				dragonInstances.put(color.getId(), instance);
			}
		}

		// Load dragon instances
		var instances = base.getCompound("dragonInstances");
		for (String jsKey : instances.getAllKeys()) {
			var key = Integer.parseInt(jsKey);
			var instance = new DragonInstance();
			instance.readNBT(instances.getCompound(jsKey));
			dragonInstances.put(key, instance);
		}
	}
}
