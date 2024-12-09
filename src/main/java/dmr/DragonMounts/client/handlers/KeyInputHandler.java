package dmr.DragonMounts.client.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.network.packets.DismountDragonPacket;
import dmr.DragonMounts.network.packets.SummonDragonPacket;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = DMR.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class KeyInputHandler {

	public static KeyMapping SUMMON_DRAGON = new KeyMapping(
		"dmr.keybind.summon_dragon",
		KeyConflictContext.IN_GAME,
		Type.KEYSYM,
		GLFW.GLFW_KEY_V,
		"dmr.keybind.category"
	);

	public static KeyMapping ATTACK_KEY = new KeyMapping(
		"dmr.keybind.attack",
		KeyConflictContext.IN_GAME,
		Type.KEYSYM,
		GLFW.GLFW_KEY_LEFT_ALT,
		"dmr.keybind.category"
	);

	public static KeyMapping DRAGON_COMMAND_KEY = new KeyMapping(
		"dmr.keybind.dragon_command",
		KeyConflictContext.IN_GAME,
		Type.KEYSYM,
		GLFW.GLFW_KEY_C,
		"dmr.keybind.category"
	);

	public static KeyMapping DISMOUNT_KEY = new KeyMapping(
		"dmr.keybind.dismount",
		KeyConflictContext.IN_GAME,
		InputConstants.UNKNOWN,
		"dmr.keybind.category"
	);

	public static KeyMapping DESCEND_KEY = new KeyMapping(
		"dmr.keybind.descend",
		KeyConflictContext.IN_GAME,
		InputConstants.UNKNOWN,
		"dmr.keybind.category"
	);

	@SubscribeEvent
	public static void registerBindings(RegisterKeyMappingsEvent event) {
		event.register(SUMMON_DRAGON);
		event.register(ATTACK_KEY);
		//		event.register(DRAGON_COMMAND_KEY);
		event.register(DISMOUNT_KEY);
		event.register(DESCEND_KEY);
	}

	@OnlyIn(Dist.CLIENT)
	@EventBusSubscriber(modid = DMR.MOD_ID, value = Dist.CLIENT, bus = Bus.GAME)
	public static class KeyClickHandler {

		private static Long lastDismountClick = null;

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void clientTick(ClientTickEvent.Post event) {
			if (Minecraft.getInstance().level == null) return;
			if (Minecraft.getInstance().player == null) return;
			if (Minecraft.getInstance().screen != null) return;

			var player = Minecraft.getInstance().player;

			if (player.getControlledVehicle() instanceof DMRDragonEntity) {
				if (Minecraft.getInstance().options.keyShift.consumeClick()) {
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
					return;
				}

				if (DISMOUNT_KEY.consumeClick()) {
					PacketDistributor.sendToServer(new DismountDragonPacket(player.getId(), true));
					return;
				}
			} else {
				if (DRAGON_COMMAND_KEY.isDown()) {
					//TODO: Open command GUI
					return;
				}

				if (SUMMON_DRAGON.consumeClick()) {
					PacketDistributor.sendToServer(new SummonDragonPacket());
					return;
				}
			}
		}
	}
}
