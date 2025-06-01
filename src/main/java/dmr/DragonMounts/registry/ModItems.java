package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.items.*;
import dmr.DragonMounts.server.items.dev.HabitatOutcomeCheck;
import dmr.DragonMounts.server.items.dev.InstantHatchItem;
import dmr.DragonMounts.server.items.dev.UseBreathAttackItem;
import java.util.HashMap;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, DMR.MOD_ID);

    public static final Supplier<Item> DRAGON_SPAWN_EGG = ITEMS.register("dragon_spawn_egg", DragonSpawnEgg::new);
    public static final Supplier<Item> DRAGON_EGG_BLOCK_ITEM =
            ITEMS.register("dragon_egg", () -> new DragonEggItemBlock(new Item.Properties()));
    public static final Supplier<Item> BLANK_EGG_BLOCK_ITEM =
            ITEMS.register("blank_egg", () -> new BlankDragonEggItemBlock(new Item.Properties()));
    public static final Supplier<Item> DRAGON_ARMOR =
            ITEMS.register("dragon_armor", () -> new DragonArmorItem(new Item.Properties().stacksTo(1)));

    public static HashMap<Integer, Supplier<Item>> DRAGON_WHISTLES = generateWhistles();

    private static HashMap<Integer, Supplier<Item>> generateWhistles() {
        HashMap<Integer, Supplier<Item>> map = new HashMap<>();
        for (DyeColor color : DyeColor.values()) {
            map.put(
                    color.getId(),
                    ITEMS.register(
                            "dragon_whistle." + color.getName(),
                            () -> new DragonWhistleItem(new Item.Properties().stacksTo(1), color)));
        }
        return map;
    }

    public static void init() {
        if (DMR.DEBUG) {
            ITEMS.register("habitat_checker", HabitatOutcomeCheck::new);
            ITEMS.register("instant_hatch", InstantHatchItem::new);
            ITEMS.register("breath_attack", UseBreathAttackItem::new);
        }
    }
}
