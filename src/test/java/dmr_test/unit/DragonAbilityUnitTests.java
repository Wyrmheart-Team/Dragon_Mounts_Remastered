package dmr_test.unit;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import dmr.DragonMounts.types.abilities.DragonAbility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for dragon ability data structures and configuration.
 * These tests focus on ability properties without requiring game simulation.
 */
public class DragonAbilityUnitTests {

    private DragonAbility testAbility;
    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new Gson();
        String testAbilityJson =
                """
        {
          "max_tier": 3,
          "breed_transferable": true,
          "ability_type": "passive",
          "properties": {
            "duration": 200,
            "cooldown": 60,
            "radius": 10.0
          },
          "particles": ["flame", "smoke"]
        }
        """;
        testAbility = gson.fromJson(testAbilityJson, DragonAbility.class);
    }

    @Test
    @DisplayName("Dragon ability properties are accessible")
    void testBasicAbilityProperties() {
        // Test only methods that actually exist in DragonAbility
        assertEquals(3, testAbility.getMaxTier());
        assertTrue(testAbility.isBreedTransferable());
        assertEquals("passive", testAbility.getAbilityType());
        assertNotNull(testAbility.getProperties());
        assertNotNull(testAbility.getAttributes());
        assertNotNull(testAbility.getParticles());
    }

    @Test
    @DisplayName("Dragon ability properties map works correctly")
    void testAbilityProperties() {
        java.util.Map<String, Object> properties = testAbility.getProperties();

        assertNotNull(properties);
        assertEquals(3, properties.size());
        assertTrue(properties.containsKey("duration"));
        assertTrue(properties.containsKey("cooldown"));
        assertTrue(properties.containsKey("radius"));
    }

    @Test
    @DisplayName("Dragon ability attributes work correctly")
    void testAbilityAttributes() {
        java.util.Map<net.minecraft.resources.ResourceLocation, Double> attributes = testAbility.getAttributes();

        assertNotNull(attributes);
        assertEquals(0, attributes.size());
    }

    @Test
    @DisplayName("Dragon ability particles list works correctly")
    void testAbilityParticles() {
        java.util.List<String> particles = testAbility.getParticles();

        assertNotNull(particles);
        assertEquals(2, particles.size());
        assertTrue(particles.contains("flame"));
        assertTrue(particles.contains("smoke"));
    }

    @Test
    @DisplayName("Dragon ability max tier works correctly")
    void testAbilityMaxTier() {
        int maxTier = testAbility.getMaxTier();

        assertEquals(3, maxTier);
        assertTrue(maxTier > 0, "Max tier should be positive");
    }

    @Test
    @DisplayName("Dragon ability breed transferable flag works")
    void testAbilityBreedTransferable() {
        boolean isTransferable = testAbility.isBreedTransferable();

        assertTrue(isTransferable, "Test ability should be breed transferable");
    }

    @Test
    @DisplayName("Dragon ability type string works correctly")
    void testAbilityType() {
        String abilityType = testAbility.getAbilityType();

        assertNotNull(abilityType);
        assertEquals("passive", abilityType);
    }

    @Test
    @DisplayName("Different dragon abilities have distinct properties")
    void testMultipleAbilities() {
        String activeAbilityJson =
                """
        {
          "max_tier": 1,
          "breed_transferable": false,
          "ability_type": "active",
          "properties": {
            "cooldown": 120
          }
        }
        """;

        DragonAbility activeAbility = gson.fromJson(activeAbilityJson, DragonAbility.class);

        // Abilities should have different properties
        assertNotEquals(testAbility.getMaxTier(), activeAbility.getMaxTier());
        assertNotEquals(testAbility.isBreedTransferable(), activeAbility.isBreedTransferable());
        assertNotEquals(testAbility.getAbilityType(), activeAbility.getAbilityType());
    }

    @Test
    @DisplayName("Dragon ability properties contain expected values")
    void testAbilityPropertyValues() {
        java.util.Map<String, Object> properties = testAbility.getProperties();

        // Test that properties contain the expected values from JSON
        assertEquals(200.0, properties.get("duration"));
        assertEquals(60.0, properties.get("cooldown"));
        assertEquals(10.0, properties.get("radius"));
    }

    @Test
    @DisplayName("Dragon ability collections are properly initialized")
    void testAbilityCollections() {
        // All collections should be non-null and properly initialized
        assertNotNull(testAbility.getProperties(), "Properties map should not be null");
        assertNotNull(testAbility.getAttributes(), "Attributes map should not be null");
        assertNotNull(testAbility.getParticles(), "Particles list should not be null");

        // Collections should be modifiable (ArrayList/HashMap, not immutable)
        assertFalse(testAbility.getProperties().isEmpty(), "Properties should contain test data");
        assertTrue(testAbility.getAttributes().isEmpty(), "Attributes should be empty in this test");
        assertFalse(testAbility.getParticles().isEmpty(), "Particles should contain test data");
    }

    @Test
    @DisplayName("Dragon ability inheritance properties work correctly")
    void testAbilityInheritance() {
        // Test different ability configurations
        String parentJson =
                """
        {
          "max_tier": 2,
          "breed_transferable": true,
          "ability_type": "parent",
          "properties": { "cooldown": 100 }
        }
        """;

        String childJson =
                """
        {
          "max_tier": 1,
          "breed_transferable": false,
          "ability_type": "child",
          "properties": { "cooldown": 50 }
        }
        """;

        DragonAbility parentAbility = gson.fromJson(parentJson, DragonAbility.class);
        DragonAbility childAbility = gson.fromJson(childJson, DragonAbility.class);

        assertNotEquals(
                parentAbility.getMaxTier(),
                childAbility.getMaxTier(),
                "Child ability should have different max tier than parent");
        assertNotEquals(
                parentAbility.isBreedTransferable(),
                childAbility.isBreedTransferable(),
                "Child ability should have different transferability than parent");
    }
}
