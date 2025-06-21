package dmr.DragonMounts.server.events;

import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.datapack.DragonArmorRegistry;
import dmr.DragonMounts.registry.datapack.DragonBreedsRegistry;
import dmr.DragonMounts.registry.item.ModItems;
import dmr.DragonMounts.types.DragonTier;
import dmr.DragonMounts.types.LootTableEntry;
import dmr.DragonMounts.types.LootTableProvider;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import java.util.Collection;
import java.util.function.BiFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

@EventBusSubscriber
public class LootTableInject {
    /**
     * Generic method to inject loot tables for any type of entry that provides loot tables
     * @param server The Minecraft server
     * @param entries Collection of entries to process
     * @param lootPoolCreator Function to create a loot pool from an entry and its loot table entry
     * @param <T> Type of entry that implements LootTableProvider
     */
    private static <T extends LootTableProvider> void injectLootTables(
            MinecraftServer server, Collection<T> entries, BiFunction<T, LootTableEntry, LootPool> lootPoolCreator) {

        for (T entry : entries) {
            for (LootTableEntry lootTableEntry : entry.getLootTable()) {
                var newTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableEntry.table());
                var table = server.reloadableRegistries().getLootTable(newTableKey);
                if (table == LootTable.EMPTY) continue;

                LootPool lootPool = lootPoolCreator.apply(entry, lootTableEntry);

                updateLootTable(table, lootPool);
            }
        }
    }

    /**
     * Updates a loot table with a new loot pool, removing any existing pool with the same name
     * @param table The loot table to update
     * @param lootPool The loot pool to add
     */
    private static void updateLootTable(LootTable table, LootPool lootPool) {
        if (table.getPool(lootPool.getName()) != null) {
            table.removePool(lootPool.getName());
        }
        table.addPool(lootPool);
    }

    // First load methods

    public static void injectLootTables(MinecraftServer server) {
        injectLootTables(server, DragonBreedsRegistry.getDragonBreeds(), LootTableInject::injectEggLoot);
        injectLootTables(server, DragonArmorRegistry.getDragonArmors(), LootTableInject::injectArmorLoot);
    }

    // Loot pool creation methods

    public static LootPool injectEggLoot(DragonBreed breed, LootTableEntry entry) {
        // Get the global chance multiplier from config
        var chanceMultiplier = 1d;
        if (ServerConfig.MOD_CONFIG_SPEC.isLoaded()) {
            chanceMultiplier = ServerConfig.DRAGON_EGG_SPAWN_CHANCE;
        }

        // Create the main loot pool builder
        var lootPoolBuilder = LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance((float) (entry.chance() * chanceMultiplier)))
                .name(breed.getId() + "-egg");

        if (ServerConfig.ENABLE_DRAGON_TIERS) {

            // Add an entry for each tier with appropriate weighting
            for (DragonTier tier : DragonTier.values()) {
                var tierItemBuilder = LootItem.lootTableItem(ModItems.DRAGON_EGG_BLOCK_ITEM.get())
                        .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_BREED.get(), breed.getId()))
                        .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_TIER.get(), tier.getLevel()));

                // Set the weight based on the tier's spawn chance
                // This ensures higher tiers are less common
                var weight = (int) (tier.getSpawnChance() * 100);
                tierItemBuilder.setWeight(weight);

                lootPoolBuilder.add(tierItemBuilder);

                breed.getVariants().forEach(variant -> {
                    var variantItemBuilder = LootItem.lootTableItem(ModItems.DRAGON_EGG_BLOCK_ITEM.get())
                            .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_BREED.get(), breed.getId()))
                            .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_TIER.get(), tier.getLevel()))
                            .apply(SetComponentsFunction.setComponent(
                                    ModComponents.DRAGON_VARIANT.get(), variant.id()));

                    variantItemBuilder.setWeight(weight);
                    lootPoolBuilder.add(variantItemBuilder);
                });
            }
        } else {
            var itemBuilder = LootItem.lootTableItem(ModItems.DRAGON_EGG_BLOCK_ITEM.get())
                    .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_BREED.get(), breed.getId()));
            lootPoolBuilder.add(itemBuilder);

            breed.getVariants().forEach(variant -> {
                var variantItemBuilder = LootItem.lootTableItem(ModItems.DRAGON_EGG_BLOCK_ITEM.get())
                        .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_BREED.get(), breed.getId()))
                        .apply(SetComponentsFunction.setComponent(ModComponents.DRAGON_VARIANT.get(), variant.id()));

                lootPoolBuilder.add(variantItemBuilder);
            });
        }

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
        if (evt == null) return;

        // Process dragon breeds
        processLootTableEvent(evt, DragonBreedsRegistry.getDragonBreeds(), LootTableInject::injectEggLoot);

        // Process dragon armor
        processLootTableEvent(evt, DragonArmorRegistry.getDragonArmors(), LootTableInject::injectArmorLoot);
    }

    /**
     * Generic method to process loot table events for any type of entry that provides loot tables
     * @param evt The loot table load event
     * @param entries Collection of entries to process
     * @param lootPoolCreator Function to create a loot pool from an entry and its loot table entry
     * @param <T> Type of entry that implements LootTableProvider
     */
    private static <T extends LootTableProvider> void processLootTableEvent(
            LootTableLoadEvent evt, Collection<T> entries, BiFunction<T, LootTableEntry, LootPool> lootPoolCreator) {

        for (T entry : entries) {
            for (LootTableEntry lootTableEntry : entry.getLootTable()) {
                if (evt.getName().equals(lootTableEntry.table())) {
                    LootPool lootPool = lootPoolCreator.apply(entry, lootTableEntry);
                    updateLootTable(evt.getTable(), lootPool);
                }
            }
        }
    }
}
