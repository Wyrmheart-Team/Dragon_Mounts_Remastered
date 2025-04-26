package dmr.tests;

import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.Objects;

/**
 * Tests for the DragonBreedsRegistry functionality.
 * These tests verify that dragon breeds can be properly registered,
 * retrieved, and managed through the registry system, including
 * hybrid breed generation and retrieval.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragon Breeds")
public class DragonBreedTests {

	/**
	 * Tests that the DragonBreedsRegistry contains at least one breed.
	 * This verifies that the breed registry is properly initialized and populated.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasBreeds(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Verify that the registry contains at least one breed
		if (DragonBreedsRegistry.getDragonBreeds().isEmpty()) {
			helper.fail("No dragon breeds found");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonBreedsRegistry has a default breed.
	 * The default breed is used as a fallback when a specific breed cannot be found.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasDefaultBreed(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Verify that the default breed exists
		if (DragonBreedsRegistry.getDefault() == null) {
			helper.fail("No default dragon breed found");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonBreedsRegistry can retrieve the first breed in the registry.
	 * This is used as a fallback when no default breed is specified.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasFirstBreed(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Verify that the first breed exists
		if (DragonBreedsRegistry.getFirst() == null) {
			helper.fail("No first dragon breed found");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonBreedsRegistry can retrieve a specific breed by ID
	 * and that the retrieved breed has the correct ID.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void getBreedById(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Retrieve a specific breed by ID
		var breed = DragonBreedsRegistry.getDragonBreed("test1");

		// Verify the breed exists
		if (breed == null) {
			helper.fail("No dragon breed found with id 'test1'");
		}

		// Verify the breed has the correct ID
		if (!Objects.equals(breed.getId(), "test1")) {
			helper.fail("Dragon breed has incorrect id");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonBreedsRegistry can check if a specific breed ID exists.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void checkBreedExists(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Check if a specific breed exists using the hasDragonBreed method
		boolean exists = DragonBreedsRegistry.hasDragonBreed("test1");

		// Verify the breed exists
		if (!exists) {
			helper.fail("test1 breed should exist but hasDragonBreed returned false");
		}

		// Check if a non-existent breed is correctly identified as not existing
		boolean nonExistentBreed = DragonBreedsRegistry.hasDragonBreed("non_existent_breed");
		if (nonExistentBreed) {
			helper.fail("non_existent_breed should not exist but hasDragonBreed returned true");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonBreedsRegistry contains at least one hybrid breed.
	 * Hybrid breeds are automatically generated from combinations of regular breeds.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasHybrids(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Verify that at least one hybrid breed exists in the registry
		if (DragonBreedsRegistry.getDragonBreeds().stream().noneMatch(IDragonBreed::isHybrid)) {
			helper.fail("No dragon hybrids found");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonBreedsRegistry can retrieve a specific hybrid breed
	 * by its parent breeds.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void getHybridByParents(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Get the parent breeds
		var breed1 = DragonBreedsRegistry.getDragonBreed("test1");
		var breed2 = DragonBreedsRegistry.getDragonBreed("test2");

		// Get the hybrid breed from the parent breeds
		var hybrid = DragonBreedsRegistry.getHybridBreed(breed1, breed2);

		// Verify the hybrid breed exists and is actually a hybrid
		if (hybrid == null || !hybrid.isHybrid()) {
			helper.fail("No hybrid breed found with parents 'test1' and 'test2'");
		}

		helper.succeed();
	}

	/**
	 * Tests that a hybrid breed has the correct properties:
	 * 1. It is an instance of DragonHybridBreed
	 * 2. It has the correct parent breeds
	 * 3. It has the correct ID format (hybrid_parent1_parent2)
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void verifyHybridProperties(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Get the parent breeds
		var breed1 = DragonBreedsRegistry.getDragonBreed("test1");
		var breed2 = DragonBreedsRegistry.getDragonBreed("test2");

		// Get the hybrid breed from the parent breeds
		var hybrid = DragonBreedsRegistry.getHybridBreed(breed1, breed2);

		// Verify the hybrid breed exists and is actually a hybrid
		if (hybrid == null || !hybrid.isHybrid()) {
			helper.fail("No hybrid breed found with parents 'test1' and 'test2'");
		}

		// Verify the hybrid is an instance of DragonHybridBreed
		if (!(hybrid instanceof DragonHybridBreed)) {
			helper.fail("Hybrid breed is not an instance of DragonHybridBreed");
		}

		// Cast to DragonHybridBreed for further checks
		DragonHybridBreed hybridBreed = (DragonHybridBreed) hybrid;

		// Verify the hybrid has the correct parent breeds
		if (!hybridBreed.parent1.getId().equals("test1") || !hybridBreed.parent2.getId().equals("test2")) {
			helper.fail("Hybrid breed has incorrect parents");
		}

		// Verify the hybrid has the correct ID format
		if (!hybridBreed.getId().equals("hybrid_test1_test2")) {
			helper.fail("Hybrid breed has incorrect id");
		}

		helper.succeed();
	}
}
