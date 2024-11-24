package compatibility.jei;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
}

class DragonSpawnEggInterpreter implements ISubtypeInterpreter<ItemStack> {

	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return ingredient.getOrDefault(ModComponents.DRAGON_BREED, "NONE");
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
		return "";
	}
}

class DragonEggInterpreter implements ISubtypeInterpreter<ItemStack> {

	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return ingredient.getOrDefault(ModComponents.DRAGON_BREED, "NONE");
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
