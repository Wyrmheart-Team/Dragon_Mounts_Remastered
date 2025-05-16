package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.recipes.NetheriteArmorRecipe;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCustomRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPES =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, DMR.MOD_ID);

    public static final Supplier<RecipeSerializer<NetheriteArmorRecipe>> NETHERITE_ARMOR_RECIPE = RECIPES.register(
            "netherite_armor_recipe", () -> new SimpleCraftingRecipeSerializer<>(NetheriteArmorRecipe::new));
}
