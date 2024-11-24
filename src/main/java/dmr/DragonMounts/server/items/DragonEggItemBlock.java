package dmr.DragonMounts.server.items;

import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModBlocks;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DragonEggItemBlock extends BlockItem {

	public DragonEggItemBlock(Properties pProperties) {
		super(ModBlocks.DRAGON_EGG_BLOCK.get(), pProperties.rarity(Rarity.EPIC));
	}

	public static ItemStack getDragonEggStack(IDragonBreed type) {
		return getDragonEggStack(type, 1);
	}

	public static ItemStack getDragonEggStack(IDragonBreed type, int count) {
		ItemStack stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get(), count);
		DragonBreed.setDragonType(stack, type);
		return stack;
	}

	@Override
	public String getDescriptionId(ItemStack pStack) {
		var breed = pStack.get(ModComponents.DRAGON_BREED);

		if (breed == null) return "item.dmr.dragon_egg.deprecated";

		return String.join(".", ModBlocks.DRAGON_EGG_BLOCK.get().getDescriptionId(), breed);
	}

	@Override
	public Component getName(ItemStack pStack) {
		var breed = DragonBreed.getDragonType(pStack);
		if (breed instanceof DragonHybridBreed hybridBreed) {
			var par1 = hybridBreed.parent1.getName().getString();
			var par2 = hybridBreed.parent2.getName().getString();
			var langKey = String.join(".", ModBlocks.DRAGON_EGG_BLOCK.get().getDescriptionId(), "hybrid");
			return Component.translatable(langKey, par1, par2);
		}
		return super.getName(pStack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag pFlag) {
		super.appendHoverText(stack, context, tooltips, pFlag);

		var breedId = stack.get(ModComponents.DRAGON_BREED);

		if (breedId == null) {
			tooltips.add(Component.translatable("item.dmr.dragon_egg.deprecated.tooltip").withStyle(ChatFormatting.GRAY));
			return;
		}

		int hatchTime = stack.getOrDefault(ModComponents.EGG_HATCH_TIME, 0);
		var breed = DragonBreedsRegistry.getDragonBreed(breedId);

		if (breed != null) {
			if (hatchTime != 0 && hatchTime != breed.getHatchTime()) {
				var minutes = hatchTime / 60;
				var seconds = hatchTime % 60;
				var time = String.format("%d:%02d", minutes, seconds);
				tooltips.add(
					Component.translatable(getDescriptionId() + ".hatch_time_tooltip", time)
						.withStyle(ChatFormatting.RED)
						.withStyle(ChatFormatting.BOLD)
				);
			}
		}

		tooltips.add(Component.translatable("item.dmr.dragon_egg.hatch_tooltip").withStyle(ChatFormatting.GRAY));
	}
}
