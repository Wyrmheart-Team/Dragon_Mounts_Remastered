package dmr.DragonMounts.client.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.network.packets.DismountDragonPacket;
import dmr.DragonMounts.network.packets.SummonDragonPacket;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = DMR.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class KeyInputHandler {

	public static KeyMapping SUMMON_DRAGON = new KeyMapping("dmr.keybind.summon_dragon", GLFW.GLFW_KEY_V, "dmr.keybind.category");
	public static KeyMapping ATTACK_KEY = new KeyMapping("dmr.keybind.attack", GLFW.GLFW_KEY_LEFT_ALT, "dmr.keybind.category");

	@SubscribeEvent
	public static void registerBindings(RegisterKeyMappingsEvent event) {
		event.register(SUMMON_DRAGON);
		event.register(ATTACK_KEY);
	}

	@OnlyIn(Dist.CLIENT)
	@EventBusSubscriber(modid = DMR.MOD_ID, value = Dist.CLIENT, bus = Bus.GAME)
	public static class KeyClickHandler {

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void clientTick(ClientTickEvent.Post event) {
			if (Minecraft.getInstance().level == null) return;
			if (Minecraft.getInstance().player == null) return;
			if (Minecraft.getInstance().screen != null) return;

			if (SUMMON_DRAGON.consumeClick()) {
				PacketDistributor.sendToServer(new SummonDragonPacket());
			}
		}

		private static Long lastDismountClick = null;

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void onKeyEvent(InputEvent.Key event) {
			if (Minecraft.getInstance().level == null) return;
			if (Minecraft.getInstance().player == null) return;
			if (Minecraft.getInstance().screen != null) return;

			var player = Minecraft.getInstance().player;

			if (player.getControlledVehicle() instanceof DMRDragonEntity) {
				if (
					event.getAction() == InputConstants.PRESS &&
					event.getKey() == Minecraft.getInstance().options.keyShift.getKey().getValue()
				) {
					if (ClientConfig.DOUBLE_PRESS_DISMOUNT.get()) {
						if (
							lastDismountClick != null &&
							System.currentTimeMillis() < lastDismountClick + TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS)
						) {
							lastDismountClick = null;
							PacketDistributor.sendToServer(new DismountDragonPacket(player.getId(), true));
						} else {
							lastDismountClick = System.currentTimeMillis();
						}
					} else {
						PacketDistributor.sendToServer(new DismountDragonPacket(player.getId(), true));
					}
				}
			}
		}
	}
}
