package dmr.DragonMounts.server.items;

import dmr.DragonMounts.ModConstants;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.registry.block.ModBlocks;
import dmr.DragonMounts.registry.datapack.DragonBreedsRegistry;
import dmr.DragonMounts.registry.item.ModItems;
import dmr.DragonMounts.types.DragonTier;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonVariant;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class DragonEggItemBlock extends BlockItem {

    public DragonEggItemBlock(Properties pProperties) {
        super(ModBlocks.DRAGON_EGG_BLOCK.get(), pProperties.rarity(Rarity.EPIC));
    }

    public static ItemStack getDragonEggStack(DragonBreed type) {
        return getDragonEggStack(type, 1, null);
    }

    public static ItemStack getDragonEggStack(DragonBreed type, DragonVariant variant) {
        return getDragonEggStack(type, 1, variant);
    }

    public static ItemStack getDragonEggStack(DragonBreed type, int count, DragonVariant variant) {
        ItemStack stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get(), count);
        if (variant != null) {
            DragonBreedsRegistry.setDragonTypeVariant(stack, type, variant);
        } else {
            DragonBreedsRegistry.setDragonType(stack, type);
        }
        return stack;
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        var breed = pStack.get(ModComponents.DRAGON_BREED);
        var variant = pStack.get(ModComponents.DRAGON_VARIANT);

        if (breed == null) return "item.dmr.dragon_egg.deprecated";

        return String.join(
                ".",
                ModBlocks.DRAGON_EGG_BLOCK.get().getDescriptionId(),
                breed + (variant != null ? ModConstants.VARIANT_DIVIDER + variant : ""));
    }

    @Override
    public Component getName(ItemStack stack) {
        // Get the base name component
        MutableComponent nameComponent = Component.translatable(getDescriptionId(stack));

        if (ServerConfig.ENABLE_DRAGON_TIERS) {
            // Get the tier from the stack
            Integer tierLevel = stack.get(ModComponents.DRAGON_TIER);
            if (tierLevel != null) {
                DragonTier tier = DragonTier.fromLevel(tierLevel);

                // Add the tier name with its color
                nameComponent = Component.empty()
                        .append(tier.getDisplayName())
                        .append(" ")
                        .append(nameComponent)
                        .withStyle(tier.getColor());
            }
        }

        return nameComponent;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag pFlag) {
        super.appendHoverText(stack, context, tooltips, pFlag);

        var breedId = stack.get(ModComponents.DRAGON_BREED);

        if (breedId == null) {
            tooltips.add(Component.translatable("item.dmr.dragon_egg.deprecated.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        int hatchTime = stack.getOrDefault(ModComponents.EGG_HATCH_TIME, 0);
        var breed = DragonBreedsRegistry.getDragonBreed(breedId);

        if (breed != null) {
            if (hatchTime != 0 && hatchTime != breed.getHatchTime()) {
                var minutes = hatchTime / 60;
                var seconds = hatchTime % 60;
                var time = String.format("%d:%02d", minutes, seconds);
                tooltips.add(Component.translatable(getDescriptionId() + ".hatch_time_tooltip", time)
                        .withStyle(ChatFormatting.RED)
                        .withStyle(ChatFormatting.BOLD));
            }
        }

        tooltips.add(Component.translatable("item.dmr.dragon_egg.hatch_tooltip").withStyle(ChatFormatting.GRAY));
    }
}
