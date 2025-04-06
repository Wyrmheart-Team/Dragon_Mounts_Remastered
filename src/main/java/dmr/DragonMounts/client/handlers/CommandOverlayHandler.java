package dmr.DragonMounts.client.handlers;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.client.gui.CommandMenu.CommandMenuScreen;
import dmr.DragonMounts.client.gui.DragonAbilityButton;
import dmr.DragonMounts.network.packets.DragonCommandPacket;
import dmr.DragonMounts.network.packets.DragonCommandPacket.Command;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4fStack;

/**
 * Based on code from the MineMenu mod, <a href="https://github.com/GirafiStudios/MineMenu">Github</a>
 */
@EventBusSubscriber(modid = DMR.MOD_ID, value = Dist.CLIENT)
public class CommandOverlayHandler {

	public static Color selectedColor = new Color(163, 163, 163, 200);
	public static Color unselectedColor = new Color(98, 98, 98, 128);

	public static void tickTimer() {
		if (animationTimer > 0) {
			animationTimer -= 5;
		}
	}

	public static void resetTimer() {
		animationTimer = 20;
	}

	@FunctionalInterface
	public interface MenuItemClickListener {
		void onClick(LocalPlayer player, ItemStack stack);
	}

	public static double getMouseAngle() {
		Minecraft mc = Minecraft.getInstance();
		return getRelativeAngle(
			mc.getWindow().getScreenWidth() * 0.5D,
			mc.getWindow().getScreenHeight() * 0.5D,
			mc.mouseHandler.xpos(),
			mc.mouseHandler.ypos()
		);
	}

	private static double getRelativeAngle(double originX, double originY, double x, double y) {
		double angle = -Math.toDegrees(Math.atan2(x - originX, y - originY));

		return correctAngle(angle);
	}

	public static double correctAngle(double angle) {
		if (angle < 0) {
			angle += 360;
		} else if (angle > 360) {
			angle -= 360;
		}
		return angle;
	}

	public record MenuItem(Component title, MenuItemClickListener clickListener) {}

	static final MenuItem[] activeArray = new MenuItem[] {
		new MenuItem(Component.translatable("dmr.command_mode.sit"), (player, stack) -> {
			PacketDistributor.sendToServer(new DragonCommandPacket(Command.SIT));
		}),
		new MenuItem(Component.translatable("dmr.command_mode.follow"), (player, stack) -> {
			PacketDistributor.sendToServer(new DragonCommandPacket(Command.FOLLOW));
		}),
		new MenuItem(Component.translatable("dmr.command_mode.wander"), (player, stack) -> {
			PacketDistributor.sendToServer(new DragonCommandPacket(Command.WANDER));
		}),
		new MenuItem(Component.translatable("dmr.command_mode.whistle"), (player, stack) -> {
			PacketDistributor.sendToServer(new DragonCommandPacket(Command.WHISTLE));
		}),
		new MenuItem(Component.translatable("dmr.command_mode.passive"), (player, stack) -> {
			PacketDistributor.sendToServer(new DragonCommandPacket(Command.PASSIVE));
		}),
		new MenuItem(Component.translatable("dmr.command_mode.neutral"), (player, stack) -> {
			PacketDistributor.sendToServer(new DragonCommandPacket(Command.NEUTRAL));
		}),
		new MenuItem(Component.translatable("dmr.command_mode.aggressive"), (player, stack) -> {
			PacketDistributor.sendToServer(new DragonCommandPacket(Command.AGGRESSIVE));
		})
	};

	public static MenuItem[] getItems() {
		return activeArray;
	}

	public static final int MAX_ITEMS = activeArray.length;
	public static final double ANGLE_PER_ITEM = 360F / MAX_ITEMS;
	public static final float OUTER_RADIUS = 50;
	public static final float INNER_RADIUS = 10;
	public static int animationTimer = 20;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		tickTimer();

