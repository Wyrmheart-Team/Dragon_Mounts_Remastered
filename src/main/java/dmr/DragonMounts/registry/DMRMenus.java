package dmr.DragonMounts.registry;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.container.DragonContainerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DMRMenus {
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, DragonMountsRemaster.MOD_ID);
	public static final Supplier<MenuType<DragonContainerMenu>> DRAGON_MENU = MENU_TYPES.register("dragon_menu", () -> IMenuTypeExtension.create(DragonContainerMenu::new));
}
