package dmr.tests;

import dmr.DMRTestConstants;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler.DragonInstance;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.Objects;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "DragonWhistle")
public class DragonWhistleTests {

    /**
     * Tests the DragonWhistleHandler.getDragonWhistleItem method.
     * 
     * This test verifies that:
     * 1. When a player has no dragon whistle, the method returns null
     * 2. When a player has a dragon whistle in their main hand and the corresponding dragon data,
     *    the method correctly returns the whistle item
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void getDragonWhistleItem(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Test with no whistle
        var whistleItem = DragonWhistleHandler.getDragonWhistleItem(player);
        if (whistleItem != null) {
            helper.fail("Found whistle when none should exist");
        }

        // Test with whistle in main hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));

            // Setup player capability to recognize this whistle
            var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
            cap.setPlayerInstance(player);
            var handler = PlayerStateUtils.getHandler(player);
			handler.dragonNBTs.put(((DragonWhistleItem)whistle.get()).getColor().getId(), new CompoundTag());
			handler.dragonInstances.put(((DragonWhistleItem)whistle.get()).getColor().getId(), new DragonInstance(player.level, null, null));

            whistleItem = DragonWhistleHandler.getDragonWhistleItem(player);
            if (whistleItem == null) {
                helper.fail("Could not find whistle in main hand");
            }
        }

        helper.succeed();
    }

    /**
     * Tests the DragonWhistleHandler.getDragonSummonIndex method.
     * 
     * This test verifies that:
     * 1. When a player has no dragon whistle, the method returns -1
     * 2. When a player has a dragon whistle in their main hand and the corresponding dragon data,
     *    the method correctly returns the whistle's color ID
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void getDragonSummonIndex(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Test with no whistle
        var index = DragonWhistleHandler.getDragonSummonIndex(player);
        if (index != -1) {
            helper.fail("Found whistle index when none should exist");
        }

        // Test with whistle in main hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));

            // Setup player capability to recognize this whistle
            var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
            cap.setPlayerInstance(player);
            var handler = PlayerStateUtils.getHandler(player);
            int whistleId = ((DragonWhistleItem)whistle.get()).getColor().getId();
			handler.dragonNBTs.put(whistleId, new CompoundTag());
            handler.dragonInstances.put(whistleId, new DragonInstance(player.level, null, null));

            index = DragonWhistleHandler.getDragonSummonIndex(player);
            if (index != whistleId) {
                helper.fail("Incorrect whistle index: expected " + whistleId + " but got " + index);
            }
        }

        helper.succeed();
    }

    /**
     * Tests the DragonWhistleHandler.setDragon method.
     * 
     * This test verifies that:
     * 1. When a dragon is set to a whistle index, the dragon instance is properly stored in the player's capability
     * 2. The stored dragon instance has the correct UUID matching the original dragon
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void setDragonToWhistle(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Set dragon to whistle
        DragonWhistleHandler.setDragon(player, dragon, 1);

        // Verify dragon is set
        var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
        if (!cap.dragonInstances.containsKey(1)) {
            helper.fail("Dragon instance not found in player capability");
        }

        var instance = cap.dragonInstances.get(1);
        if (instance == null) {
            helper.fail("Dragon instance is null");
        }

        if (!instance.getUUID().equals(dragon.getDragonUUID())) {
            helper.fail("Dragon UUID mismatch");
        }

        helper.succeed();
    }

    /**
     * Tests the DragonWhistleHandler.canCall method when a player has no whistle.
     * 
     * This test verifies that:
     * 1. When a player has no dragon whistle (index = -1), the canCall method returns false
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void canCallWithNoWhistle(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Test with no whistle
        boolean canCall = DragonWhistleHandler.canCall(player, -1);
        if (canCall) {
            helper.fail("Player can call dragon with no whistle");
        }

        helper.succeed();
    }

    /**
     * Tests the DragonWhistleHandler.canCall method when a player has a whistle but no dragon.
     * 
     * This test verifies that:
     * 1. When a player has a dragon whistle but no associated dragon, the canCall method returns false
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void canCallWithNoDragon(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Test with whistle but no dragon
        boolean canCall = DragonWhistleHandler.canCall(player, 1);
        if (canCall) {
            helper.fail("Player can call dragon when no dragon exists");
        }

        helper.succeed();
    }

    /**
     * Tests the DragonWhistleHandler.findDragon method.
     * 
     * This test verifies that:
     * 1. When a dragon is set to a whistle index, the findDragon method can locate the dragon
     * 2. The found dragon has the correct UUID matching the original dragon
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void findDragon(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Set dragon to whistle
        DragonWhistleHandler.setDragon(player, dragon, 1);

        // Find dragon
        var foundDragon = DragonWhistleHandler.findDragon(player, 1);
        if (foundDragon == null) {
            helper.fail("Could not find dragon");
        }

        if (!foundDragon.getDragonUUID().equals(dragon.getDragonUUID())) {
            helper.fail("Found wrong dragon");
        }

        helper.succeed();
    }

    /**
     * Tests the DragonWhistleHandler.callDragon method.
     * 
     * This test verifies that:
     * 1. When a player has a dragon whistle and an associated dragon, the callDragon method returns true
     * 2. The dragon can be successfully called using the whistle
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate
    @GameTest
    @TestHolder
    public static void callDragon(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Set dragon to whistle
        DragonWhistleHandler.setDragon(player, dragon, 1);

        // Setup player capability to recognize this whistle
        var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
        cap.setPlayerInstance(player);
        var handler = PlayerStateUtils.getHandler(player);

        // Add a whistle to player's hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            int whistleId = ((DragonWhistleItem)whistle.get()).getColor().getId();
            if (whistleId == 1) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                handler.dragonInstances.put(whistleId, new DragonInstance(player.level, dragon.getUUID(), dragon.getDragonUUID()));
                handler.dragonNBTs.put(whistleId, new CompoundTag());
                break;
            }
        }

        // Call dragon
        boolean result = DragonWhistleHandler.callDragon(player);
        if (!result) {
            helper.fail("Failed to call dragon");
        }

        helper.succeed();
    }

    /**
     * Tests the DragonWhistleHandler.summonDragon method.
     * 
     * This test verifies that:
     * 1. When a player has a dragon whistle and an associated dragon, the summonDragon method updates the lastCall timestamp
     * 2. The lastCall timestamp is different after summoning the dragon
     * 
     * @param helper The game test helper
     */
    @EmptyTemplate
    @GameTest
    @TestHolder
    public static void summonDragon(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Set dragon to whistle
        DragonWhistleHandler.setDragon(player, dragon, 1);

        // Setup player capability to recognize this whistle
        var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
        cap.setPlayerInstance(player);
        var handler = PlayerStateUtils.getHandler(player);

        // Add a whistle to player's hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            int whistleId = ((DragonWhistleItem)whistle.get()).getColor().getId();

            if (whistleId == 1) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                handler.dragonInstances.put(whistleId, new DragonInstance(player.level, dragon.getUUID(), dragon.getDragonUUID()));
                handler.dragonNBTs.put(whistleId, new CompoundTag());
                break;
            }
        }

        // Record the last call time before summoning
        Long lastCallBefore = handler.lastCall;

        // Summon dragon
        DragonWhistleHandler.summonDragon(player);

        // Verify that lastCall was updated
        if (Objects.equals(handler.lastCall, lastCallBefore)) {
            helper.fail("Last call was not updated");
        }

        helper.succeed();
    }

}
