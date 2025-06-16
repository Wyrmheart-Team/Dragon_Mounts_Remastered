package dmr.DragonMounts.server.recipes;

import dmr.DragonMounts.registry.datapack.DragonArmorRegistry;
import dmr.DragonMounts.registry.item.ModCustomRecipes;
import dmr.DragonMounts.registry.item.ModItems;
import dmr.DragonMounts.types.armor.DragonArmor;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class NetheriteArmorRecipe extends CustomRecipe {

    public NetheriteArmorRecipe(CraftingBookCategory category) {
        super(category);
    }

    public NetheriteArmorRecipe() {
        super(CraftingBookCategory.MISC);
    }

    public boolean matches(CraftingInput input, Level level) {
        var hasDiamondArmor = false;
        var hasNetheriteIngot = false;
        for (int i = 0; i < input.height(); i++) {
            for (int j = 0; j < input.width(); j++) {
                ItemStack itemstack = input.getItem(j, i);
                if (itemstack.isEmpty()) continue;

                var isArmor = false;
                var isIngot = false;

                if (itemstack.is(ModItems.DRAGON_ARMOR.get())) {
                    if (DragonArmor.getArmorType(itemstack) == DragonArmorRegistry.getDragonArmor("diamond")) {
                        isArmor = true;
                    }
                }

                if (itemstack.is(Items.NETHERITE_INGOT)) {
                    isIngot = true;
                }

                // More than one type of armor or ingot is not allowed.
                if ((isArmor && hasDiamondArmor) || (isIngot && hasNetheriteIngot)) {
                    return false;
                }

                // Invalid item
                if (!isIngot && !isArmor) return false;

                if (isArmor) {
                    hasDiamondArmor = true;
                }

                if (isIngot) {
                    hasNetheriteIngot = true;
                }
            }
        }

        return hasDiamondArmor && hasNetheriteIngot;
    }

    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var result = new ItemStack(ModItems.DRAGON_ARMOR.get());
        DragonArmor.setArmorType(result, DragonArmorRegistry.getDragonArmor("netherite"));
        return result.copy();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 || height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModCustomRecipes.NETHERITE_ARMOR_RECIPE.get();
    }
}
