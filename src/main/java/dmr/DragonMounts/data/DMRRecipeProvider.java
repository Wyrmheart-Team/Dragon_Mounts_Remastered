package dmr.DragonMounts.data;

import com.google.common.collect.ImmutableMap;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DMRRecipeProvider extends RecipeProvider
{
	public DMRRecipeProvider(PackOutput p_250820_)
	{
		super(p_250820_);
	}
	
	@Override
	protected void buildRecipes(RecipeOutput pRecipeOutput)
	{
		for(DyeColor color : DyeColor.values()){
			var pId = DragonMountsRemaster.id("dragon_whistle_item_" + color.getName());
			Map<Character, Ingredient> keys = ImmutableMap.of('I', Ingredient.of(Items.IRON_INGOT), '#', Ingredient.of(ItemTags.PLANKS), 'D', Ingredient.of(DyeItem.byColor(color)));
			List<String> rows = List.of("I#I", "#D#", "I#I");
			
			ShapedRecipe shapedrecipe = new ShapedRecipe(
					"dragon",
					RecipeBuilder.determineBookCategory(RecipeCategory.MISC),
					ShapedRecipePattern.of(keys, rows),
					DragonWhistleItem.getWhistleItem(color),
					false
			);
			
			pRecipeOutput.accept(pId, shapedrecipe, null);
		}
	}
}
