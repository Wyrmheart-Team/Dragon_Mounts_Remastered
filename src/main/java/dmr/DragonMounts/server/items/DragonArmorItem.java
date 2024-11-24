package dmr.DragonMounts.server.items;

import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.types.armor.DragonArmor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DragonArmorItem extends Item {

	public DragonArmorItem(Properties pProperties) {
		super(pProperties);
	}

	public static ItemStack getArmorStack(DragonArmor type) {
		return getArmorStack(type, 1);
	}

	public static ItemStack getArmorStack(DragonArmor type, int count) {
		ItemStack stack = new ItemStack(ModItems.DRAGON_ARMOR.get(), count);
		DragonArmor.setArmorType(stack, type);
		return stack;
	}

	@Override
	public String getDescriptionId(ItemStack pStack) {
		var type = pStack.get(ModComponents.ARMOR_TYPE);
		return String.join(".", ModItems.DRAGON_ARMOR.get().getDescriptionId(), type);
	}
}
