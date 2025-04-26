package compatibility.jei;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.types.armor.DragonArmor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class DMRJeiPlugin implements IModPlugin {
	
	@Override
	public ResourceLocation getPluginUid() {
		return DMR.id("jei_plugin");
	}
	
	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(ModItems.DRAGON_SPAWN_EGG.get(), new DragonSpawnEggInterpreter());
		registration.registerSubtypeInterpreter(ModItems.DRAGON_EGG_BLOCK_ITEM.get(), new DragonEggInterpreter());
		registration.registerSubtypeInterpreter(ModItems.DRAGON_ARMOR.get(), new DragonArmorInterpreter());
	}
	
	
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
		addNetheriteArmorRecipe(recipes);
		addBlankDragonEggRecipe(recipes);
		registration.addRecipes(RecipeTypes.CRAFTING, recipes);
	}
	
	public static void addNetheriteArmorRecipe(List<RecipeHolder<CraftingRecipe>> recipes) {
		var result = new ItemStack(ModItems.DRAGON_ARMOR.get());
		var diamondArmor = new ItemStack(ModItems.DRAGON_ARMOR.get());
		DragonArmor.setArmorType(diamondArmor, DragonArmorRegistry.getDragonArmor("diamond"));
		DragonArmor.setArmorType(result, DragonArmorRegistry.getDragonArmor("netherite"));
		
		var netheriteArmorRecipe = new ShapelessRecipe("dragon", CraftingBookCategory.MISC, result, NonNullList.of(Ingredient.EMPTY, Ingredient.of(diamondArmor), Ingredient.of(Items.NETHERITE_INGOT)));
		var netheriteArmorRecipeHolder = new RecipeHolder<CraftingRecipe>(DMR.id("netherite_armor_recipe"), netheriteArmorRecipe);
		recipes.add(netheriteArmorRecipeHolder);
	}
	
	public static void addBlankDragonEggRecipe(List<RecipeHolder<CraftingRecipe>> recipes) {
		var result = new ItemStack(ModItems.BLANK_EGG_BLOCK_ITEM.get());
		var eggRecipe = new ShapelessRecipe("dragon", CraftingBookCategory.MISC, result, NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.DRAGON_EGG_BLOCK_ITEM.get())));
		var eggRecipeHolder = new RecipeHolder<CraftingRecipe>(DMR.id("blank_egg_recipe"), eggRecipe);
		recipes.add(eggRecipeHolder);
	}
	
	public static void addDragonEggRecipe(List<RecipeHolder<CraftingRecipe>> recipes) {
		var result = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get());
		var eggRecipe = new ShapelessRecipe("dragon", CraftingBookCategory.MISC, result, NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.DRAGON_EGG_BLOCK_ITEM.get()), Ingredient.of(Items.EGG)));
	}
}

class DragonSpawnEggInterpreter implements ISubtypeInterpreter<ItemStack> {

	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return (
			ingredient.getOrDefault(ModComponents.DRAGON_BREED, "NONE") +
			":" +
			ingredient.getOrDefault(ModComponents.DRAGON_VARIANT, "NONE")
		);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
		return "";
	}
}

class DragonEggInterpreter implements ISubtypeInterpreter<ItemStack> {

	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return (
			ingredient.getOrDefault(ModComponents.DRAGON_BREED, "NONE") +
			":" +
			ingredient.getOrDefault(ModComponents.DRAGON_VARIANT, "NONE")
		);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
		return "";
	}
}

class DragonArmorInterpreter implements ISubtypeInterpreter<ItemStack> {

	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return ingredient.getOrDefault(ModComponents.ARMOR_TYPE, "NONE");
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
		return "";
	}
}
