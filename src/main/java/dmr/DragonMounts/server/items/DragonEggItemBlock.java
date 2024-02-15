package dmr.DragonMounts.server.items;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.registry.DMRBlocks;
import dmr.DragonMounts.registry.DMRItems;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonEggItemBlock extends BlockItem
{
	public DragonEggItemBlock(Properties pProperties)
	{
		super(DMRBlocks.DRAGON_EGG_BLOCK.get(), pProperties.rarity(Rarity.EPIC));
	}
	
	public static ItemStack getDragonEggStack(IDragonBreed type){
		return getDragonEggStack(type, 1);
	}
	
	public static ItemStack getDragonEggStack(IDragonBreed type, int count){
		ItemStack stack = new ItemStack(DMRItems.DRAGON_EGG_BLOCK_ITEM.get(), count);
		DragonBreed.setDragonType(stack, type);
		return stack;
	}
	
	@Override
	public String getDescriptionId(ItemStack pStack)
	{
		var tag = pStack.getTag();
		
		if(tag != null && tag.contains(NBTConstants.BREED)){
			return String.join(".", DMRBlocks.DRAGON_EGG_BLOCK.get().getDescriptionId(), tag.getString(NBTConstants.BREED));
		}
		
		return super.getDescriptionId(pStack);
	}
	
	@Override
	public Component getName(ItemStack pStack)
	{
		var breed = DragonBreed.getDragonType(pStack);
		if(breed instanceof DragonHybridBreed hybridBreed){
			return Component.translatable(String.join(".", DMRBlocks.DRAGON_EGG_BLOCK.get().getDescriptionId(), "hybrid"), hybridBreed.parent1.getName().getString(), hybridBreed.parent2.getName().getString());
		}
		return super.getName(pStack);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag pFlag)
	{
		super.appendHoverText(stack, level, tooltips, pFlag);
		
		var tag = stack.getTag();
		
		if(tag != null && tag.contains("breed") && tag.contains("hatchTime")){
			var breed = DragonBreedsRegistry.getDragonBreed(tag.getString("breed"));
			if(breed != null){
				var hatchTime = tag.getInt("hatchTime");
				
				if(hatchTime != breed.getHatchTime()) {
					var minutes = hatchTime / 60;
					var seconds = hatchTime % 60;
					var time = String.format("%d:%02d", minutes, seconds);
					tooltips.add(Component.translatable(getDescriptionId() + ".hatch_time", time).withStyle(ChatFormatting.GRAY));
				}
			}
		}
	}
}
