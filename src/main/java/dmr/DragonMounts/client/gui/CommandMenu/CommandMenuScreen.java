package dmr.DragonMounts.client.gui.CommandMenu;

import dmr.DragonMounts.client.handlers.CommandOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class CommandMenuScreen extends Screen {

	public static final CommandMenuScreen INSTANCE = new CommandMenuScreen();
	public static boolean active = false;

	public CommandMenuScreen() {
		super(Component.empty());
	}

	public static void activate() {
		if (Minecraft.getInstance().screen == null) {
			active = true;
			Minecraft.getInstance().setScreen(INSTANCE);
		}
	}

	public static void deactivate() {
		active = false;
		if (Minecraft.getInstance().screen == INSTANCE) {
			Minecraft.getInstance().setScreen(null);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (active && CommandOverlayHandler.animationTimer == 0) {
			Minecraft mc = Minecraft.getInstance();

			// Get the mouse angle.
			double mouseAngle = CommandOverlayHandler.correctAngle(
				CommandOverlayHandler.getMouseAngle() + (CommandOverlayHandler.ANGLE_PER_ITEM + 39)
			);

			float centerX = (float) (mc.getWindow().getGuiScaledWidth() * 0.5D);
			float centerY = (float) (mc.getWindow().getGuiScaledHeight() * 0.5D);

			// Compute mouse distance from center in GUI coordinates.
			// We convert the raw screen coordinates into GUI coordinates.
			double scaleX = (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
			double scaleY = (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
			double guiMouseX = mc.mouseHandler.xpos() * scaleX;
			double guiMouseY = mc.mouseHandler.ypos() * scaleY;
			double dx = guiMouseX - centerX;
			double dy = guiMouseY - centerY;
			double mouseDistance = Math.sqrt(dx * dx + dy * dy);

			if (!mc.options.hideGui) {
				for (int i = 0; i < CommandOverlayHandler.MAX_ITEMS; i++) {
					// Segments in the unrotated system start at 0 degrees.
					double segStart = CommandOverlayHandler.ANGLE_PER_ITEM * i + 1;
					double segEnd = segStart + CommandOverlayHandler.ANGLE_PER_ITEM - 2;

					if (
						mouseDistance < (CommandOverlayHandler.INNER_RADIUS * 1.3f) ||
						mouseDistance > (CommandOverlayHandler.OUTER_RADIUS * 1.3f)
					) {
						continue;
					}

					// Check if the adjusted mouse angle falls within this segment.
					if (mouseAngle >= segStart && mouseAngle < segEnd) {
						CommandOverlayHandler.MenuItem menuItem = CommandOverlayHandler.getItems()[(i) % CommandOverlayHandler.MAX_ITEMS];
						if (menuItem != null && menuItem.clickListener() != null) {
							menuItem.clickListener().onClick(mc.player, mc.player.getMainHandItem());
							deactivate();
						}
						break;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void removed() {
		super.removed();
		active = false;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void renderBackground(@Nonnull GuiGraphics guiGraphics, int i, int i1, float i2) {}
}
