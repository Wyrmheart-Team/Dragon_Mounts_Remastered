package dmr.DragonMounts.common.events;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.registry.DMRCapability;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent.LevelTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingTickEvent;

import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = DragonMountsRemaster.MOD_ID)
public class DragonWhistleEvent
{
	private static boolean despawnCheck(DMRDragonEntity dragon){
		LivingEntity owner = dragon.getOwner();
		if (owner instanceof Player player) {
			
			var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
			
			if(dragon.getDragonUUID().equals(state.dragonUUID)) {
				return state.summonInstance != null && dragon.getSummonInstance() != null && !state.summonInstance.equals(dragon.getSummonInstance());
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
				
				if(state.dragonUUID != null) {
					var dragonWasKilled = DragonWorldDataManager.isDragonDead(event.getLevel(), state.dragonUUID);
					
					if (dragonWasKilled) {
						var dragonRespawnDelay = DragonWorldDataManager.getDeathDelay(event.getLevel(), state.dragonUUID);
						var message = DragonWorldDataManager.getDeathMessage(event.getLevel(), state.dragonUUID);
						var mes = (MutableComponent) Component.translatable(message);
						player.displayClientMessage(mes.withStyle(ChatFormatting.RED), false);
						state.respawnDelay = dragonRespawnDelay;
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
				if(state.respawnDelay > 0){
					state.respawnDelay--;
					
					if(state.respawnDelay == 0){
						DragonWorldDataManager.clearDragonData(player.level, state.dragonUUID);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event){
		if(!event.getEntity().level.isClientSide) {
			if (event.getEntity() instanceof DMRDragonEntity dragon) {
				//Player is online, do death handle
				if(dragon.getOwner() != null && dragon.getOwner() instanceof Player player) {
					var mes = (MutableComponent)event.getSource().getLocalizedDeathMessage(event.getEntity());
					player.displayClientMessage(mes.withStyle(ChatFormatting.RED), false);
					
					if(!DMRConfig.ALLOW_RESPAWN.get()){
						var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
						state.dragonUUID = null;
						state.summonInstance = null;
					}else if(DMRConfig.RESPAWN_TIME.get() > 0){
						var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
						state.respawnDelay = DMRConfig.RESPAWN_TIME.get();
					}
				}else{
					//Player isnt online, save to world
					DragonWorldDataManager.setDragonDead(dragon, event.getSource());
				}
			}
		}
	}
}
