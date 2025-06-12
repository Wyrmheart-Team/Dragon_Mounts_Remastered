package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.items.*;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonVariant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DMR.MOD_ID);

    public static Supplier<CreativeModeTab> MOD_TESTING_DEV_TAB;

    public static final Supplier<CreativeModeTab> MOD_TAB =
            CREATIVE_MODE_TABS.register("dragon_mounts", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get()))
                    .title(Component.translatable("itemGroup.dragon_mounts"))
                    .displayItems((enabledFeatures, entries) -> {
                        entries.accept(ModItems.BLANK_EGG_BLOCK_ITEM.get().getDefaultInstance());

                        var breeds = DragonBreedsRegistry.getDragonBreeds();
                        for (DragonBreed type : breeds) {
                            entries.accept(DragonEggItemBlock.getDragonEggStack(type));

                            for (DragonVariant variant : type.getVariants()) {
                                entries.accept(DragonEggItemBlock.getDragonEggStack(type, variant));
                            }
                        }
                        for (DragonBreed type : breeds) {
                            entries.accept(DragonSpawnEgg.create(type));

                            for (DragonVariant variant : type.getVariants()) {
                                entries.accept(DragonSpawnEgg.create(type, variant));
                            }
                        }
                        var armors = new ArrayList<>(DragonArmorRegistry.getDragonArmors());
                        armors.sort(Comparator.comparing(DragonArmor::getProtection));

                        for (DragonArmor armor : armors) {
                            entries.accept(DragonArmorItem.getArmorStack(armor));
                        }

                        for (DyeColor color : DyeColor.values()) {
                            entries.accept(DragonWhistleItem.getWhistleItem(color));
                        }
                    })
                    .build());

    public static void init() {
        if (DMR.DEBUG) {

            MOD_TESTING_DEV_TAB =
                    CREATIVE_MODE_TABS.register("dragon_mounts_testing_dev", () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get()))
                            .title(Component.literal("DMR Testing items"))
                            .displayItems((enabledFeatures, entries) -> ModItems.ITEMS
                                    .getEntries()
                                    .forEach(supplier -> {
                                        var item = supplier.get();

                                        if (item instanceof DMRDevItem) {
                                            entries.accept(item.getDefaultInstance());
                                        }
                                    }))
                            .build());
        }
    }
}
