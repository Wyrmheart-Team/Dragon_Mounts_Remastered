package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.registry.DMRCapability;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CompleteDataSync(int playerId, CompoundTag tag) implements IMessage<CompleteDataSync>
{
	public CompleteDataSync(Player player)
	{
		this(player.getId(), player.getData(DMRCapability.PLAYER_CAPABILITY).serializeNBT(player.level.registryAccess()));
	}
	
	public static final StreamCodec<FriendlyByteBuf, CompleteDataSync> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, CompleteDataSync::playerId, ByteBufCodecs.COMPOUND_TAG, CompleteDataSync::tag, CompleteDataSync::new);
	
	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, CompleteDataSync> streamCodec()
	{
		return STREAM_CODEC;
	}
	
	public static final CustomPacketPayload.Type<DragonStatePacket> TYPE = new CustomPacketPayload.Type<>(DragonMountsRemaster.id("complete_data_sync"));
	
	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
	
	@Override
	public CompleteDataSync decode(FriendlyByteBuf buffer)
	{
		return new CompleteDataSync(buffer.readInt(), buffer.readNbt());
	}
	
	
	@Override
	public boolean autoSync()
	{
		return true;
	}
	
	public void handle(IPayloadContext supplier, Player player)
	{
		PlayerStateUtils.getHandler(player).deserializeNBT(player.level.registryAccess(), tag);
		player.refreshDimensions();
	}
}
