package dmr.DragonMounts.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.types.abilities.types.Ability;
import java.util.List;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class DragonAbilityButton extends ExtendedButton {

	private static final ResourceLocation TITLE_BOX_SPRITE = ResourceLocation.withDefaultNamespace("advancements/title_box");

	private ResourceLocation ABILITY_ICON;
	private final Minecraft minecraft;

	private final FormattedCharSequence title;
	private final int width;
	public final List<FormattedCharSequence> description;

	public DragonAbilityButton(int xPos, int yPos, Ability ability) {
		super(xPos, yPos, 120, 32, Component.empty(), bt -> {});
		ABILITY_ICON = DMR.id("textures/gui/ability_icons/" + ability.type() + ".png");
		this.minecraft = Minecraft.getInstance();
		this.title = Language.getInstance().getVisualOrder(minecraft.font.substrByWidth(ability.getTranslatedName(), 163));
		int j = 29 + minecraft.font.width(this.title);
		this.description = Language.getInstance().getVisualOrder(this.findOptimalLines(ability.getTranslatedDescription().copy(), 110));

		for (FormattedCharSequence formattedcharsequence : this.description) {
			j = Math.max(j, minecraft.font.width(formattedcharsequence));
		}

		this.width = Math.max(j + 3 + 5, 120);
	}

	@Override
	public void playDownSound(SoundManager pHandler) {
		// no sound
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTick) {
		guiGraphics.pose().pushPose();
		RenderSystem.enableBlend();
		int l = getY() - 6;
		int i1 = getX();
		int j1 = 40 + this.description.size() * 9;
		if (!this.description.isEmpty()) {
			guiGraphics.blitSprite(TITLE_BOX_SPRITE, i1, l, this.width, j1);
		}

		l += 5;
		i1 += 2;

		guiGraphics.pose().pushPose();
		guiGraphics.blitSprite(AdvancementWidgetType.OBTAINED.boxSprite(), 200, 26, 200, 0, i1 + 2, l, this.width - 10, 26);
		guiGraphics.blitSprite(AdvancementWidgetType.OBTAINED.boxSprite(), 200, 26, 200 + 190, 0, i1 + (this.width - 15), l, 10, 26);
		guiGraphics.blitSprite(AdvancementWidgetType.OBTAINED.frameSprite(AdvancementType.TASK), i1, l, 26, 26);
		guiGraphics.pose().popPose();

		guiGraphics.pose().pushPose();
		guiGraphics.blit(ABILITY_ICON, i1 + 5, l + 5, 0, 0, 16, 16, 16, 16);
		guiGraphics.pose().popPose();

		guiGraphics.drawCenteredString(this.minecraft.font, this.title, getX() + 28 + 43, getY() + 8, -1);

		for (int k1 = 0; k1 < this.description.size(); k1++) {
			guiGraphics.drawString(this.minecraft.font, this.description.get(k1), i1 + 5, l + 27 + k1 * 9, -5592406, false);
		}
		guiGraphics.pose().popPose();
	}

	private static final int[] TEST_SPLIT_OFFSETS = new int[] { 0, 10, -10, 25, -25 };

	private static float getMaxWidth(StringSplitter manager, List<FormattedText> text) {
		return (float) text.stream().mapToDouble(manager::stringWidth).max().orElse(0.0);
	}

	private List<FormattedText> findOptimalLines(Component component, int maxWidth) {
		StringSplitter stringsplitter = this.minecraft.font.getSplitter();
		List<FormattedText> list = null;
		float f = Float.MAX_VALUE;

		for (int i : TEST_SPLIT_OFFSETS) {
			List<FormattedText> list1 = stringsplitter.splitLines(component, maxWidth - i, Style.EMPTY);
			float f1 = Math.abs(getMaxWidth(stringsplitter, list1) - (float) maxWidth);
			if (f1 <= 10.0F) {
				return list1;
			}

			if (f1 < f) {
				f = f1;
				list = list1;
			}
		}

		return list;
	}
}
