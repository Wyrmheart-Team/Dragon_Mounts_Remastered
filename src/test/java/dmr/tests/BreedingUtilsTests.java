package dmr.tests;

import dmr.DMRTestConstants;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.habitats.Habitat;
import dmr.DragonMounts.util.BreedingUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

/**
 * Tests for the breeding utility functions of the mod. These tests verify that
 * dragon breeding mechanics work correctly.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Breeding")
public class BreedingUtilsTests {

    /**
     * Tests the habitat-based breed outcome functionality. This test verifies that:
     * 1. The getHabitatBreedOutcomes method returns a sorted list of breeds based
     * on habitat points 2. The getHabitatBreedOutcome method returns the breed with
     * the highest habitat points
     *
     * @param helper
     *               The test helper provided by the game test framework
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testHabitatBreedOutcomes(ExtendedGameTestHelper helper) {
        // Create a player to ensure the level is loaded
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var level = player.level.getServer().overworld();
        var pos = new BlockPos(0, 64, 0);

        // Create test breeds with different habitats
        DragonBreed testBreed1 = new DragonBreed();
        testBreed1.setId("test_breed1");

        // Create a habitat that always returns 10 points
        Habitat habitat1 = new Habitat() {
            @Override
            public int getHabitatPoints(Level level, BlockPos pos) {
                return 10;
            }

            @Override
            public String type() {
                return "test_habitat1";
            }
        };

        List<Habitat> habitats1 = new ArrayList<>();
        habitats1.add(habitat1);

        // Use reflection to set the habitats field
        try {
            Field habitatsField = DragonBreed.class.getDeclaredField("habitats");
            habitatsField.setAccessible(true);
            habitatsField.set(testBreed1, habitats1);
        } catch (Exception e) {
            helper.fail("Failed to set habitats for test_breed1: " + e.getMessage());
        }

        DragonBreed testBreed2 = new DragonBreed();
        testBreed2.setId("test_breed2");

        // Create a habitat that always returns 20 points
        Habitat habitat2 = new Habitat() {
            @Override
            public int getHabitatPoints(Level level, BlockPos pos) {
                return 20;
            }

            @Override
            public String type() {
                return "test_habitat2";
            }
        };

        List<Habitat> habitats2 = new ArrayList<>();
        habitats2.add(habitat2);

        // Use reflection to set the habitats field
        try {
            Field habitatsField = DragonBreed.class.getDeclaredField("habitats");
            habitatsField.setAccessible(true);
            habitatsField.set(testBreed2, habitats2);
        } catch (Exception e) {
            helper.fail("Failed to set habitats for test_breed2: " + e.getMessage());
        }

        // Register the test breeds
        DragonBreedsRegistry.register(testBreed1);
        DragonBreedsRegistry.register(testBreed2);

        // Test getHabitatBreedOutcomes
        List<Entry<Integer, IDragonBreed>> outcomes = BreedingUtils.getHabitatBreedOutcomes(level, pos);

        // Verify that the outcomes are sorted by habitat points (highest first)
        if (outcomes.isEmpty()) {
            helper.fail("No habitat breed outcomes found");
        }

        // The first outcome should have the highest points (testBreed2 with 20 points)
        Entry<Integer, IDragonBreed> firstOutcome = outcomes.get(0);
        if (firstOutcome.getKey() != 20 || !firstOutcome.getValue().getId().equals("test_breed2")) {
            helper.fail("First outcome should be test_breed2 with 20 points");
        }

        // Test getHabitatBreedOutcome
        IDragonBreed highestBreed = BreedingUtils.getHabitatBreedOutcome(level, pos);
        if (highestBreed == null) {
            helper.fail("No highest habitat breed found");
        }

        if (!highestBreed.getId().equals("test_breed2")) {
            helper.fail("Highest habitat breed should be test_breed2");
        }

        helper.succeed();
    }

    /**
     * Tests the custom name generation for baby dragons. This test verifies that:
     * 1. The generateCustomName method generates a name based on the parents' names
     * 2. The generated name is not empty 3. The generated name follows the expected
     * format based on the parents' names
     *
     * @param helper
     *               The test helper provided by the game test framework
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testGenerateCustomName(ExtendedGameTestHelper helper) {
        // Create a player to ensure the level is loaded
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

        // Create two dragons with custom names
        var dragon1 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon1.setBreed(DragonBreedsRegistry.getDefault());
        dragon1.setCustomName(Component.literal("Tempor Invidunt"));

        var dragon2 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon2.setBreed(DragonBreedsRegistry.getDefault());
        dragon2.setCustomName(Component.literal("Magna Dolore"));

        // Generate a custom name for the baby
        String babyName = BreedingUtils.generateCustomName(dragon1, dragon2);

        // Verify that the name is not empty
        if (babyName == null || babyName.isEmpty()) {
            helper.fail("Generated name is empty");
        }

        // Verify that the name contains parts of the parents' names
        // Since the name generation has random elements, we can only check that
        // the name contains at least one word from either parent
        boolean containsParentWord = false;
        String[] parentWords = {"Tempor", "Invidunt", "Magna", "Dolore"};
        for (String word : parentWords) {
            if (babyName.contains(word)) {
                containsParentWord = true;
                break;
            }
        }

        if (!containsParentWord) {
            helper.fail("Generated name does not contain any parent word");
        }

        helper.succeed();
    }
}
