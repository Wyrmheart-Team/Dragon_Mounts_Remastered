package dmr.DragonMounts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public interface IMessage <T extends CustomPacketPayload> extends CustomPacketPayload
{
	T decode(FriendlyByteBuf buffer);
	
	default boolean autoSync() { return false; }
	
	void handle(PlayPayloadContext context, Player player);
	default void handleServer(PlayPayloadContext context, ServerPlayer player){}
	default void handleClient(PlayPayloadContext context, Player player){}
}
