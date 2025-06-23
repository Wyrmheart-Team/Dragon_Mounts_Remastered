package dmr_test.unit;

import static org.junit.jupiter.api.Assertions.*;

import dmr.DragonMounts.types.habitats.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for habitat types and their configuration.
 * These tests focus on data structure validation without requiring game environment.
 */
public class HabitatUnitTests {

    @Test
    @DisplayName("BiomeHabitat configuration works correctly")
    void testBiomeHabitatConfiguration() {
        TagKey<Biome> forestTag = TagKey.create(Registries.BIOME, Biomes.FOREST.location());
        BiomeHabitat habitat = new BiomeHabitat(25, forestTag);

        assertEquals(Habitat.BIOMES, habitat.type());
        assertEquals(25, habitat.points());
        assertEquals(forestTag, habitat.biomeTag());
    }

    @Test
    @DisplayName("HeightHabitat configuration works correctly")
    void testHeightHabitatConfiguration() {
        HeightHabitat habitat = new HeightHabitat(15, false, 100);

        assertEquals(Habitat.WORLD_HEIGHT, habitat.type());
        assertEquals(15, habitat.points());
        assertEquals(100, habitat.height());
        assertFalse(habitat.below());

        // Test below configuration
        HeightHabitat belowHabitat = new HeightHabitat(20, true, 50);
        assertTrue(belowHabitat.below());
        assertEquals(50, belowHabitat.height());
    }

    @Test
    @DisplayName("LightHabitat configuration works correctly")
    void testLightHabitatConfiguration() {
        LightHabitat habitat = new LightHabitat(10, true, 8);

        assertEquals(Habitat.LIGHT, habitat.type());
        assertEquals(10, habitat.points());
        assertEquals(8, habitat.light());
        assertTrue(habitat.below());

        // Test above configuration
        LightHabitat aboveHabitat = new LightHabitat(15, false, 12);
        assertFalse(aboveHabitat.below());
        assertEquals(12, aboveHabitat.light());
    }

    @Test
    @DisplayName("FluidHabitat configuration works correctly")
    void testFluidHabitatConfiguration() {
        TagKey<Fluid> waterTag = TagKey.create(Registries.FLUID, ResourceLocation.parse("minecraft:water"));
        FluidHabitat habitat = new FluidHabitat(1.5f, waterTag);

        assertEquals(Habitat.IN_FLUID, habitat.type());
        assertEquals(1.5f, habitat.multiplier());
        assertEquals(waterTag, habitat.fluidType());
    }

    @Test
    @DisplayName("NearbyBlocksHabitat configuration works correctly")
    void testNearbyBlocksHabitatConfiguration() {
        TagKey<Block> stoneTag = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:stone"));
        NearbyBlocksHabitat habitat = new NearbyBlocksHabitat(2.0f, stoneTag);

        assertEquals(Habitat.NEARBY_BLOCKS, habitat.type());
        assertEquals(2.0f, habitat.multiplier());
        assertEquals(stoneTag, habitat.tag());
    }

    @Test
    @DisplayName("TimeOfDayHabitat configuration works correctly")
    void testTimeOfDayHabitatConfiguration() {
        TimeOfDayHabitat dayHabitat = new TimeOfDayHabitat(5, true);

        assertEquals(Habitat.TIME_OF_DAY, dayHabitat.type());
        assertEquals(5, dayHabitat.points());
        assertTrue(dayHabitat.isDayTime());

        // Test night configuration
        TimeOfDayHabitat nightHabitat = new TimeOfDayHabitat(8, false);
        assertFalse(nightHabitat.isDayTime());
        assertEquals(8, nightHabitat.points());
    }

    @Test
    @DisplayName("DragonBreathHabitat configuration works correctly")
    void testDragonBreathHabitatConfiguration() {
        DragonBreathHabitat habitat = DragonBreathHabitat.INSTANCE;

        assertEquals(Habitat.DRAGON_BREATH, habitat.type());
        assertNotNull(habitat, "DragonBreathHabitat instance should not be null");
    }

    @Test
    @DisplayName("PickyHabitat configuration works correctly")
    void testPickyHabitatConfiguration() {
        TagKey<Biome> forestTag = TagKey.create(Registries.BIOME, Biomes.FOREST.location());
        BiomeHabitat biomeHabitat = new BiomeHabitat(10, forestTag);
        HeightHabitat heightHabitat = new HeightHabitat(5, false, 100);

        java.util.List<Habitat> habitats = java.util.List.of(biomeHabitat, heightHabitat);
        PickyHabitat pickyHabitat = new PickyHabitat(habitats);

        assertEquals(Habitat.PICKY, pickyHabitat.type());
        assertEquals(2, pickyHabitat.habitats().size());
        assertTrue(pickyHabitat.habitats().contains(biomeHabitat));
        assertTrue(pickyHabitat.habitats().contains(heightHabitat));
    }

    @Test
    @DisplayName("Habitat point values work correctly")
    void testHabitatPointValues() {
        TagKey<net.minecraft.world.level.biome.Biome> forestTag =
                TagKey.create(Registries.BIOME, Biomes.FOREST.location());

        // Test normal points
        BiomeHabitat habitat25 = new BiomeHabitat(25, forestTag);
        assertEquals(25, habitat25.points());

        // Test zero points
        BiomeHabitat habitat0 = new BiomeHabitat(0, forestTag);
        assertEquals(0, habitat0.points());

        // Test high points
        BiomeHabitat habitat1000 = new BiomeHabitat(1000, forestTag);
        assertEquals(1000, habitat1000.points());
    }

    @Test
    @DisplayName("Multiple habitats have different types")
    void testHabitatTypes() {
        TagKey<Biome> forestTag = TagKey.create(Registries.BIOME, Biomes.FOREST.location());
        BiomeHabitat biomeHabitat = new BiomeHabitat(20, forestTag);
        HeightHabitat heightHabitat = new HeightHabitat(15, false, 100);
        LightHabitat lightHabitat = new LightHabitat(12, true, 5);
        TimeOfDayHabitat timeHabitat = new TimeOfDayHabitat(8, true);

        // All habitats should have different types
        assertNotEquals(biomeHabitat.type(), heightHabitat.type());
        assertNotEquals(biomeHabitat.type(), lightHabitat.type());
        assertNotEquals(heightHabitat.type(), timeHabitat.type());

        assertEquals(Habitat.BIOMES, biomeHabitat.type());
        assertEquals(Habitat.WORLD_HEIGHT, heightHabitat.type());
        assertEquals(Habitat.LIGHT, lightHabitat.type());
        assertEquals(Habitat.TIME_OF_DAY, timeHabitat.type());

        // Points should be accessible
        assertEquals(20, biomeHabitat.points());
        assertEquals(15, heightHabitat.points());
        assertEquals(12, lightHabitat.points());
        assertEquals(8, timeHabitat.points());
    }
}
