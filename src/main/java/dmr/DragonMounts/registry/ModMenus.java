package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.container.DragonContainerMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, DMR.MOD_ID);
	public static final Supplier<MenuType<DragonContainerMenu>> DRAGON_MENU = MENU_TYPES.register("dragon_menu", () ->
		IMenuTypeExtension.create(DragonContainerMenu::new)
	);
}
