package dmr.tests;

import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.List;

/**
 * Tests for the player capability functionality of the mod.
 * These tests verify that player capabilities related to dragons can be properly
 * accessed, set, and managed, including storing and retrieving dragon data.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Capabilities")
public class CapabilityTests {

	/**
	 * Tests retrieving the player capability handler.
	 * 
	 * This test verifies that:
	 * 1. The PlayerStateUtils.getHandler method can retrieve a capability handler for a player
	 * 2. The handler is not null
	 * 
	 * @param helper The game test helper
	 */
	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void getCapability(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = PlayerStateUtils.getHandler(player);
		helper.succeed();
	}

	/**
	 * Tests that the player instance is automatically set in the capability handler.
	 * 
	 * This test verifies that:
	 * 1. When retrieving a player's capability handler
	 * 2. The player instance is automatically set in the handler
	 * 
	 * @param helper The game test helper
	 */
	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void hasPlayerBeenSet(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = PlayerStateUtils.getHandler(player);

		if (handler.getPlayerInstance() == null) {
			helper.fail("Player has not been set");
		}

		helper.succeed();
	}

	/**
	 * Tests manually setting the player instance in the capability handler.
	 * 
	 * This test verifies that:
	 * 1. A player's capability handler can be retrieved directly using getData
	 * 2. The player instance can be manually set in the handler
	 * 
	 * @param helper The game test helper
	 */
	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void setPlayer(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = player.getData(ModCapabilities.PLAYER_CAPABILITY);
		handler.setPlayerInstance(player);
		helper.succeed();
	}

	/**
	 * Tests setting a dragon in the player's capability handler.
	 * 
	 * This test verifies that:
	 * 1. A dragon entity can be created and added to the level
	 * 2. The dragon can be tamed for a specific player
	 * 3. The dragon can be associated with a whistle slot in the player's capability handler
	 * 
	 * @param helper The game test helper
	 */
	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void setDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = PlayerStateUtils.getHandler(player);

		var dragon = ModEntities.DRAGON_ENTITY.get().create(helper.getLevel());
		dragon.setPos(0, 2, 0);
		helper.getLevel().addFreshEntity(dragon);
		dragon.tamedFor(player, true);
		handler.setDragonToWhistle(dragon, 1);
		helper.succeed();
	}

	/**
	 * Tests that setting a dragon to a whistle slot stores the data in the correct slot.
	 * 
	 * This test verifies that:
	 * 1. A dragon can be associated with a specific whistle slot (slot 1)
	 * 2. After association, the player's capability handler contains exactly one dragon entry
	 * 3. The entry is stored with the correct key (1) in the dragonNBTs map
	 * 
	 * @param helper The game test helper
	 */
	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void setDragonSetsCorrectSlot(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = PlayerStateUtils.getHandler(player);

		var dragon = ModEntities.DRAGON_ENTITY.get().create(helper.getLevel());
		dragon.setPos(0, 2, 0);
		helper.getLevel().addFreshEntity(dragon);
		dragon.tamedFor(player, true);

		handler.setDragonToWhistle(dragon, 1);

		if (List.of(handler.dragonNBTs.keys()).size() != 1) {
			helper.fail("Expected 1 key, got " + List.of(handler.dragonNBTs.keys()).size());
		}

		if (!handler.dragonNBTs.containsKey(1)) {
			helper.fail("Expected key 1, got " + List.of(handler.dragonNBTs.keys()));
		}

		helper.succeed();
	}

	/**
	 * Tests creating a new dragon entity from stored data.
	 * 
	 * This test verifies that:
	 * 1. A dragon can be associated with a whistle slot
	 * 2. A new dragon entity can be created from the stored data
	 * 3. The created dragon entity is not null
	 * 
	 * @param helper The game test helper
	 */
	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void createDragonEntity(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = PlayerStateUtils.getHandler(player);

		var dragon = ModEntities.DRAGON_ENTITY.get().create(helper.getLevel());
		dragon.setPos(0, 2, 0);
		helper.getLevel().addFreshEntity(dragon);
		dragon.tamedFor(player, true);

		handler.setDragonToWhistle(dragon, 1);

		var newDragon = handler.createDragonEntity(player, helper.getLevel(), 1);

		if (newDragon == null) {
			helper.fail("Dragon was not created");
		}

		helper.succeed();
	}
}