		Minecraft mc = Minecraft.getInstance();
		if ((mc.level == null || mc.isPaused()) && CommandMenuScreen.active) {
			CommandMenuScreen.deactivate();
		}
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
		if (!(event.getName().equals(VanillaGuiLayers.TAB_LIST))) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && !mc.options.hideGui && !mc.isPaused() && CommandMenuScreen.active) {
			GuiGraphics guiGraphics = event.getGuiGraphics();
			renderButtonBackgrounds();
			renderItems(guiGraphics);
		}
	}

	public static void renderButtonBackgrounds() {
		Minecraft mc = Minecraft.getInstance();
		var player = mc.player;
		var itemStack = player.getMainHandItem();

		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof DragonWhistleItem whistleItem)) {
			return;
		}

		var bsColor = new Color(whistleItem.getColor().getTextureDiffuseColor()).darker();

		Matrix4fStack matrix = RenderSystem.getModelViewStack();
		matrix.pushMatrix();

		// Center the origin.
		float centerX = (float) (mc.getWindow().getGuiScaledWidth() * 0.5D);
		float centerY = (float) (mc.getWindow().getGuiScaledHeight() * 0.5D);
		matrix.translate(centerX, centerY, 0);
		RenderSystem.applyModelViewMatrix();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		// Adjust the mouse angle so it falls between segments.
		double mouseAngle = correctAngle(getMouseAngle() + (ANGLE_PER_ITEM + 39));

		// Compute mouse distance from center in GUI coordinates.
		// We convert the raw screen coordinates into GUI coordinates.
		double scaleX = (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
		double scaleY = (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
		double guiMouseX = mc.mouseHandler.xpos() * scaleX;
		double guiMouseY = mc.mouseHandler.ypos() * scaleY;
		double dx = guiMouseX - centerX;
		double dy = guiMouseY - centerY;
		double mouseDistance = Math.sqrt(dx * dx + dy * dy);

		// Begin the tessellation using QUADS.
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		// Loop over each segment.
		for (int i = 0; i < MAX_ITEMS; i++) {
			double trueSegStart = ANGLE_PER_ITEM * i + 1;
			double trueSegEnd = trueSegStart + ANGLE_PER_ITEM - 2;

			// Determine whether the mouse is in this segment.
			boolean mouseIn = (mouseAngle > trueSegStart && mouseAngle < trueSegEnd);

			// If the mouse pointer is within the inner radius, force mouseIn to false.
			if (mouseDistance < (INNER_RADIUS * 1.3f) || mouseDistance > (OUTER_RADIUS * 1.3f)) {
				mouseIn = false;
			}

			// Adjust inner and outer radii based on animation and mouse hover.
			float innerRadius = (((INNER_RADIUS - animationTimer) / 100F) * 130F);
			float outerRadius = (((OUTER_RADIUS - animationTimer) / 100F) * 130F);

			// Use subdivisions for a smooth arc along this segment.
			int subdivisions = 2;
			for (int j = 0; j < subdivisions; j++) {
				double t1 = (double) j / subdivisions;
				double t2 = (double) (j + 1) / subdivisions;

				double angle1 = Math.toRadians(trueSegStart + t1 * (trueSegEnd - trueSegStart));
				double angle2 = Math.toRadians(trueSegStart + t2 * (trueSegEnd - trueSegStart));

				// Choose the proper color.
				var color = mouseIn ? selectedColor : unselectedColor;
				var useWhistleColor = true;
				var bgColor = useWhistleColor ? bsColor : color;
				float r = bgColor.getRed() / 255F;
				float g = bgColor.getGreen() / 255F;
				float b = bgColor.getBlue() / 255F;
				float a = color.getAlpha() / 255F;

				// Compute the positions for outer and inner points.
				float xOuter1 = (float) (Math.cos(angle1) * outerRadius);
				float yOuter1 = (float) (Math.sin(angle1) * outerRadius);

				float xOuter2 = (float) (Math.cos(angle2) * outerRadius);
				float yOuter2 = (float) (Math.sin(angle2) * outerRadius);

				float xInner2 = (float) (Math.cos(angle2) * innerRadius);
				float yInner2 = (float) (Math.sin(angle2) * innerRadius);

				float xInner1 = (float) (Math.cos(angle1) * innerRadius);
				float yInner1 = (float) (Math.sin(angle1) * innerRadius);

				// Build the quad (four vertices) for this small sub-arc.
				// Order: outer vertex at angle1, outer vertex at angle2,
				// inner vertex at angle2, inner vertex at angle1.
				bufferBuilder.addVertex(xOuter1, yOuter1, 0).setColor(r, g, b, a);
				bufferBuilder.addVertex(xOuter2, yOuter2, 0).setColor(r, g, b, a);
				bufferBuilder.addVertex(xInner2, yInner2, 0).setColor(r, g, b, a);
				bufferBuilder.addVertex(xInner1, yInner1, 0).setColor(r, g, b, a);
			}
		}

		// Now upload the built vertices.
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

		RenderSystem.enableCull();
		RenderSystem.disableBlend();

		matrix.popMatrix();
		RenderSystem.applyModelViewMatrix();
	}

	public static void renderItems(GuiGraphics guiGraphics) {
		Minecraft mc = Minecraft.getInstance();
		Window window = mc.getWindow();
		Font fontRenderer = mc.font;
		PoseStack poseStack = guiGraphics.pose();

		// Center the whole UI.
		poseStack.pushPose();
		poseStack.translate(window.getGuiScaledWidth() * 0.5D, window.getGuiScaledHeight() * 0.5D, 0);

		for (int i = 0; i < MAX_ITEMS; i++) {
			MenuItem item = getItems()[i];

			double angle = ANGLE_PER_ITEM * i + ANGLE_PER_ITEM / 2;
			// --- Drawing the text ---
			double textDistance = (OUTER_RADIUS) - animationTimer;
			double textX = textDistance * Math.cos(Math.toRadians(angle));
			double textY = textDistance * Math.sin(Math.toRadians(angle));

			var string = item.title();
			var lines = DragonAbilityButton.findOptimalLines(mc, string, 60);
			var text = Language.getInstance().getVisualOrder(lines);

			poseStack.pushPose();
			// Translate to where the text should be drawn.
			poseStack.translate(textX, textY, 0);

			poseStack.scale(0.5F, 0.5F, 1.0F);
			// Draw centered text.
			for (int k1 = 0; k1 < text.size(); k1++) {
				guiGraphics.drawCenteredString(fontRenderer, text.get(k1), 0, -5 + k1 * 9, 0xFFFFFF);
			}
			poseStack.popPose();
		}
		poseStack.popPose();
	}
}
