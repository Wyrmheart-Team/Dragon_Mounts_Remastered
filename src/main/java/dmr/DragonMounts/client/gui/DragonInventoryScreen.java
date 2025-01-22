package dmr.DragonMounts.client.gui;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.abilities.DragonAbility;
import dmr.DragonMounts.network.packets.DragonStatePacket;
import dmr.DragonMounts.registry.DragonAbilityRegistry;
import dmr.DragonMounts.server.container.DragonContainerMenu;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class DragonInventoryScreen extends AbstractContainerScreen<DragonContainerMenu> {

	private static final ResourceLocation INVENTORY_LOCATION = DMR.id("textures/gui/dragon.png");
	private final DMRDragonEntity dragon;
	/**
	 * The mouse x-position recorded during the last rendered frame.
	 */
	private float xMouse;
	/**
	 * The mouse y-position recorded during the last rendered frame.
	 */
	private float yMouse;

	public DragonInventoryScreen(DragonContainerMenu pMenu, Inventory pPlayerInventory, Component component) {
		super(pMenu, pPlayerInventory, component);
		this.dragon = pMenu.dragon;
	}

	@Override
	protected void init() {
		super.init();
		int xSize = 176;
		int ySize = 233;
		leftPos = (width - xSize) / 2;
		topPos = (height - ySize) / 2;

		addRenderableWidget(
			new ExtendedButton(leftPos + 114, topPos + 7, 55, 20, Component.translatable("dmr.inventory.sit"), p_214087_1_ -> {
				PacketDistributor.sendToServer(new DragonStatePacket(dragon.getId(), 0));
			})
		);

		addRenderableWidget(
			new ExtendedButton(leftPos + 114, topPos + 31, 55, 20, Component.translatable("dmr.inventory.follow"), p_214087_1_ -> {
				PacketDistributor.sendToServer(new DragonStatePacket(dragon.getId(), 1));
			})
		);

		addRenderableWidget(
			new ExtendedButton(leftPos + 114, topPos + 55, 55, 20, Component.translatable("dmr.inventory.wander"), p_214087_1_ -> {
				PacketDistributor.sendToServer(new DragonStatePacket(dragon.getId(), 2));
			})
		);

		int i = 0;
		int offsetY = 0;
		for (DragonAbility ability : dragon.getBreed().getAbilities().stream().map(DragonAbilityRegistry::getDragonAbility).toList()) {
			var btn = new DragonAbilityButton(leftPos - 124, topPos + 5 + offsetY, ability);
			addRenderableWidget(btn);
			offsetY += 40 + btn.description.size() * 9;
			i++;
		}
	}

	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		this.xMouse = (float) pMouseX;
		this.yMouse = (float) pMouseY;
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
	}

	protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
		pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

		if (dragon.hasChest()) {
			if (dragon.inventory.getItem(DMRDragonEntity.CHEST_SLOT).is(Items.ENDER_CHEST)) {
				pGuiGraphics.drawString(
					this.font,
					Component.translatable("container.enderchest"),
					this.inventoryLabelX,
					this.inventoryLabelY,
					4210752,
					false
				);
				return;
			}
		}

		pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
	}

	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		pGuiGraphics.blit(INVENTORY_LOCATION, leftPos, topPos, 0, 0, 176, 233, 512, 512);

		if (dragon.hasChest()) {
			pGuiGraphics.blit(INVENTORY_LOCATION, leftPos + 7, topPos + 83, 176, 0, 9 * 18, 54, 512, 512);
		}

		InventoryScreen.renderEntityInInventoryFollowsMouse(
			pGuiGraphics,
			leftPos + 28,
			topPos + 18,
			leftPos + 28 + 81,
			topPos + 18 + 52,
			10,
			1,
			this.xMouse,
			this.yMouse,
			this.dragon
		);
	}
}
