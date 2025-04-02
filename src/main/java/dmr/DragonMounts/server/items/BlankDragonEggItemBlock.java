package dmr.DragonMounts.server.items;

import dmr.DragonMounts.registry.ModBlocks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class BlankDragonEggItemBlock extends BlockItem {

	public BlankDragonEggItemBlock(Properties pProperties) {
		super(ModBlocks.BLANK_EGG_BLOCK.get(), pProperties.rarity(Rarity.EPIC));
	}

	@Override
	public String getDescriptionId() {
		return "item.dmr.blank_egg";
	}

	@Override
	public Component getName(ItemStack pStack) {
		return Component.translatable(ModBlocks.BLANK_EGG_BLOCK.get().getDescriptionId());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag pFlag) {
		tooltips.add(Component.translatable("item.dmr.blank_egg.tooltip").withStyle(ChatFormatting.GRAY));
	}
}
