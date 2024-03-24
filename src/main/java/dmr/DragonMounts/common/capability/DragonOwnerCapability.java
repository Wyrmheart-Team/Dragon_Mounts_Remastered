package dmr.DragonMounts.common.capability;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class DragonOwnerCapability implements INBTSerializable<CompoundTag>
{
	@Setter
	private Player player;
	
	private float dragonHealth;
	public Long lastCall;
	
	public int respawnDelay;
	
	public UUID dragonUUID;
	public UUID summonInstance;
	
	private ResourceKey<Level> lastSeenDim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("overworld"));
	private CompoundTag dragonNBT = new CompoundTag();
	private Vec3 lastSeenPos = Vec3.ZERO;
	
	public boolean shouldDismount;
	
	public DMRDragonEntity createDragonEntity(Player player, Level world){
		setPlayer(player);
		
		Optional<EntityType<?>> type = EntityType.by(dragonNBT);
		
		if (type.isPresent())
		{
			Entity entity = type.get().create(world);
			if (entity instanceof DMRDragonEntity dragon)
			{
				dragon.load(dragonNBT);
				dragon.setUUID(UUID.randomUUID());
				dragon.clearFire();
				dragon.hurtTime = 0;
				
				setDragon(dragon);
				
				return dragon;
			}
		}
		return null;
	}
	
	public void setDragon(DMRDragonEntity dragon){
		dragon.setTame(true);
		dragon.setOwnerUUID(player.getGameProfile().getId());
		
		summonInstance = UUID.randomUUID();
		dragon.setSummonInstance(summonInstance);
		
		dragonUUID = dragon.getDragonUUID();
		dragonNBT = dragon.serializeNBT();
		
		setLastSeen(player);
	}
	
	public boolean isSelectedDragon(DMRDragonEntity dragon){
		return dragonUUID != null && dragon.getDragonUUID() != null && dragonUUID.equals(dragon.getDragonUUID());
	}
	
	public void setLastSeen(Player player){
		lastSeenDim = player.level.dimension();
		lastSeenPos = player.position();
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.putBoolean("shouldDismount", shouldDismount);
		
		if(dragonUUID != null) {
			tag.putFloat("dragonHealth", dragonHealth);
			
			if(dragonUUID != null) {
				tag.putUUID("dragonUUID", dragonUUID);
			}
			
			if(summonInstance != null) {
				tag.putUUID("summonInstance", summonInstance);
			}
			
			if(lastCall != null) {
				tag.putLong("lastCall", lastCall);
			}
			
			if(dragonNBT != null) {
				tag.put("dragonNBT", dragonNBT);
			}
			
			tag.put("lastSeenPos", NbtUtils.writeBlockPos(new BlockPos((int) lastSeenPos.x, (int)lastSeenPos.y, (int)lastSeenPos.z)));
			tag.putString("lastSeenDim", lastSeenDim.location().toString());
			
			tag.putInt("respawnDelay", respawnDelay);
		}
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag base)
	{
		if(base.contains("shouldDismount")){
			shouldDismount = base.getBoolean("shouldDismount");
		}
		
		if(base.contains("dragonUUID")){
			if(base.contains("dragonHealth")) {
				dragonHealth = base.getFloat("dragonHealth");
			}
			
			if(base.contains("dragonUUID")) {
				dragonUUID = base.getUUID("dragonUUID");
			}
			
			if(base.contains("summonInstance")) {
				summonInstance = base.getUUID("summonInstance");
			}
			
			if(base.contains("lastCall")){
				lastCall = base.getLong("lastCall");
			}
			
			if(base.contains("dragonNBT")){
				dragonNBT = base.getCompound("dragonNBT");
			}
			
			if(base.contains("respawnDelay")){
				respawnDelay = base.getInt("respawnDelay");
			}
			
			BlockPos temp = NbtUtils.readBlockPos(base.getCompound("lastSeenPos"));
			lastSeenPos = new Vec3(temp.getX(), temp.getY(), temp.getZ());
			lastSeenDim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(base.getString("lastSeenDim")));
		}
	}
}
