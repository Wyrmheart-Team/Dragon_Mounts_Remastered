package dmr.tests;

import dmr.DragonMounts.registry.DragonArmorRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.Objects;

/**
 * Tests for the DragonArmorRegistry functionality.
 * These tests verify that dragon armor types can be properly registered,
 * retrieved, and managed through the registry system.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragon Armors")
public class DragonArmorTests {

	/**
	 * Tests that the DragonArmorRegistry contains at least one armor type.
	 * This verifies that the armor registry is properly initialized and populated.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasArmors(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Verify that the registry contains at least one armor
		if (DragonArmorRegistry.getDragonArmors().isEmpty()) {
			helper.fail("No dragon armors found");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonArmorRegistry has a default armor type.
	 * The default armor is used as a fallback when a specific armor type cannot be found.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasDefaultArmor(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Verify that the default armor exists
		if (DragonArmorRegistry.getDefault() == null) {
			helper.fail("No default dragon armor found");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonArmorRegistry can retrieve the first armor in the registry.
	 * This is used as a fallback when no default armor is specified.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasFirstArmor(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Verify that the first armor exists
		if (DragonArmorRegistry.getFirst() == null) {
			helper.fail("No first dragon armor found");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonArmorRegistry can retrieve a specific armor by ID
	 * and that the retrieved armor has the correct ID.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void getArmorById(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Retrieve a specific armor by ID
		var armor = DragonArmorRegistry.getDragonArmor("test1");

		// Verify the armor exists
		if (armor == null) {
			helper.fail("No test1 dragon armor found");
		}

		// Verify the armor has the correct ID
		if (!Objects.equals(armor.getId(), "test1")) {
			helper.fail("Dragon armor has incorrect id");
		}

		helper.succeed();
	}

	/**
	 * Tests that the DragonArmorRegistry can check if a specific armor ID exists.
	 *
	 * @param helper The test helper provided by the game test framework
	 */
	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void checkArmorExists(ExtendedGameTestHelper helper) {
		// Create a player to ensure data packs are loaded
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE);

		// Check if a specific armor exists using the hasDragonArmor method
		boolean exists = DragonArmorRegistry.hasDragonArmor("test1");

		// Verify the armor exists
		if (!exists) {
			helper.fail("test1 armor should exist but hasDragonArmor returned false");
		}

		// Check if a non-existent armor is correctly identified as not existing
		boolean nonExistentArmor = DragonArmorRegistry.hasDragonArmor("non_existent_armor");
		if (nonExistentArmor) {
			helper.fail("non_existent_armor should not exist but hasDragonArmor returned true");
		}

		helper.succeed();
	}
}
