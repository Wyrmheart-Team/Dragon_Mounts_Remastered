package dmr.DragonMounts.data;

import com.google.common.collect.ImmutableMap;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.registry.ModBlocks;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DMRRecipeProvider extends RecipeProvider {

    public DMRRecipeProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        for (DyeColor color : DyeColor.values()) {
            var pId = DMR.id("dragon_whistle_item_" + color.getName());
            Map<Character, Ingredient> keys = ImmutableMap.of(
                    'I',
                    Ingredient.of(Items.IRON_INGOT),
                    '#',
                    Ingredient.of(ItemTags.PLANKS),
                    'D',
                    Ingredient.of(DyeItem.byColor(color)));
            List<String> rows = List.of("I#I", "#D#", "I#I");

            ShapedRecipe shapedrecipe = new ShapedRecipe(
                    "dragon",
                    RecipeBuilder.determineBookCategory(RecipeCategory.MISC),
                    ShapedRecipePattern.of(keys, rows),
                    DragonWhistleItem.getWhistleItem(color),
                    false);

            pRecipeOutput.accept(pId, shapedrecipe, null);
        }

        ShapelessRecipe shapelessrecipe = new ShapelessRecipe(
                "dragon",
                RecipeBuilder.determineBookCategory(RecipeCategory.MISC),
                new ItemStack(ModItems.BLANK_EGG_BLOCK_ITEM.get()),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModBlocks.DRAGON_EGG_BLOCK.get())));

        pRecipeOutput.accept(DMR.id("blank_egg"), shapelessrecipe, null);
    }
}
