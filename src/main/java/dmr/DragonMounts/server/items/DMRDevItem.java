package dmr.DragonMounts.server.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class DMRDevItem extends Item {
	public DMRDevItem()
	{
		super(new Properties().rarity(Rarity.EPIC).stacksTo(1));
	}
}
