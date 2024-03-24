package dmr.DragonMounts.common.handlers;

import com.google.common.collect.ImmutableList;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.network.NetworkHandler;
import dmr.DragonMounts.network.packets.DragonStatePacket;
import dmr.DragonMounts.registry.DMRCapability;
import dmr.DragonMounts.registry.DMRSounds;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor.TargetPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DragonWhistleHandler
{
	public static boolean canCall(Player player){
		var handler = PlayerStateUtils.getHandler(player);
		
		if(handler.dragonUUID == null){
			player.displayClientMessage(Component.translatable("dmr.dragon_call.nodragon").withStyle(ChatFormatting.RED), true);
			return false;
		}
		
		if(handler.respawnDelay > 0){
			player.displayClientMessage(Component.translatable("dmr.dragon_call.respawn", handler.respawnDelay / 20).withStyle(ChatFormatting.RED), true);
			return false;
		}
		
		if (player.getVehicle() != null)
		{
			player.displayClientMessage(Component.translatable("dmr.dragon_call.riding").withStyle(ChatFormatting.RED), true);
			return false;
		}
		
		if(DMRConfig.CALL_CHECK_SPACE.get()){
			if (!player.level.collidesWithSuffocatingBlock(null, player.getBoundingBox().inflate(1, 1, 1)))
			{
				player.displayClientMessage(Component.translatable("dmr.dragon_call.nospace").withStyle(ChatFormatting.RED), true);
				return false;
			}
		}
		
		if(handler.lastCall != null && DMRConfig.WHISTLE_COOLDOWN_CONFIG.get() > 0){
			if(handler.lastCall + DMRConfig.WHISTLE_COOLDOWN_CONFIG.get() > System.currentTimeMillis()){
				player.displayClientMessage(Component.translatable("dmr.dragon_call.on_cooldown").withStyle(ChatFormatting.RED), true);
				return false;
			}
		}
		
		return true;
	}
	
	public static void summonDragon(Player player){
		if(player != null){
			if (callDragon(player)){
				var handler = PlayerStateUtils.getHandler(player);
				handler.lastCall = System.currentTimeMillis();
			}
		}
	}
	
	public static boolean callDragon(Player player){
		if(player != null){
			DragonOwnerCapability cap = player.getData(DMRCapability.PLAYER_CAPABILITY);
			
			if (!canCall(player))
				return false;
			
			Random rand = new Random();
			player.level.playSound(null, player.blockPosition(), DMRSounds.DRAGON_WHISTLE_SOUND.get(), player.getSoundSource(), 1, (float) (1.4 + rand.nextGaussian() / 3));
			
			DMRDragonEntity dragon = findDragon(player, cap.dragonUUID);
			
			if(dragon != null){
				if (dragon.level.dimensionType() == player.level.dimensionType())
				{
					dragon.ejectPassengers();
					
					if (dragon.position().distanceTo(player.position()) <= DMRDragonEntity.BASE_FOLLOW_RANGE * 2)
					{
						//Walk to player
						dragon.setOrderedToSit(false);
						dragon.setWanderTarget(null);
						
						if(!player.level.isClientSide){
							dragon.setPathingGoal(player.blockPosition());
						}
						
						if(!player.level.isClientSide){
							NetworkHandler.send(PacketDistributor.TRACKING_ENTITY.with(dragon), new DragonStatePacket(dragon.getId(), 1));
						}
					}
					else
					{
						//Teleport to player
						dragon.setOrderedToSit(false);
						dragon.setWanderTarget(null);
						
						if(!player.level.isClientSide){
							dragon.setPos(player.getX(), player.getY(), player.getZ());
						}
						
						if(!player.level.isClientSide){
							NetworkHandler.send(PacketDistributor.TRACKING_ENTITY.with(dragon), new DragonStatePacket(dragon.getId(), 1));
						}
					}
					
					cap.setLastSeen(player);
					return true;
				}
			}
			
			// Spawning a new dragon
			DMRDragonEntity newDragon = cap.createDragonEntity(player, player.level);
			newDragon.setPos(player.getX(), player.getY(), player.getZ());
			player.level.addFreshEntity(newDragon);
			return true;
		}
		
		return false;
	}
	
	public static void setDragon(Player player, DMRDragonEntity dragon){
		player.getData(DMRCapability.PLAYER_CAPABILITY).setPlayer(player);
		player.getData(DMRCapability.PLAYER_CAPABILITY).setDragon(dragon);
	}
	
	public static DMRDragonEntity findDragon(Player player, UUID dragonId){
		var dragons = player.level.getNearbyEntities(DMRDragonEntity.class, TargetingConditions.forNonCombat().ignoreLineOfSight(), player, player.getBoundingBox().inflate(32));
		
		for (var e : dragons)
		{
			if (e.getDragonUUID().equals(dragonId))
			{
				return e;
			}
		}
		
		return null;
	}
}
