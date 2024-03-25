package dmr.DragonMounts.common.events;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.network.packets.CompleteDataSync;
import dmr.DragonMounts.network.packets.DragonRespawnDelayPacket;
import dmr.DragonMounts.registry.DMRCapability;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent.LevelTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = DragonMountsRemaster.MOD_ID)
public class DragonWhistleEvent
{
	private static boolean despawnCheck(DMRDragonEntity dragon){
		LivingEntity owner = dragon.getOwner();
		if (owner instanceof Player player) {
			
			var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
			
			for(Map.Entry<Integer, UUID> ent : state.dragonUUIDs.entrySet()){
				var index = ent.getKey();
				var id = ent.getValue();
				if(id.equals(dragon.getDragonUUID())){
					if(state.summonInstances.containsKey(index)){
						return !state.summonInstances.get(index).equals(dragon.getSummonInstance());
					}
				}
			}
		}
		return false;
	}
	
	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent event){
		if(!event.level.isClientSide) {
			var data = DragonWorldDataManager.getInstance(event.level);
			
			for(var uuid : data.deadDragons){
				if(data.deathDelay.get(uuid) > 0){
					data.deathDelay.put(uuid, data.deathDelay.get(uuid) - 1);
				}
			}
			
			for(var uuid : new CopyOnWriteArrayList<>(data.deadDragons)){
				if(data.deathDelay.get(uuid) <= 0){
					DragonWorldDataManager.clearDragonData(event.level, uuid);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
		if(!event.getLevel().isClientSide) {
			if (event.getEntity() instanceof DMRDragonEntity dragon) {
				if(despawnCheck(dragon)){
					event.setCanceled(true);
				}
			}else if(event.getEntity() instanceof Player player){
				var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
				
				if(!state.dragonUUIDs.values().isEmpty()) {
					for (Map.Entry<Integer, UUID> ent : state.dragonUUIDs.entrySet()) {
						var index = ent.getKey();
						var id = ent.getValue();
						
						var dragonWasKilled = DragonWorldDataManager.isDragonDead(event.getLevel(), id);
						
						if (dragonWasKilled) {
							var dragonRespawnDelay = DragonWorldDataManager.getDeathDelay(event.getLevel(), id);
							var message = DragonWorldDataManager.getDeathMessage(event.getLevel(), id);
							var mes = Component.Serializer.fromJsonLenient(message);
							
							if (mes != null) {
								player.displayClientMessage(mes, false);
							}
							
							if (DMRConfig.ALLOW_RESPAWN.get()) {
								state.respawnDelays.put(index, dragonRespawnDelay);
							} else {
								state.dragonUUIDs.remove(index);
								state.dragonNBTs.remove(index);
								state.summonInstances.remove(index);
								state.respawnDelays.remove(index);
							}
							
							DragonWorldDataManager.clearDragonData(event.getLevel(), id);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onLivingUpdate(LivingTickEvent event){
		if(!event.getEntity().level.isClientSide) {
			if (event.getEntity() instanceof DMRDragonEntity dragon) {
				if(despawnCheck(dragon)){
					dragon.discard();
				}
			}else if(event.getEntity() instanceof Player player){
				var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
				for (Map.Entry<Integer, UUID> ent : state.dragonUUIDs.entrySet()) {
					var index = ent.getKey();
					var id = ent.getValue();
					
					if(state.respawnDelays.containsKey(index) && state.respawnDelays.get(index) > 0){
						state.respawnDelays.put(index, state.respawnDelays.get(index) - 1);
						NetworkHandler.sendToPlayer((ServerPlayer)player, new DragonRespawnDelayPacket(index, state.respawnDelays.get(index)));
						
						if(state.respawnDelays.get(index) == 0){
							DragonWorldDataManager.clearDragonData(player.level, id);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event){
		if(!event.getEntity().level.isClientSide) {
			if (event.getEntity() instanceof DMRDragonEntity dragon) {
				var mes = ((MutableComponent)event.getSource().getLocalizedDeathMessage(event.getEntity())).withStyle(ChatFormatting.RED);
				
				//Player is online, do death handle
				if(dragon.getOwner() != null && dragon.getOwner() instanceof Player player) {
					
					//Death message already gets sent by vanilla, so until I can figure out how to cancel that, just let vanilla send the message when player is online
					//player.displayClientMessage(mes, false);
					
					var index = DragonWhistleHandler.getDragonSummonIndex(player, dragon.getDragonUUID());
					
					if(!DMRConfig.ALLOW_RESPAWN.get()){
						var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
													
			            state.dragonUUIDs.remove(index);
						state.dragonNBTs.remove(index);
						state.summonInstances.remove(index);
						state.respawnDelays.remove(index);
						NetworkHandler.sendToPlayer((ServerPlayer)player, new CompleteDataSync(player));
						
					}else if(DMRConfig.RESPAWN_TIME.get() > 0){
						var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
						state.respawnDelays.put(index, DMRConfig.RESPAWN_TIME.get() * 20);
						
						var whistle = DMRItems.DRAGON_WHISTLES.get(index).get();
						
						if(!player.getCooldowns().isOnCooldown(whistle)){
							player.getCooldowns().addCooldown(whistle, DMRConfig.RESPAWN_TIME.get() * 20);
						}
						
						NetworkHandler.sendToPlayer((ServerPlayer)player, new CompleteDataSync(player));
					}
				}else{
					//Player isnt online, save to world
					DragonWorldDataManager.setDragonDead(dragon, Component.Serializer.toJson(mes));
				}
			}
		}
	}
}
