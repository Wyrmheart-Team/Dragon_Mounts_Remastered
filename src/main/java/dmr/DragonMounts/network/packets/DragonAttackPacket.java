package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonAttackPacket(int entityId) implements IMessage<DragonAttackPacket>
{
	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DragonMountsRemaster.id("dragon_attack"));
	
	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
	
	@Override
	public DragonAttackPacket decode(FriendlyByteBuf buffer)
	{
		return new DragonAttackPacket(buffer.readInt());
	}
	
	@Override
	public void handle(IPayloadContext context, Player player)
	{
		var entity = player.level.getEntity(entityId);
		
		if(entity instanceof DMRDragonEntity dragon){
			dragon.swing(InteractionHand.MAIN_HAND);
			dragon.triggerAnim("head-controller", "bite");
			
			var dimensions = dragon.getDimensions(dragon.getPose());
			float degrees = Mth.wrapDegrees(player.yBodyRot);
			
			double yawRadians = -Math.toRadians(degrees);
			double f4 = Math.sin(yawRadians);
			double f5 = Math.cos(yawRadians);
			Vec3 lookVector = new Vec3(f4 * dimensions.width() * 2, 0, f5 * dimensions.width() * 2);
			
			var offsetAabb = dragon.getBoundingBox().move(lookVector).inflate(2, 5, 2);
			
			var entities = dragon.level.getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat().selector(player::canAttack).selector(s -> !s.isAlliedTo(player)), player, offsetAabb);
			var target = entities.stream().filter(e -> e != dragon && e != player).findFirst().orElse(null);
			
			if (target != null) {
				dragon.doHurtTarget(target);
			}
		}
	}
	
	public static final StreamCodec<FriendlyByteBuf, DragonAttackPacket> STREAM_CODEC =
			StreamCodec.composite(ByteBufCodecs.INT, DragonAttackPacket::entityId, DragonAttackPacket::new);
	
	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DragonAttackPacket> streamCodec()
	{
		return STREAM_CODEC;
	}
}
