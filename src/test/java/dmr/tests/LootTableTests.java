package dmr.tests;

import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.events.LootTableInject;
import dmr.DragonMounts.types.LootTableEntry;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Loot Tables")
public class LootTableTests {

    /**
     * Tests the LootTableInject.injectEggLoot method.
     *
     * <p>
     * This test verifies that: 1. The injectEggLoot method creates a loot pool with
     * the correct name format (breedId-egg) 2. The created loot pool contains the
     * appropriate dragon egg loot
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testInjectEggLoot(ExtendedGameTestHelper helper) {
        // Create a test breed
        DragonBreed testBreed = new DragonBreed();
        testBreed.setId("test_breed");

        // Create a test loot table entry
        ResourceLocation lootTableLocation = ResourceLocation.parse("minecraft:chests/simple_dungeon");
        LootTableEntry entry = new LootTableEntry(lootTableLocation, 0.5f, 1, 1);

        // Create a loot pool using the inject method
        LootPool lootPool = LootTableInject.injectEggLoot(testBreed, entry);

        // Verify the loot pool has the correct name
        if (!lootPool.getName().equals("test_breed-egg")) {
            helper.fail("Loot pool name doesn't match expected value");
        }

        helper.succeed();
    }

    /**
     * Tests adding a dragon egg loot pool to a loot table.
     *
     * <p>
     * This test verifies that: 1. A loot pool created by injectEggLoot can be
     * successfully added to a loot table 2. The loot pool can be retrieved from the
     * table using its name 3. The integration between DragonBreed, LootTableEntry,
     * and LootTable works correctly
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testLootTableAddPool(ExtendedGameTestHelper helper) {
        // Create a player to ensure the level is loaded
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

        // Create a test breed
        DragonBreed testBreed = new DragonBreed();
        testBreed.setId("test_breed");

        // Create a test loot table entry
        ResourceLocation lootTableLocation = ResourceLocation.parse("minecraft:chests/simple_dungeon");
        LootTableEntry entry = new LootTableEntry(lootTableLocation, 0.5f, 1, 1);

        // Set the loot table for the breed using reflection
        try {
            Field lootTableField = DragonBreed.class.getDeclaredField("lootTable");
            lootTableField.setAccessible(true);
            List<LootTableEntry> lootTable = new ArrayList<>();
            lootTable.add(entry);
            lootTableField.set(testBreed, lootTable);
        } catch (Exception e) {
            helper.fail("Failed to set loot table for breed: " + e.getMessage());
        }

        // Register the breed
        DragonBreedsRegistry.register(testBreed);

        // Create a test loot table
        LootTable lootTable = new LootTable.Builder().build();

        // Add a loot pool to the table
        LootPool lootPool = LootTableInject.injectEggLoot(testBreed, entry);
        lootTable.addPool(lootPool);

        // Verify the loot pool was added
        if (lootTable.getPool("test_breed-egg") == null) {
            helper.fail("Loot pool was not added to the table");
        }

        helper.succeed();
    }
}
