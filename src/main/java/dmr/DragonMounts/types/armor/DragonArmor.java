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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
		CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
		
		if(tag.contains(NBTConstants.ARMOR)){
			return DragonArmorRegistry.getDragonArmor(tag.getString(NBTConstants.ARMOR));
		}
		
		return null;
	}
	
	public static void setArmorType(ItemStack stack, DragonArmor type){
		if(type == null) return;
		CompoundTag tag = new CompoundTag();
		tag.putString(NBTConstants.ARMOR, type.getId());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}
}
