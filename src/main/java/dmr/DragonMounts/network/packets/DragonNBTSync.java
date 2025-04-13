package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonNBTSync(int id, CompoundTag tag) implements IMessage<DragonNBTSync> {
	public static final StreamCodec<FriendlyByteBuf, DragonNBTSync> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			DragonNBTSync::id,
			ByteBufCodecs.COMPOUND_TAG,
			DragonNBTSync::tag,
			DragonNBTSync::new
	);
	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DMR.id("whistle_data_sync"));
	
	@Override
	public DragonNBTSync decode(FriendlyByteBuf buffer) {
		return new DragonNBTSync(buffer.readInt(), buffer.readNbt());
	}
	
	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DragonNBTSync> streamCodec() {
		return STREAM_CODEC;
	}
	
	@Override
	public void handle(IPayloadContext context, Player player) {}
	
	@Override
	public void handleServer(IPayloadContext context, ServerPlayer player) {
		var state = PlayerStateUtils.getHandler(player);
		var tag = state.dragonNBTs.get(id);
		PacketDistributor.sendToPlayer(player, new DragonNBTSync(id, tag == null ? new CompoundTag() : tag));
	}
	
	@Override
	public void handleClient(IPayloadContext context, Player player) {
		var state = PlayerStateUtils.getHandler(player);
		
		if(tag.isEmpty()){
			state.dragonNBTs.remove(id);
			return;
		}
		
		state.dragonNBTs.put(id, tag);
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
