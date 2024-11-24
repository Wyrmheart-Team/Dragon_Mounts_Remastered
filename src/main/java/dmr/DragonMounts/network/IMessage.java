package dmr.DragonMounts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IMessage <T extends CustomPacketPayload> extends CustomPacketPayload {
	T decode(FriendlyByteBuf buffer);
	StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();
	
	default boolean autoSync() {return false;}
	
	void handle(IPayloadContext context, Player player);
	default void handleServer(IPayloadContext context, ServerPlayer player) {}
	default void handleClient(IPayloadContext context, Player player) {}
}
