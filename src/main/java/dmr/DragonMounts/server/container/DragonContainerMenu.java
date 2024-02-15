package dmr.DragonMounts.server.container;

import dmr.DragonMounts.registry.DMRMenus;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DragonContainerMenu extends AbstractContainerMenu
{
	private final Container dragonContainer;
	public final DMRDragonEntity dragon;
	
	public DragonContainerMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf data)
	{
		super(DMRMenus.DRAGON_MENU.get(), pContainerId);
		dragon = (DMRDragonEntity)pPlayerInventory.player.level.getEntity(data.readInt());
		this.dragonContainer = dragon.inventory;
		
		dragonContainer.startOpen(pPlayerInventory.player);
		this.addSlot(new Slot(dragonContainer, DMRDragonEntity.SADDLE_SLOT, 8, 18) {
			public boolean mayPlace(ItemStack p_39677_) {
				return p_39677_.is(Items.SADDLE) && !this.hasItem() && dragon.isSaddleable();
			}
			
			public boolean isActive() {
				return dragon.isSaddleable();
			}
			
			@Override
			public void set(ItemStack pStack)
			{
				super.set(pStack);
				dragon.updateContainerEquipment();
				setChanged();
			}
		});

		//TODO Implement armor
//		this.addSlot(new Slot(dragonContainer, DMRDragonEntity.ARMOR_SLOT, 8, 36) {
//			public boolean mayPlace(ItemStack p_39690_) {
//				return dragon.isArmor(p_39690_);
//			}
//
//			public int getMaxStackSize() {
//				return 1;
//			}
//
//			@Override
//			public void set(ItemStack pStack)
//			{
//				super.set(pStack);
//				dragon.updateContainerEquipment();
//			}
//		});
		
		this.addSlot(new Slot(dragonContainer, DMRDragonEntity.CHEST_SLOT, 8, 54) {
			public boolean mayPlace(ItemStack p_39690_) {
				return p_39690_.is(Items.CHEST);
			}
			
			@Override
			public void set(ItemStack pStack)
			{
				super.set(pStack);
				dragon.updateContainerEquipment();
			}
			
			public int getMaxStackSize() {
				return 1;
			}
		});
		
		if (dragon.hasChest()) {
			for(int k = 0; k < 3; ++k) {
				for(int l = 0; l < 9; ++l) {
					this.addSlot(new Slot(dragonContainer, 3 + l + k * 9, 8 + l * 18, 84 + k * 18){
						
						@Override
						public boolean isActive()
						{
							return dragon.hasChest();
						}
					});
				}
			}
		}
		
		for(int i1 = 0; i1 < 3; ++i1) {
			for(int k1 = 0; k1 < 9; ++k1) {
				this.addSlot(new Slot(pPlayerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 151 + i1 * 18));
			}
		}
		
		for(int j1 = 0; j1 < 9; ++j1) {
			this.addSlot(new Slot(pPlayerInventory, j1, 8 + j1 * 18, 209));
		}
	}

	public boolean stillValid(Player pPlayer) {
		return !this.dragon.hasInventoryChanged(this.dragonContainer) && this.dragonContainer.stillValid(pPlayer) && this.dragon.isAlive() && this.dragon.distanceTo(pPlayer) < 8.0F;
	}
	
	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(pIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			int i = this.dragonContainer.getContainerSize();
			if (pIndex < i) {
				if (!this.moveItemStackTo(itemstack1, i, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(2).mayPlace(itemstack1) && !this.getSlot(2).hasItem()) {
				if (!this.moveItemStackTo(itemstack1, 1, 3, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
				if (!this.moveItemStackTo(itemstack1, 1, 3, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(0).mayPlace(itemstack1)) {
				if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i <= 3 || !this.moveItemStackTo(itemstack1, 3, i, false)) {
				int j = i + 27;
				int k = j + 9;
				if (pIndex >= j && pIndex < k) {
					if (!this.moveItemStackTo(itemstack1, i, j, false)) {
						return ItemStack.EMPTY;
					}
				} else if (pIndex >= i && pIndex < j) {
					if (!this.moveItemStackTo(itemstack1, j, k, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemstack1, j, j, false)) {
					return ItemStack.EMPTY;
				}
				
				return ItemStack.EMPTY;
			}
			
			if (itemstack1.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		
		return itemstack;
	}
	
	/**
	 * Called when the container is closed.
	 */
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		this.dragonContainer.stopOpen(pPlayer);
	}
}
