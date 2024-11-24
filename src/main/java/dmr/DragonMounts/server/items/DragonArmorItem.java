package dmr.DragonMounts.server.items;

import dmr.DragonMounts.registry.DMRComponents;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.types.armor.DragonArmor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DragonArmorItem extends Item {
	public DragonArmorItem(Properties pProperties)
	{
		super(pProperties);
	}
	
	public static ItemStack getArmorStack(DragonArmor type)
	{
		return getArmorStack(type, 1);
	}
	
	public static ItemStack getArmorStack(DragonArmor type, int count)
	{
		ItemStack stack = new ItemStack(DMRItems.DRAGON_ARMOR.get(), count);
		DragonArmor.setArmorType(stack, type);
		return stack;
	}
	
	@Override
	public String getDescriptionId(ItemStack pStack)
	{
		var type = pStack.get(DMRComponents.ARMOR_TYPE); return String.join(".", DMRItems.DRAGON_ARMOR.get().getDescriptionId(), type);
	}
}
