package dmr.DragonMounts.server.items;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.registry.DMREntities;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;


public class DragonSpawnEgg extends DeferredSpawnEggItem
{
	public DragonSpawnEgg()
	{
		super(DMREntities.DRAGON_ENTITY, 0, 0, new Item.Properties());
	}
	
	public static final String DATA_TAG = "ItemData";
	public static final String DATA_ITEM_NAME = "ItemName";
	public static final String DATA_PRIM_COLOR = "PrimaryColor";
	public static final String DATA_SEC_COLOR = "SecondaryColor";
	
	public static ItemStack create(IDragonBreed breed)
	{
		var id = breed.getId();
		var root = new CompoundTag();
		
		// entity tag
		var entityTag = new CompoundTag();
		entityTag.putString(NBTConstants.BREED, id);
		root.put(EntityType.ENTITY_TAG, entityTag);
		
		// name & colors
		// storing these in the stack nbt is more performant than getting the breed everytime
		var itemDataTag = new CompoundTag();
		itemDataTag.putString(DATA_ITEM_NAME, String.join(".", DMRItems.DRAGON_SPAWN_EGG.get().getDescriptionId(), id));
		itemDataTag.putInt(DATA_PRIM_COLOR, breed.getPrimaryColor());
		itemDataTag.putInt(DATA_SEC_COLOR, breed.getSecondaryColor());
		root.put(DATA_TAG, itemDataTag);
		
		ItemStack stack = new ItemStack(DMRItems.DRAGON_SPAWN_EGG.get());
		stack.setTag(root);
		return stack;
	}
	
	@Override
	public Component getName(ItemStack stack)
	{
		var tag = stack.getTagElement(DATA_TAG);
		if (tag != null && tag.contains(DATA_ITEM_NAME)) {
			var entityTag = stack.getTagElement(EntityType.ENTITY_TAG);
			var breed = DragonBreedsRegistry.getDragonBreed(entityTag.getString(NBTConstants.BREED));
			
			if(breed instanceof DragonHybridBreed hybridBreed){
				return Component.translatable(String.join(".", DMRItems.DRAGON_SPAWN_EGG.get().getDescriptionId(), "hybrid"), hybridBreed.parent1.getName().getString(), hybridBreed.parent2.getName().getString());
			}
			
			return Component.translatable(tag.getString(DATA_ITEM_NAME));
		}
		return super.getName(stack);
	}
	
	
	public static int getColor(ItemStack stack, int tintIndex)
	{
		var tag = stack.getTagElement(DATA_TAG);
		if (tag != null)
			return tintIndex == 0? tag.getInt(DATA_PRIM_COLOR) : tag.getInt(DATA_SEC_COLOR);
		return 0xffffff;
	}
}
