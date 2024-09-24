package compatibility.jei;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class DMRJeiPlugin implements IModPlugin
{
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


class DragonSpawnEggInterpreter implements ISubtypeInterpreter<ItemStack>{
	
	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context)
	{
		return ingredient.get(DataComponents.CUSTOM_DATA);
	}
	
	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context)
	{
		if(ingredient.has(DataComponents.CUSTOM_DATA)) {
			var entityTag = ingredient.get(DataComponents.CUSTOM_DATA).copyTag();
			
			if (entityTag.contains(NBTConstants.BREED)) {
				return entityTag.getString(NBTConstants.BREED);
			}
		}
		return "NONE";
	}
}

class DragonEggInterpreter implements ISubtypeInterpreter<ItemStack>{
	
	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context)
	{
		return ingredient.get(DataComponents.CUSTOM_DATA);
	}
	
	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context)
	{
		if(ingredient.has(DataComponents.CUSTOM_DATA)) {
			var entityTag = ingredient.get(DataComponents.CUSTOM_DATA).copyTag();
			
			if (entityTag.contains(NBTConstants.BREED)) {
				return entityTag.getString(NBTConstants.BREED);
			}
		}
		return "NONE";
	}
}

class DragonArmorInterpreter implements ISubtypeInterpreter<ItemStack>{
	
	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context)
	{
		return ingredient.get(DataComponents.CUSTOM_DATA);
	}
	
	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context)
	{
		if(ingredient.has(DataComponents.CUSTOM_DATA)) {
			var entityTag = ingredient.get(DataComponents.CUSTOM_DATA).copyTag();
			
			if (entityTag.contains(NBTConstants.ARMOR)) {
				return entityTag.getString(NBTConstants.ARMOR);
			}
		}
		return "NONE";
	}
}