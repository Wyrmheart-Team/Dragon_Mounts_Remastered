package dmr.DragonMounts.server.items;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.types.armor.DragonArmor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class DragonArmorItem extends Item
{
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
		var customData = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		
		var tag = customData.copyTag();
		
		if (tag.contains(NBTConstants.ARMOR)) {
			return String.join(".", DMRItems.DRAGON_ARMOR.get().getDescriptionId(), tag.getString(NBTConstants.ARMOR));
		}
		
		return super.getDescriptionId(pStack);
	}
}
