package dmr.DragonMounts.network;


import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.packets.*;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import java.util.List;


public class NetworkHandler{
	public static <M extends CustomPacketPayload> void send(PacketDistributor.PacketTarget distributor, M packet) {
		distributor.send(packet);
	}
	public static <M extends CustomPacketPayload> void sendToServer(M packet) {
		send(PacketDistributor.SERVER.noArg(), packet);
	}
	public static <M extends CustomPacketPayload> void sendToClients(Player player, M packet) {send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player), packet);}
	public static <M extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, M packet) {send(PacketDistributor.PLAYER.with(player), packet);}
	
	private static final List<IMessage<?>> messages = List.of(
			new DragonStatePacket(-1, -1),
			new SyncDataPackPacket(),
			new SummonDragonPacket(),
			new CompleteDataSync(-1, null),
			new DismountDragonPacket(-1, false),
			new DragonAgeSyncPacket(-1, -1),
			new DragonAttackPacket(-1),
			new DragonBreathPacket(-1)
	);
	
	public static void registerEvent(RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(DragonMountsRemaster.MOD_ID);
		
		for(IMessage<?> mes : messages){
			registrar.play(mes.id(), mes::decode, (payload, context) -> {
				               if (context.flow().isClientbound()) {
					               if (payload instanceof IMessage<?> message) {
						               context.workHandler().execute(() -> runClientSided(context, message));
					               }
				               }
				               
				               if (context.flow().isServerbound()) {
					               if (payload instanceof IMessage<?> message) {
						               if (context.player().isEmpty()) {
							               System.err.println("Player is not present for DMR networking!");
							               return;
						               }
						               
						               context.player().ifPresent(player -> {
							               message.handle(context, player);
							               
							               if (player instanceof ServerPlayer player1) {
								               message.handleServer(context, player1);
							               }
							               
							               if (message.autoSync()) {
								               sendToClients(player, message);
							               }
						               });
					               }
				               }
			               }
			);
		}
	}
	
	@OnlyIn( Dist.CLIENT)
	private static void runClientSided(PlayPayloadContext context, IMessage<?> message)
	{
		var player = Minecraft.getInstance().player;
		message.handle(context, player);
		message.handleClient(context, player);
	}
}