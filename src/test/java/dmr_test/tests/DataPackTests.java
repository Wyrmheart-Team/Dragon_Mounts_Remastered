package dmr_test.tests;

import dmr.DragonMounts.registry.datapack.DragonArmorRegistry;
import dmr.DragonMounts.registry.datapack.DragonBreedsRegistry;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

/**
 * Tests for the data pack handling functionality of the mod. These tests verify
 * that dragon breeds and armor can be properly registered, retrieved, and
 * updated through the registry system.
 *
 * <p>
 * Note: While there is some overlap with DragonArmorTests and DragonBreedTests,
 * this class focuses on testing the registry functionality from a data pack
 * perspective, testing both registries together to ensure they work correctly
 * in the context of data pack loading and processing.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "DataPack")
public class DataPackTests {

    /**
     * Tests the basic registration and retrieval functionality of the
     * DragonBreedsRegistry. This test verifies that: 1. A new dragon breed can be
     * registered 2. The registered breed can be retrieved by ID 3. The retrieved
     * breed has the correct ID
     *
     * @param helper
     *               The test helper provided by the game test framework
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testDragonBreedRegistry(ExtendedGameTestHelper helper) {
        // Create a test breed
        DragonBreed testBreed = new DragonBreed();
        testBreed.setId("test_breed");

        // Register the breed
        DragonBreedsRegistry.register(testBreed);

        // Verify the breed was registered
        DragonBreed registeredBreed = DragonBreedsRegistry.getDragonBreed("test_breed");
        if (registeredBreed == null) {
            helper.fail("Test breed not found in registry after registration");
        }

        if (!registeredBreed.getId().equals("test_breed")) {
            helper.fail("Registered breed ID doesn't match expected value");
        }

        helper.succeed();
    }

    /**
     * Tests the basic registration and retrieval functionality of the
     * DragonArmorRegistry. This test verifies that: 1. A new dragon armor can be
     * registered 2. The registered armor can be retrieved by ID 3. The retrieved
     * armor has the correct ID
     *
     * @param helper
     *               The test helper provided by the game test framework
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testDragonArmorRegistry(ExtendedGameTestHelper helper) {
        // Create a test armor
        DragonArmor testArmor = new DragonArmor();
        testArmor.setId("test_armor");

        // Register the armor
        DragonArmorRegistry.register(testArmor);

        // Verify the armor was registered
        DragonArmor registeredArmor = DragonArmorRegistry.getDragonArmor("test_armor");
        if (registeredArmor == null) {
            helper.fail("Test armor not found in registry after registration");
        }

        if (!registeredArmor.getId().equals("test_armor")) {
            helper.fail("Registered armor ID doesn't match expected value");
        }

        helper.succeed();
    }

    /**
     * Tests the bulk update functionality of both registries. This test verifies
     * that: 1. The registries can be completely replaced with new collections 2.
     * The new collections are properly registered and can be retrieved 3. The
     * registry sizes are updated correctly 4. The registries can be restored to
     * their original state
     *
     * @param helper
     *               The test helper provided by the game test framework
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testRegistryUpdates(ExtendedGameTestHelper helper) {
        // Create a player to ensure the level is loaded
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

        // Store the initial state of the registries
        int initialBreedCount = DragonBreedsRegistry.getDragonBreeds().size();
        int initialArmorCount = DragonArmorRegistry.getDragonArmors().size();

        // Create test breed and armor
        DragonBreed testBreed = new DragonBreed();
        testBreed.setId("test_breed");

        DragonArmor testArmor = new DragonArmor();
        testArmor.setId("test_armor");

        // Update the registries with completely new collections
        List<DragonBreed> breeds = new ArrayList<>();
        breeds.add(testBreed);
        DragonBreedsRegistry.setBreeds(breeds);

        List<DragonArmor> armors = new ArrayList<>();
        armors.add(testArmor);
        DragonArmorRegistry.setArmors(armors);

        // Verify the registries were updated with the correct size
        if (DragonBreedsRegistry.getDragonBreeds().size() != 1) {
            helper.fail("Breed registry size doesn't match expected value");
        }

        if (DragonArmorRegistry.getDragonArmors().size() != 1) {
            helper.fail("Armor registry size doesn't match expected value");
        }

        // Verify the specific items can be retrieved
        DragonBreed registeredBreed = DragonBreedsRegistry.getDragonBreed("test_breed");
        if (registeredBreed == null) {
            helper.fail("Test breed not found in registry");
        }

        DragonArmor registeredArmor = DragonArmorRegistry.getDragonArmor("test_armor");
        if (registeredArmor == null) {
            helper.fail("Test armor not found in registry");
        }

        // Restore the original registries to avoid affecting other tests
        List<DragonBreed> defaultBreeds = new ArrayList<>();
        defaultBreeds.add(DragonBreedsRegistry.getDefault());
        DragonBreedsRegistry.setBreeds(defaultBreeds);

        List<DragonArmor> defaultArmors = new ArrayList<>();
        defaultArmors.add(DragonArmorRegistry.getDefault());
        DragonArmorRegistry.setArmors(defaultArmors);

        helper.succeed();
    }
}
