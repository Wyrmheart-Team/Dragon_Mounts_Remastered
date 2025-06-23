package dmr_test.unit;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for dragon breed data structures and basic functionality.
 * These tests don't require the full game environment and test read-only properties.
 */
public class DragonBreedUnitTests {

    private DragonBreed testBreed;
    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new Gson();
        String testBreedJson =
                """
        {
          "id": "test_breed",
          "immunities": ["fire", "lava"],
          "primary_color": "ff6200",
          "secondary_color": "ffaa00",
          "accessories": ["tail_horns"]
        }
        """;
        testBreed = gson.fromJson(testBreedJson, DragonBreed.class);
    }

    @Test
    @DisplayName("Dragon breed provides default values from config")
    void testDefaultBreedProperties() {
        // Test only methods that actually exist in DragonBreed
        assertTrue(testBreed.getHatchTime() > 0, "Hatch time should be positive");
        assertTrue(testBreed.getGrowthTime() > 0, "Growth time should be positive");
        assertTrue(testBreed.getSizeModifier() > 0, "Size modifier should be positive");
    }

    @Test
    @DisplayName("Dragon breed immunity list is accessible")
    void testBreedImmunities() {
        // Test that immunity list exists and is accessible
        assertNotNull(testBreed.getImmunities(), "Immunity list should not be null");
        assertEquals(2, testBreed.getImmunities().size(), "Test breed should have 2 immunities");
        assertTrue(testBreed.getImmunities().contains("fire"), "Test breed should be immune to fire");
        assertTrue(testBreed.getImmunities().contains("lava"), "Test breed should be immune to lava");
    }

    @Test
    @DisplayName("Dragon breed variants are accessible")
    void testBreedVariants() {
        // Test that variant list exists and is accessible
        assertNotNull(testBreed.getVariants(), "Variant list should not be null");
    }

    @Test
    @DisplayName("Dragon breed abilities are accessible")
    void testBreedAbilities() {
        // Test that ability list exists and is accessible
        assertNotNull(testBreed.getAbilities(), "Ability entries should not be null");
        assertEquals(0, testBreed.getAbilities().size(), "Test breed should have 0 abilities (simplified test)");
    }

    @Test
    @DisplayName("Dragon breed habitats are accessible")
    void testBreedHabitats() {
        // Test that habitat list exists and is accessible
        assertNotNull(testBreed.getHabitats(), "Habitat list should not be null");
    }

    @Test
    @DisplayName("Dragon breed timing properties are accessible")
    void testBreedingProperties() {
        // Test only properties that actually exist
        int growthTime = testBreed.getGrowthTime();
        int hatchTime = testBreed.getHatchTime();

        assertTrue(growthTime > 0, "Growth time should be positive");
        assertTrue(hatchTime > 0, "Hatch time should be positive");
    }

    @Test
    @DisplayName("Dragon breed component access works correctly")
    void testBreedComponentAccess() {
        // Test only methods that actually exist
        assertNotNull(testBreed.getVariants(), "Variants should not be null");
        assertNotNull(testBreed.getAbilities(), "Abilities should not be null");
        assertNotNull(testBreed.getHabitats(), "Habitats should not be null");
        assertNotNull(testBreed.getImmunities(), "Immunities should not be null");
        assertNotNull(testBreed.getLootTable(), "Loot tables should not be null");
        assertNotNull(testBreed.getName(), "Name component should not be null");
        assertNotNull(testBreed.getResourceLocation(), "Resource location should not be null");
        assertNotNull(testBreed.getAccessories(), "Accessories should not be null");
    }

    @Test
    @DisplayName("Dragon breed color and appearance properties work")
    void testBreedAppearanceProperties() {
        // Test appearance-related properties - colors return int values
        int primaryColor = testBreed.getPrimaryColor();
        int secondaryColor = testBreed.getSecondaryColor();
        assertNotNull(testBreed.getAccessories(), "Accessories should not be null");

        // Model and animation locations should be valid
        assertNotNull(testBreed.getDragonModelLocation(), "Model location should not be null");
        assertNotNull(testBreed.getDragonAnimationLocation(), "Animation location should not be null");

        // Test our JSON colors are parsed correctly
        assertEquals(0xff6200, primaryColor, "Primary color should match JSON value");
        assertEquals(0xffaa00, secondaryColor, "Secondary color should match JSON value");
    }

    @Test
    @DisplayName("Dragon breed breath type is accessible")
    void testBreedBreathType() {
        // Test breath type access
        var breathType = testBreed.getBreathType();
        // Breath type can be null if not specified in JSON
    }
}
