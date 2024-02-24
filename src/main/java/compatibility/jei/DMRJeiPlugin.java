package compatibility.jei;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

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
		registration.registerSubtypeInterpreter(DMRItems.DRAGON_SPAWN_EGG.get(), (stack, context) -> {
			if(stack.getTag() != null ) {
				if (stack.getTag().contains(EntityType.ENTITY_TAG)) {
					var entityTag = stack.getTag().getCompound(EntityType.ENTITY_TAG);
					
					if (entityTag.contains(NBTConstants.BREED)) {
						return entityTag.getString(NBTConstants.BREED);
					}
				}
			}
			return "NONE";
		});
		registration.registerSubtypeInterpreter(DMRItems.DRAGON_EGG_BLOCK_ITEM.get(), (stack, context) -> {
			if(stack.getTag() != null && stack.getTag().contains(NBTConstants.BREED)) {
				return stack.getTag().getString(NBTConstants.BREED);
			}
			return "NONE";
		});
		
		registration.registerSubtypeInterpreter(DMRItems.DRAGON_ARMOR.get(), (stack, context) -> {
			if(stack.getTag() != null && stack.getTag().contains(NBTConstants.ARMOR)) {
				return stack.getTag().getString(NBTConstants.ARMOR);
			}
			return "NONE";
		});
	}
}
