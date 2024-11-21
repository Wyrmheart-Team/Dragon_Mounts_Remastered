package dmr.DragonMounts.network;


import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.network.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;


public class NetworkHandler{
	private static final List<IMessage<?>> messages = List.of(
			new DragonStatePacket(-1, -1),
			new SyncDataPackPacket(),
			new SummonDragonPacket(),
			new CompleteDataSync(-1, null),
			new DismountDragonPacket(-1, false),
			new DragonAgeSyncPacket(-1, -1),
			new DragonAttackPacket(-1),
			new DragonBreathPacket(-1),
			new DragonRespawnDelayPacket(-1, -1)
	);
	
	public static void registerEvent(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(DragonMountsRemaster.MOD_ID);
		
		for(IMessage mes : messages){
			registrar.playBidirectional(mes.type(), mes.streamCodec(), (payload, context) -> {
				               if (context.flow().isClientbound()) {
					               if (payload instanceof IMessage<?> message) {
						               context.enqueueWork(() -> runClientSided(context, message));
					               }
				               }
				               
				               if (context.flow().isServerbound()) {
					               if (payload instanceof IMessage<?> message) {
									   var player = context.player();
						               message.handle(context, player);
						               
						               if (player instanceof ServerPlayer player1) {
							               message.handleServer(context, player1);
						               }
						               
						               if (message.autoSync()) {
										   PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, message);
						               }
					               }
				               }
			               }
			);
		}
	}
	
	@OnlyIn( Dist.CLIENT)
	private static void runClientSided(IPayloadContext context, IMessage<?> message)
	{
		var player = Minecraft.getInstance().player;
		message.handle(context, player);
		message.handleClient(context, player);
	}
}