package dmr.DragonMounts.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.types.abilities.Ability;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class DragonAbilityButton extends ExtendedButton {

    private static final ResourceLocation TITLE_BOX_SPRITE =
            ResourceLocation.withDefaultNamespace("advancements/title_box");
    private static final ResourceLocation INVENTORY_LOCATION = DMR.id("textures/gui/dragon_inventory.png");

    private ResourceLocation ABILITY_ICON;
    private Minecraft minecraft;

    private FormattedCharSequence title;
    private int width;
    public List<FormattedCharSequence> description;

    public DragonAbilityButton(int xPos, int yPos, Ability ability) {
        super(xPos, yPos, 24, 24, Component.empty(), bt -> {});
        if (ability != null) {
            ABILITY_ICON = DMR.id("textures/gui/ability_icons/" + ability.type() + ".png");
            this.minecraft = Minecraft.getInstance();
            this.title = Language.getInstance()
                    .getVisualOrder(minecraft.font.substrByWidth(ability.getTranslatedName(), 160));
            this.description = ComponentRenderUtils.wrapComponents(
                    ability.getTranslatedDescription().copy(), 120, this.minecraft.font);

            this.width = 29 + minecraft.font.width(this.title);
            for (FormattedCharSequence formattedcharsequence : this.description) {
                this.width = Math.max(this.width, minecraft.font.width(formattedcharsequence) + 15);
            }
        }
    }

    @Override
    public void playDownSound(SoundManager pHandler) {
        // no sound
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        guiGraphics.blit(INVENTORY_LOCATION, getX(), getY(), 274, 0, 24, 24, 512, 512);
        if (ABILITY_ICON == null) {
            guiGraphics.fill(getX() + 1, getY() + 1, getX() + 23, getY() + 23, FastColor.ARGB32.color(200, 0x5B5B5B));
            return;
        }

        guiGraphics.blit(ABILITY_ICON, getX() + 2, getY() + 2, 0, 0, 20, 20, 20, 20);

        if (isHovered()) {
            guiGraphics.pose().pushPose();
            RenderSystem.enableBlend();
            guiGraphics.pose().translate(0, 0, 1000);
            int l = getY() - 6;
            int i1 = getX();
            int j1 = 40 + this.description.size() * 9;
            if (!this.description.isEmpty()) {
                guiGraphics.blitSprite(TITLE_BOX_SPRITE, i1, l, this.width, j1);
            }

            l += 5;
            i1 += 2;

            guiGraphics.pose().pushPose();
            guiGraphics.blitSprite(
                    ResourceLocation.withDefaultNamespace("advancements/box_obtained"),
                    200,
                    26,
                    200,
                    0,
                    i1 + 2,
                    l,
                    this.width - 10,
                    26);
            guiGraphics.blitSprite(
                    ResourceLocation.withDefaultNamespace("advancements/box_obtained"),
                    200,
                    26,
                    200 + 190,
                    0,
                    i1 + (this.width - 15),
                    l,
                    10,
                    26);
            guiGraphics.blitSprite(
                    ResourceLocation.withDefaultNamespace("advancements/task_frame_obtained"), i1, l, 26, 26);
            guiGraphics.pose().popPose();

            guiGraphics.pose().pushPose();
            guiGraphics.blit(ABILITY_ICON, i1 + 5, l + 5, 0, 0, 16, 16, 16, 16);
            guiGraphics.pose().popPose();

            guiGraphics.drawCenteredString(
                    this.minecraft.font, this.title, getX() + 28 + ((this.width - 28 - 5) / 2), getY() + 8, -1);

            for (int k1 = 0; k1 < this.description.size(); k1++) {
                guiGraphics.drawString(
                        this.minecraft.font, this.description.get(k1), i1 + 5, l + 27 + k1 * 9, -5592406, false);
            }
            guiGraphics.pose().popPose();
        }
    }
}
