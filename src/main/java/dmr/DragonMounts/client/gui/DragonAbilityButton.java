package dmr.DragonMounts.client.gui;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class DragonAbilityButton extends ExtendedButton {
	private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE = ResourceLocation.parse("container/inventory/effect_background_large");
	private ResourceLocation ABILITY_ICON;
	
	private final Ability ability;
	
	public DragonAbilityButton(int xPos, int yPos, Ability ability)
	{
		super(xPos, yPos, 120, 32, Component.empty(), (bt) -> {});
		this.ability = ability;
		this.setTooltip(Tooltip.create(ability.getTranslatedDescription()));
		
		ABILITY_ICON = DragonMountsRemaster.id("textures/gui/ability_icons/" + ability.type() + ".png");
		
		if (Minecraft.getInstance().getTextureManager().getTexture(ABILITY_ICON, null) == null) {
			ABILITY_ICON = DragonMountsRemaster.id("textures/gui/ability_icons/missing.png");
		}
	}
	
	@Override
	public void playDownSound(SoundManager pHandler)
	{
		// no sound
	}
	
	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
	{
		guiGraphics.blitSprite(EFFECT_BACKGROUND_LARGE_SPRITE, getX(), getY(), getWidth(), getHeight());
		guiGraphics.blit(ABILITY_ICON, getX() + 7, getY() + 8, 0, 0, 16, 16, 16, 16);
		guiGraphics.drawString(Minecraft.getInstance().font, ability.getTranslatedName(), getX() + 10 + 18, getY() + 6 + 5, 16777215);
	}
}
