package dmr.DragonMounts.common.handlers;

import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.common.capability.types.NBTInterface;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.network.packets.DragonStatePacket;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.registry.ModSounds;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class DragonWhistleHandler {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DragonInstance implements NBTInterface {

		String dimension;
		UUID entityId;
		UUID UUID;

		public DragonInstance(Level level, UUID entityId, UUID dragonUUID) {
			this.dimension = level.dimension().location().toString();
			this.entityId = entityId;
			this.UUID = dragonUUID;
		}

		public DragonInstance(DMRDragonEntity dragon) {
			this.dimension = dragon.level.dimension().location().toString();
			this.entityId = dragon.getUUID();
			this.UUID = dragon.getDragonUUID();
		}

		@Override
		public CompoundTag writeNBT() {
			CompoundTag tag = new CompoundTag();
			tag.putString("dimension", dimension);
			tag.putUUID("entityId", entityId);
			tag.putUUID("uuid", UUID);
			return tag;
		}

		@Override
		public void readNBT(CompoundTag base) {
			if (base.contains("dimension")) {
				dimension = base.getString("dimension");
			}
			if (base.contains("entityId")) {
				entityId = base.getUUID("entityId");
			}
			if (base.contains("uuid")) {
				UUID = base.getUUID("uuid");
			}
		}
	}

	public static boolean canCall(Player player, int index) {
		var handler = PlayerStateUtils.getHandler(player);

		if (index == -1) {
			player.displayClientMessage(Component.translatable("dmr.dragon_call.no_whistle").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (!handler.dragonInstances.containsKey(index) || handler.dragonInstances.get(index) == null) {
			if (!player.level.isClientSide) {
				player.displayClientMessage(Component.translatable("dmr.dragon_call.nodragon").withStyle(ChatFormatting.RED), true);
			}
			return false;
		}

		if (handler.respawnDelays.getOrDefault(index, 0) > 0) {
			if (!player.level.isClientSide) {
				player.displayClientMessage(
					Component.translatable("dmr.dragon_call.respawn", handler.respawnDelays.getOrDefault(index, 0) / 20).withStyle(
						ChatFormatting.RED
					),
					true
				);
			}
			return false;
		}

		if (player.getVehicle() != null) {
			if (!player.level.isClientSide) {
				player.displayClientMessage(Component.translatable("dmr.dragon_call.riding").withStyle(ChatFormatting.RED), true);
			}
			return false;
		}

		if (ServerConfig.CALL_CHECK_SPACE.get()) {
			if (!player.level.collidesWithSuffocatingBlock(null, player.getBoundingBox().inflate(1, 1, 1))) {
				if (!player.level.isClientSide) {
					player.displayClientMessage(Component.translatable("dmr.dragon_call.nospace").withStyle(ChatFormatting.RED), true);
				}
				return false;
			}
		}

		if (handler.lastCall != null && ServerConfig.WHISTLE_COOLDOWN_CONFIG.get() > 0) {
			if (handler.lastCall + ServerConfig.WHISTLE_COOLDOWN_CONFIG.get() > System.currentTimeMillis()) {
				if (!player.level.isClientSide) {
					player.displayClientMessage(Component.translatable("dmr.dragon_call.on_cooldown").withStyle(ChatFormatting.RED), true);
				}
				return false;
			}
		}

		return true;
	}

	public static void summonDragon(Player player) {
		if (player != null) {
			if (callDragon(player)) {
				var handler = PlayerStateUtils.getHandler(player);
				handler.lastCall = System.currentTimeMillis();
				ModItems.DRAGON_WHISTLES.values()
					.forEach(s -> {
						if (!player.getCooldowns().isOnCooldown(s.get())) {
							player
								.getCooldowns()
								.addCooldown(
									s.get(),
									(int) TimeUnit.SECONDS.convert(ServerConfig.WHISTLE_COOLDOWN_CONFIG.get(), TimeUnit.MILLISECONDS) * 20
								);
						}
					});
			}
		}
	}

	public static boolean callDragon(Player player) {
		if (player != null) {
			DragonOwnerCapability cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);

			var summonItemIndex = getDragonSummonIndex(player);

			if (!canCall(player, summonItemIndex)) return false;

			Random rand = new Random();
			player.level.playSound(
				null,
				player.blockPosition(),
				ModSounds.DRAGON_WHISTLE_SOUND.get(),
				player.getSoundSource(),
				1,
				(float) (1.4 + rand.nextGaussian() / 3)
			);

			DragonInstance instance = cap.dragonInstances.get(summonItemIndex);
			DMRDragonEntity dragon = findDragon(player, summonItemIndex);

			if (instance != null && !player.level.isClientSide) {
				var key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(instance.getDimension()));

				if (key != player.level.dimension()) {
					var server = player.level.getServer();
					assert server != null;
					var level = server.getLevel(key);

					var worldData1 = DragonWorldDataManager.getInstance(level);
					var worldData2 = DragonWorldDataManager.getInstance(player.level);

					// Transfer the dragon inventory
					worldData2.dragonInventories.put(instance.getUUID(), worldData1.dragonInventories.get(instance.getUUID()));
					worldData1.dragonInventories.remove(instance.getUUID());
				}
			}

			if (dragon != null) {
				dragon.ejectPassengers();

				if (dragon.position().distanceTo(player.position()) <= DMRDragonEntity.BASE_FOLLOW_RANGE * 2) {
					//Walk to player
					dragon.setOrderedToSit(false);
					dragon.setWanderTarget(Optional.empty());

					if (!player.level.isClientSide) {
						PacketDistributor.sendToPlayersTrackingEntity(dragon, new DragonStatePacket(dragon.getId(), 1));
					}
				} else {
					//Teleport to player
					dragon.setOrderedToSit(false);
					dragon.setWanderTarget(Optional.empty());

					if (!player.level.isClientSide) {
						dragon.setPos(player.getX(), player.getY(), player.getZ());
					}

					if (!player.level.isClientSide) {
						PacketDistributor.sendToPlayersTrackingEntity(dragon, new DragonStatePacket(dragon.getId(), 1));
					}
				}
				return true;
			}

			// Spawning a new dragon
			DMRDragonEntity newDragon = cap.createDragonEntity(player, player.level, summonItemIndex);
			newDragon.setPos(player.getX(), player.getY(), player.getZ());
			player.level.addFreshEntity(newDragon);

			if (!player.level.isClientSide) {
				PacketDistributor.sendToPlayersTrackingEntity(newDragon, new DragonStatePacket(newDragon.getId(), 1));
			}

			return true;
		}

		return false;
	}

	public static int getDragonSummonIndex(Player player) {
		//Main hand - first
		if (player.getInventory().getSelected().getItem() instanceof DragonWhistleItem whistleItem) {
			DyeColor c = whistleItem.getColor();
			return c.getId();
		}

		//Off hand - second
		if (player.getInventory().offhand.get(0).getItem() instanceof DragonWhistleItem whistleItem) {
			DyeColor c = whistleItem.getColor();
			return c.getId();
		}

		//Hotbar - third
		for (int i = 0; i < 9; i++) {
			if (player.getInventory().getItem(i).getItem() instanceof DragonWhistleItem whistleItem) {
				DyeColor c = whistleItem.getColor();
				return c.getId();
			}
		}

		//Inventory - fourth
		for (int i = 9; i < player.getInventory().getContainerSize(); i++) {
			if (player.getInventory().getItem(i).getItem() instanceof DragonWhistleItem whistleItem) {
				DyeColor c = whistleItem.getColor();
				return c.getId();
			}
		}

		return -1;
	}

	public static int getDragonSummonIndex(Player player, UUID dragonUUID) {
		var handler = PlayerStateUtils.getHandler(player);

		return handler.dragonInstances
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue() != null && entry.getValue().UUID.equals(dragonUUID))
			.map(Entry::getKey)
			.findFirst()
			.orElse(0);
	}

	public static void setDragon(Player player, DMRDragonEntity dragon, int index) {
		player.getData(ModCapabilities.PLAYER_CAPABILITY).setPlayerInstance(player);
		player.getData(ModCapabilities.PLAYER_CAPABILITY).setDragonToWhistle(dragon, index);
	}

	public static DMRDragonEntity findDragon(Player player, int index) {
		if (player.level.isClientSide) {
			return null;
		}

		var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
		var instance = cap.dragonInstances.get(index);

		if (instance != null) {
			var dim = instance.getDimension();
			var server = player.level.getServer();
			var key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim));

			//Check if the dimension is the same as the players
			if (key != player.level.dimension()) {
				return null;
			}

			assert server != null;
			var level = server.getLevel(key);

			if (level != null) {
				var entity = level.getEntity(instance.getEntityId());
				if (entity instanceof DMRDragonEntity dragon) {
					return dragon;
				}
			}
		}

		return null;
	}
}
