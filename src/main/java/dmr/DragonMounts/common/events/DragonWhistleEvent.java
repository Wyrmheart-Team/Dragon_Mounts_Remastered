package dmr.DragonMounts.common.events;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler.DragonInstance;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.network.packets.CompleteDataSync;
import dmr.DragonMounts.network.packets.DragonRespawnDelayPacket;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DMR.MOD_ID)
public class DragonWhistleEvent {

	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Post event) {
		if (!event.getLevel().isClientSide) {
			var data = DragonWorldDataManager.getInstance(event.getLevel());

			for (var uuid : data.deadDragons) {
				if (data.deathDelay.get(uuid) > 0) {
					data.deathDelay.put(uuid, data.deathDelay.get(uuid) - 1);
				}
			}

			for (var uuid : new CopyOnWriteArrayList<>(data.deadDragons)) {
				if (data.deathDelay.get(uuid) <= 0) {
					DragonWorldDataManager.clearDragonData(event.getLevel(), uuid);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!event.getLevel().isClientSide) {
			// Remove dragon from the world if the last summon was in a different dimension
			if (event.getEntity() instanceof DMRDragonEntity dragon) {
				var level = event.getLevel();

				if (dragon.getOwner() != null && dragon.getOwner() instanceof Player player) {
					var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
					var index = DragonWhistleHandler.getDragonSummonIndex(player, dragon.getDragonUUID());
					var instance = cap.dragonInstances.get(index);

					if (instance != null) {
						if (instance.getDimension() != null) {
							var key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(instance.getDimension()));

							if (level.dimension() != key) {
								event.setCanceled(true);
								return;
							}
						}
					}
				}
			}

			//Check if player is online and has a dragon instance
			if (event.getEntity() instanceof Player player) {
				var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);

				if (!state.dragonInstances.isEmpty()) {
					for (Map.Entry<Integer, DragonInstance> ent : state.dragonInstances.entrySet()) {
						var index = ent.getKey();
						var id = ent.getValue().getUUID();

						var dragonWasKilled = DragonWorldDataManager.isDragonDead(event.getLevel(), id);

						if (dragonWasKilled) {
							var dragonRespawnDelay = DragonWorldDataManager.getDeathDelay(event.getLevel(), id);
							var message = DragonWorldDataManager.getDeathMessage(event.getLevel(), id);
							var mes = Component.Serializer.fromJsonLenient(message, event.getLevel().registryAccess());

							if (mes != null) {
								player.displayClientMessage(mes, false);
							}

							if (ServerConfig.ALLOW_RESPAWN.get()) {
								state.respawnDelays.put(index, dragonRespawnDelay);
							} else {
								state.dragonNBTs.remove(index);
								state.respawnDelays.remove(index);
								state.dragonInstances.remove(index);
							}

							DragonWorldDataManager.clearDragonData(event.getLevel(), id);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdate(EntityTickEvent.Post event) {
		if (!event.getEntity().level.isClientSide) {
			if (event.getEntity() instanceof Player player) {
				var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);
				for (Map.Entry<Integer, DragonInstance> ent : state.dragonInstances.entrySet()) {
					var index = ent.getKey();
					var id = ent.getValue().getUUID();

					if (state.respawnDelays.containsKey(index) && state.respawnDelays.get(index) > 0) {
						state.respawnDelays.put(index, state.respawnDelays.get(index) - 1);
						PacketDistributor.sendToPlayer(
							(ServerPlayer) player,
							new DragonRespawnDelayPacket(index, state.respawnDelays.get(index))
						);
						if (state.respawnDelays.get(index) == 0) {
							DragonWorldDataManager.clearDragonData(player.level, id);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (!event.getEntity().level.isClientSide) {
			if (event.getEntity() instanceof DMRDragonEntity dragon) {
				var mes = ((MutableComponent) event.getSource().getLocalizedDeathMessage(event.getEntity())).withStyle(ChatFormatting.RED);

				//Player is online, do death handle
				if (dragon.getOwner() != null && dragon.getOwner() instanceof Player player) {
					//Death message already gets sent by vanilla, so until I can figure out how to cancel that, just let vanilla send the message when player is online
					player.displayClientMessage(mes, false);

					var index = DragonWhistleHandler.getDragonSummonIndex(player, dragon.getDragonUUID());

					if (!ServerConfig.ALLOW_RESPAWN.get()) {
						var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);

						state.dragonInstances.remove(index);
						state.dragonNBTs.remove(index);
						state.respawnDelays.remove(index);
						PacketDistributor.sendToPlayer((ServerPlayer) player, new CompleteDataSync(player));
					} else if (ServerConfig.RESPAWN_TIME.get() > 0) {
						var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);
						state.respawnDelays.put(index, ServerConfig.RESPAWN_TIME.get() * 20);

						var whistle = ModItems.DRAGON_WHISTLES.get(index).get();

						if (!player.getCooldowns().isOnCooldown(whistle)) {
							player.getCooldowns().addCooldown(whistle, ServerConfig.RESPAWN_TIME.get() * 20);
						}

						if (player instanceof ServerPlayer serverPlayer) {
							PacketDistributor.sendToPlayer(serverPlayer, new CompleteDataSync(player));
						}
					}
				} else {
					//Player isnt online, save to world
					DragonWorldDataManager.setDragonDead(
						dragon,
						Component.Serializer.toJson(mes, event.getEntity().level.registryAccess())
					);
				}
			}
		}
	}
}
