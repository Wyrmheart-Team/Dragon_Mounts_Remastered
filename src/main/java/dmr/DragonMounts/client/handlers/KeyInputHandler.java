package dmr.DragonMounts.client.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.client.gui.CommandMenu.CommandMenuScreen;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.network.packets.DismountDragonPacket;
import dmr.DragonMounts.network.packets.SummonDragonPacket;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
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
	private static boolean lastWheelState = false;

	@SubscribeEvent
	public static void registerBindings(RegisterKeyMappingsEvent event) {
		event.register(SUMMON_DRAGON);
		event.register(ATTACK_KEY);
		event.register(DRAGON_COMMAND_KEY);
		event.register(DISMOUNT_KEY);
		event.register(DESCEND_KEY);
	}

	public static void onKeyboardTick() {
		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null) {
			return;
		}

		if (mc.player == null) {
			return;
		}

		ItemStack heldItem = mc.player.getMainHandItem();

		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof DragonWhistleItem whistleItem)) {
			// If the player is not holding a dragon whistle, return
			return;
		}

		var capability = PlayerStateUtils.getHandler(mc.player);

		if (!capability.dragonInstances.containsKey(whistleItem.getColor().getId())) {
			return;
		}

		long handle = Minecraft.getInstance().getWindow().getWindow();
		int keycode = DRAGON_COMMAND_KEY.getKey().getValue();
		if (keycode >= 0) {
			boolean radialMenuKeyDown =
				(DRAGON_COMMAND_KEY.matchesMouse(keycode)
						? GLFW.glfwGetMouseButton(handle, keycode) == 1
						: InputConstants.isKeyDown(handle, keycode));
			if (radialMenuKeyDown != lastWheelState) {
				if (radialMenuKeyDown != CommandMenuScreen.active) {
					if (radialMenuKeyDown) {
						if (mc.screen == null || mc.screen instanceof CommandMenuScreen) {
							CommandOverlayHandler.resetTimer();
							CommandMenuScreen.activate();
						}
					} else {
						CommandMenuScreen.INSTANCE.mouseClicked(mc.mouseHandler.xpos(), mc.mouseHandler.ypos(), 0);
						CommandMenuScreen.deactivate();
					}
				}
			}
			lastWheelState = radialMenuKeyDown;
		}
	}

	@OnlyIn(Dist.CLIENT)
	@EventBusSubscriber(modid = DMR.MOD_ID, value = Dist.CLIENT, bus = Bus.GAME)
	public static class KeyClickHandler {

		private static Long lastUnshift = null;
		private static boolean wasShiftDown = false;

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void clientTick(ClientTickEvent.Pre event) {
			onKeyboardTick();
		}

		@OnlyIn(Dist.CLIENT)
		@SubscribeEvent
		public static void clientTick(ClientTickEvent.Post event) {
			if (Minecraft.getInstance().level == null) return;
			if (Minecraft.getInstance().player == null) return;
			if (Minecraft.getInstance().screen != null) return;

			var player = Minecraft.getInstance().player;

			if (player.getControlledVehicle() instanceof DMRDragonEntity) {
				if (Minecraft.getInstance().options.keyShift.consumeClick()) {
					wasShiftDown = true;

					if (ClientConfig.DOUBLE_PRESS_DISMOUNT.get()) {
						if (
							lastUnshift != null &&
							System.currentTimeMillis() > lastUnshift &&
							System.currentTimeMillis() < lastUnshift + TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS)
						) {
							PacketDistributor.sendToServer(new DismountDragonPacket(player.getId(), true));
							lastUnshift = null;
							wasShiftDown = false;
						}
					} else {
						PacketDistributor.sendToServer(new DismountDragonPacket(player.getId(), true));
					}
					return;
				} else if (wasShiftDown && !Minecraft.getInstance().options.keyShift.isDown()) {
					lastUnshift = System.currentTimeMillis();
					wasShiftDown = false;
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
