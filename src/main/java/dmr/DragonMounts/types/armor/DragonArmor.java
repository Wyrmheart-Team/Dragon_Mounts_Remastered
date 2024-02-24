package dmr.DragonMounts.types.armor;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.registry.DMRBlocks;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed.LootTableEntry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DragonArmor
{
	private @Getter @Setter String id;
	
	@SerializedName("protection")
	@Getter
	private int protection = 0;
	
	public Component getName()
	{
		return Component.translatable(DragonMountsRemaster.MOD_ID + ".dragon_armor." + getId());
	}
	
	@SerializedName("loot_tables")
	@Getter
	private List<LootTableEntry> lootTable = new ArrayList<>();
	
	public static DragonArmor getArmorType(ItemStack stack){
		CompoundTag tag = stack.getTag();
		
		if(tag != null && tag.contains(NBTConstants.ARMOR)){
			return DragonArmorRegistry.getDragonArmor(tag.getString(NBTConstants.ARMOR));
		}
		
		return null;
	}
	
	public static void setArmorType(ItemStack stack, DragonArmor type){
		if(type == null) return;
		
		CompoundTag tag = stack.getTag() == null ? new CompoundTag() : stack.getTag();
		tag.putString(NBTConstants.ARMOR, type.getId());
		
		if(!stack.hasTag()){
			stack.setTag(tag);
		}
	}
}
