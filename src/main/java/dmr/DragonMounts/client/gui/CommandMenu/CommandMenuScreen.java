package dmr.DragonMounts.client.gui.CommandMenu;

import com.mojang.blaze3d.platform.InputConstants;
import dmr.DragonMounts.client.handlers.CommandOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

import static dmr.DragonMounts.client.handlers.CommandOverlayHandler.ANGLE_PER_ITEM;

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
				CommandOverlayHandler.getMouseAngle() + (2.5f * ANGLE_PER_ITEM)
			);
			
			// Compute mouse distance from center in GUI coordinates.
			// We convert the raw screen coordinates into GUI coordinates.
			double mouseDistance = CommandOverlayHandler.getMouseDistance();
			
			if (!mc.options.hideGui) {
				for (int i = 0; i < CommandOverlayHandler.MAX_ITEMS; i++) {
					// Segments in the unrotated system start at 0 degrees.
					double segStart = ANGLE_PER_ITEM * i;
					double segEnd = segStart + ANGLE_PER_ITEM;

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
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void renderBackground(@Nonnull GuiGraphics guiGraphics, int i, int i1, float i2) {}
}
