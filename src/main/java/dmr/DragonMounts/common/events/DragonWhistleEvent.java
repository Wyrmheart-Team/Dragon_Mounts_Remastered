package dmr.DragonMounts.common.events;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.registry.DMRCapability;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingTickEvent;

@EventBusSubscriber(modid = DragonMountsRemaster.MOD_ID)
public class DragonWhistleEvent
{
	private static boolean despawnCheck(DMRDragonEntity dragon){
		LivingEntity owner = dragon.getOwner();
		if (owner instanceof Player player) {
			
			var state = player.getData(DMRCapability.PLAYER_CAPABILITY);
			
			if(dragon.getDragonUUID().equals(state.dragonUUID)) {
				if (state.summonInstance != null && dragon.getSummonInstance() != null && !state.summonInstance.equals(dragon.getSummonInstance())) {
					System.out.println("Despawning dragon");
					return true;
				}
			}
		}
		
		return false;
	}
	
	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
		if(!event.getLevel().isClientSide) {
			if (event.getEntity() instanceof DMRDragonEntity dragon) {
				if(despawnCheck(dragon)){
					event.setCanceled(true);
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
			}
		}
	}
}
