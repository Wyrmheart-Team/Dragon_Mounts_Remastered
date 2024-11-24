package dmr.DragonMounts.server.container.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class DragonInventorySlot extends Slot {

	private final Container enderChest;
	private final Container dragonInv;
	private final int origId;

	public DragonInventorySlot(int pSlot, int pX, int pY, Container dragonInv, Container enderChest) {
		super(dragonInv, pSlot + 3, pX, pY);
		this.origId = pSlot;
		this.dragonInv = dragonInv;
		this.enderChest = enderChest;
	}

	public void setChestTypeChanged(boolean enderChest) {
		if (enderChest) {
			this.container = this.enderChest;
			this.slot = this.origId;
		} else {
			this.container = this.dragonInv;
			this.slot = this.origId + 3;
		}
	}
}
