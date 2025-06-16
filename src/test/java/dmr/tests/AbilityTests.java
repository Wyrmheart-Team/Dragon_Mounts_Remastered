package dmr.tests;

import dmr.DragonMounts.registry.datapack.DragonAbilityRegistry;
import dmr.DragonMounts.types.abilities.DragonAbility;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

/**
 * Tests for the dragon ability system. These tests verify that abilities
 * can be properly defined, registered, and retrieved through the registry system.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Ability")
public class AbilityTests {

    /**
     * Tests the basic registration and retrieval functionality of the
     * DragonAbilityRegistry. This test verifies that:
     * 1. A new ability definition can be created
     * 2. The ability definition can be registered
     * 3. The registered ability definition can be retrieved by ID
     *
     * @param helper The test helper provided by the game test framework
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testAbilityRegistration(ExtendedGameTestHelper helper) {
        // Create a test ability definition
        DragonAbility testAbility = new DragonAbility();
        testAbility.setId("test_ability");

        // Register the ability definition
        DragonAbilityRegistry.registerAbilityDefinition(testAbility);

        // Verify the ability definition was registered
        ResourceLocation id = ResourceLocation.parse("dmr:test_ability");
        DragonAbility registeredAbility = DragonAbilityRegistry.getAbilityDefinition(id);

        if (registeredAbility == null) {
            helper.fail("Test ability definition not found in registry after registration");
        }

        if (!registeredAbility.getId().equals("test_ability")) {
            helper.fail("Registered ability ID doesn't match expected value");
        }

        helper.succeed();
    }
}
