package compatibility.jei;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.registry.DMRComponents;
import dmr.DragonMounts.registry.DMRItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class DMRJeiPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid()
	{
		return DragonMountsRemaster.id("jei_plugin");
	}
	
	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration)
	{
		registration.registerSubtypeInterpreter(DMRItems.DRAGON_SPAWN_EGG.get(), new DragonSpawnEggInterpreter());
		registration.registerSubtypeInterpreter(DMRItems.DRAGON_EGG_BLOCK_ITEM.get(), new DragonEggInterpreter());
		registration.registerSubtypeInterpreter(DMRItems.DRAGON_ARMOR.get(), new DragonArmorInterpreter());
	}
}


class DragonSpawnEggInterpreter implements ISubtypeInterpreter<ItemStack> {
	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context)
	{
		return ingredient.getOrDefault(DMRComponents.DRAGON_BREED, "NONE");
	}
	
	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context)
	{
		return "";
	}
}

class DragonEggInterpreter implements ISubtypeInterpreter<ItemStack> {
	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context)
	{
		return ingredient.getOrDefault(DMRComponents.DRAGON_BREED, "NONE");
	}
	
	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context)
	{
		return "";
	}
}

class DragonArmorInterpreter implements ISubtypeInterpreter<ItemStack> {
	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context)
	{
		return ingredient.getOrDefault(DMRComponents.ARMOR_TYPE, "NONE");
	}
	
	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context)
	{
		return "";
	}
}