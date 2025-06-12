package dmr.DragonMounts.server.events;

import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.types.LootTableEntry;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public class LootTableInject {

    // Hacky method to add egg loot tables without having to run /reload
    // This is due to NeoForge running the LootTableLoadEvent event before the data
    // pack is loaded
    public static void firstLoadInjectBreeds(LevelAccessor level) {
        var server = level.getServer();
        if (server != null) {
            for (DragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
                for (LootTableEntry entry : breed.getLootTable()) {
                    var newTableKey = ResourceKey.create(Registries.LOOT_TABLE, entry.table());
                    var table = server.reloadableRegistries().getLootTable(newTableKey);
                    if (table == LootTable.EMPTY) continue;

                    LootPool lootPool = injectEggLoot(breed, entry, null);

                    breed.getVariants().forEach(variant -> {
                        LootPool variantLootPool = injectEggLoot(breed, entry, variant);

                        if (table.getPool(variantLootPool.getName()) != null) {
                            table.removePool(variantLootPool.getName());
                        }

                        table.addPool(variantLootPool);
                    });

                    if (table.getPool(lootPool.getName()) != null) {
                        table.removePool(lootPool.getName());
                    }

                    table.addPool(lootPool);
                }
            }
        }
    }

    // Hacky method to add armor loot tables without having to run /reload
    // This is due to NeoForge running the LootTableLoadEvent event before the data
    // pack is loaded
    public static void firstLoadInjectArmor(LevelAccessor level) {
        var server = level.getServer();

        if (server != null) {
            for (DragonArmor armor : DragonArmorRegistry.getDragonArmors()) {
                for (LootTableEntry entry : armor.getLootTable()) {
                    var newTableKey = ResourceKey.create(Registries.LOOT_TABLE, entry.table());
                    var table = server.reloadableRegistries().getLootTable(newTableKey);
                    if (table == LootTable.EMPTY) continue;

                    LootPool lootPool = injectArmorLoot(armor, entry);

                    if (table.getPool(lootPool.getName()) != null) {
                        table.removePool(lootPool.getName());
                    }

                    table.addPool(lootPool);
                }
            }
        }
    }

    public static LootPool injectEggLoot(DragonBreed breed, LootTableEntry entry, DragonVariant variant) {
        var lootItemBuilder = LootItem.lootTableItem(ModItems.DRAGON_EGG_BLOCK_ITEM.get())
                .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_BREED.get(), breed.getId()));
        if (variant != null) {
            lootItemBuilder.apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_VARIANT.get(), variant.id()));
        }
        var chanceMultiplier = 1d;

        if (ServerConfig.MOD_CONFIG_SPEC.isLoaded()) {
            chanceMultiplier = ServerConfig.DRAGON_EGG_SPAWN_CHANCE;
        }

        var lootPoolBuilder = LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance((float) (entry.chance() * chanceMultiplier)))
                .add(lootItemBuilder)
                .name(breed.getId() + "-egg");

        return lootPoolBuilder.build();
    }

    public static LootPool injectArmorLoot(DragonArmor armor, LootTableEntry entry) {
        var lootItemBuilder = LootItem.lootTableItem(ModItems.DRAGON_ARMOR.get())
                .apply(SetComponentsFunction.setComponent(ModComponents.ARMOR_TYPE.get(), armor.getId()));
        var lootPoolBuilder = LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance(entry.chance()))
                .add(lootItemBuilder)
                .name(armor.getId() + "-armor");
        return lootPoolBuilder.build();
    }

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent evt) {
        for (DragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
            for (LootTableEntry entry : breed.getLootTable()) {
                if (evt != null) {
                    if (evt.getName().equals(entry.table())) {
                        var pool = injectEggLoot(breed, entry, null);

                        if (evt.getTable().getPool(pool.getName()) != null) {
                            evt.getTable().removePool(pool.getName());
                        }

                        evt.getTable().addPool(pool);

                        for (DragonVariant variant : breed.getVariants()) {
                            var vPool = injectEggLoot(breed, entry, variant);

                            if (evt.getTable().getPool(vPool.getName()) != null) {
                                evt.getTable().removePool(vPool.getName());
                            }

                            evt.getTable().addPool(vPool);
                        }
                    }
                }
            }
        }

        for (DragonArmor armor : DragonArmorRegistry.getDragonArmors()) {
            for (LootTableEntry entry : armor.getLootTable()) {
                if (evt != null) {
                    if (evt.getName().equals(entry.table())) {
                        var armorPool = injectArmorLoot(armor, entry);

                        if (evt.getTable().getPool(armorPool.getName()) != null) {
                            evt.getTable().removePool(armorPool.getName());
                        }

                        evt.getTable().addPool(armorPool);
                    }
                }
            }
        }
    }
}
