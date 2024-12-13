package dmr.DragonMounts.common.handlers;

import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.network.packets.DragonStatePacket;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.registry.ModSounds;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

public class DragonWhistleHandler {

	public static boolean canCall(Player player, int index) {
		var handler = PlayerStateUtils.getHandler(player);

		if (index == -1) {
			player.displayClientMessage(Component.translatable("dmr.dragon_call.no_whistle").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (!handler.dragonUUIDs.containsKey(index) || handler.dragonUUIDs.get(index) == null) {
			player.displayClientMessage(Component.translatable("dmr.dragon_call.nodragon").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (handler.respawnDelays.getOrDefault(index, 0) > 0) {
			player.displayClientMessage(
				Component.translatable("dmr.dragon_call.respawn", handler.respawnDelays.getOrDefault(index, 0) / 20).withStyle(
					ChatFormatting.RED
				),
				true
			);
			return false;
		}

		if (player.getVehicle() != null) {
			player.displayClientMessage(Component.translatable("dmr.dragon_call.riding").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (ServerConfig.CALL_CHECK_SPACE.get()) {
			if (!player.level.collidesWithSuffocatingBlock(null, player.getBoundingBox().inflate(1, 1, 1))) {
				player.displayClientMessage(Component.translatable("dmr.dragon_call.nospace").withStyle(ChatFormatting.RED), true);
				return false;
			}
		}

		if (handler.lastCall != null && ServerConfig.WHISTLE_COOLDOWN_CONFIG.get() > 0) {
			if (handler.lastCall + ServerConfig.WHISTLE_COOLDOWN_CONFIG.get() > System.currentTimeMillis()) {
				player.displayClientMessage(Component.translatable("dmr.dragon_call.on_cooldown").withStyle(ChatFormatting.RED), true);
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

			DMRDragonEntity dragon = findDragon(player, cap.dragonUUIDs.get(summonItemIndex));

			if (dragon != null) {
				if (dragon.level.dimensionType() == player.level.dimensionType()) {
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

		for (var entry : handler.dragonUUIDs.entrySet()) {
			if (entry.getValue().equals(dragonUUID)) {
				return entry.getKey();
			}
		}

		return 0;
	}

	public static void setDragon(Player player, DMRDragonEntity dragon, int index) {
		player.getData(ModCapabilities.PLAYER_CAPABILITY).setPlayer(player);
		player.getData(ModCapabilities.PLAYER_CAPABILITY).setDragon(dragon, index);
	}

	public static DMRDragonEntity findDragon(Player player, UUID dragonId) {
		var dragons = player.level.getNearbyEntities(
			DMRDragonEntity.class,
			TargetingConditions.forNonCombat().ignoreLineOfSight(),
			player,
			player.getBoundingBox().inflate(32)
		);

		for (var e : dragons) {
			if (e.getDragonUUID().equals(dragonId)) {
				return e;
			}
		}

		return null;
	}
}
