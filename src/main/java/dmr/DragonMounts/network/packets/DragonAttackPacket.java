package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record DragonAttackPacket(int entityId) implements IMessage<DragonAttackPacket>
{
	public static ResourceLocation ID = DragonMountsRemaster.id("dragon_attack");
	
	@Override
	public DragonAttackPacket decode(FriendlyByteBuf buffer)
	{
		return new DragonAttackPacket(buffer.readInt());
	}
	
	@Override
	public void handle(PlayPayloadContext context, Player player)
	{
		var entity = player.level.getEntity(entityId);
		
		if(entity instanceof DMRDragonEntity dragon){
			dragon.swing(InteractionHand.MAIN_HAND);
			dragon.triggerAnim("head-controller", "bite");
			
			var dimensions = dragon.getDimensions(dragon.getPose());
			
			float f1 = -(float)dragon.yBodyRot * ((float)Math.PI / 180F);
			float f4 = Mth.sin(f1);
			float f5 = Mth.cos(f1);
			Vec3 lookVector = new Vec3(f4 * dimensions.width * 2, 0, f5 * dimensions.width * 2);
			
			var offsetAabb = dragon.getBoundingBox().move(lookVector).inflate(2, 5, 2);
			
			var entities = dragon.level.getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat().selector(player::canAttack).selector(s -> !s.isAlliedTo(player)), player, offsetAabb);
			var target = entities.stream().filter(e -> e != dragon && e != player).findFirst().orElse(null);
			
			if (target != null) {
				dragon.doHurtTarget(target);
			}
		}
	}
	
	@Override
	public boolean autoSync()
	{
		return false;
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer)
	{
		pBuffer.writeInt(entityId);
	}
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
