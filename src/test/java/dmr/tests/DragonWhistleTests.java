package dmr.tests;

import dmr.DMRTestConstants;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler.DragonInstance;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.Objects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragon Whistles")
public class DragonWhistleTests {

    /**
     * Tests the DragonWhistleHandler.getDragonWhistleItem method.
     *
     * <p>
     * This test verifies that: 1. When a player has no dragon whistle, the method
     * returns null 2. When a player has a dragon whistle in their main hand and the
     * corresponding dragon data, the method correctly returns the whistle item
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void findWhistleItem(ExtendedGameTestHelper helper) {
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
            handler.dragonNBTs.put(
                    ((DragonWhistleItem) whistle.get()).getColor().getId(), new CompoundTag());
            handler.dragonInstances.put(
                    ((DragonWhistleItem) whistle.get()).getColor().getId(),
                    new DragonInstance(player.level, null, null));

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
     * <p>
     * This test verifies that: 1. When a player has no dragon whistle, the method
     * returns -1 2. When a player has a dragon whistle in their main hand and the
     * corresponding dragon data, the method correctly returns the whistle's color
     * ID
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void getWhistleIndex(ExtendedGameTestHelper helper) {
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
            player.getData(ModCapabilities.PLAYER_CAPABILITY).setPlayerInstance(player);
            var handler = PlayerStateUtils.getHandler(player);
            int whistleId = ((DragonWhistleItem) whistle.get()).getColor().getId();
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
     * <p>
     * This test verifies that: 1. When a dragon is set to a whistle index, the
     * dragon instance is properly stored in the player's capability 2. The stored
     * dragon instance has the correct UUID matching the original dragon
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void bindDragonToWhistle(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Set dragon to whistle
        DragonWhistleHandler.setDragon(player, dragon, 1);

        // Verify dragon is set
        if (!player.getData(ModCapabilities.PLAYER_CAPABILITY).dragonInstances.containsKey(1)) {
            helper.fail("Dragon instance not found in player capability");
        }

        var instance = player.getData(ModCapabilities.PLAYER_CAPABILITY)
                .dragonInstances
                .get(1);
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
     * <p>
     * This test verifies that: 1. When a player has no dragon whistle (index = -1),
     * the canCall method returns false
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void cannotCallWithoutWhistle(ExtendedGameTestHelper helper) {
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
     * Tests the DragonWhistleHandler.canCall method when a player has a whistle but
     * no dragon.
     *
     * <p>
     * This test verifies that: 1. When a player has a dragon whistle but no
     * associated dragon, the canCall method returns false
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void cannotCallWithoutDragon(ExtendedGameTestHelper helper) {
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
     * <p>
     * This test verifies that: 1. When a dragon is set to a whistle index, the
     * findDragon method can locate the dragon 2. The found dragon has the correct
     * UUID matching the original dragon
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void locateBoundDragon(ExtendedGameTestHelper helper) {
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
     * <p>
     * This test verifies that: 1. When a player has a dragon whistle and an
     * associated dragon, the callDragon method returns true 2. The dragon can be
     * successfully called using the whistle
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate
    @GameTest
    @TestHolder
    public static void callBoundDragon(ExtendedGameTestHelper helper) {
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
            int whistleId = ((DragonWhistleItem) whistle.get()).getColor().getId();
            if (whistleId == 1) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                handler.dragonInstances.put(
                        whistleId, new DragonInstance(player.level, dragon.getUUID(), dragon.getDragonUUID()));
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
     * <p>
     * This test verifies that: 1. When a player has a dragon whistle and an
     * associated dragon, the summonDragon method updates the lastCall timestamp 2.
     * The lastCall timestamp is different after summoning the dragon
     *
     * @param helper
     *            The game test helper
     */
    @EmptyTemplate
    @GameTest
    @TestHolder
    public static void summonUpdatesLastCall(ExtendedGameTestHelper helper) {
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
            int whistleId = ((DragonWhistleItem) whistle.get()).getColor().getId();

            if (whistleId == 1) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                handler.dragonInstances.put(
                        whistleId, new DragonInstance(player.level, dragon.getUUID(), dragon.getDragonUUID()));
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

    /**
     * Tests that a dragon follows the player when called with a whistle.
     *
     * <p>
     * This test verifies that:
     * 1. A dragon can be tamed and bound to a whistle
     * 2. When called with a whistle, the dragon will follow the player
     * 3. The dragon will teleport to the player if it's too far away
     *
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void dragonFollowsWhenCalled(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Spawn and tame a dragon
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());
        dragon.tamedFor(player, true);

        // Set dragon to whistle
        DragonWhistleHandler.setDragon(player, dragon, 1);

        // Setup player capability to recognize this whistle
        var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
        cap.setPlayerInstance(player);
        var handler = PlayerStateUtils.getHandler(player);

        // Add a whistle to player's hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            int whistleId = ((DragonWhistleItem) whistle.get()).getColor().getId();

            if (whistleId == 1) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                break;
            }
        }

        // Move dragon far away
        dragon.setPos(dragon.getX() + 5, dragon.getY(), dragon.getZ() + 5);

        // Call the dragon
        boolean result = DragonWhistleHandler.callDragon(player);
        if (!result) {
            helper.fail("Failed to call dragon");
        }

        dragon = DragonWhistleHandler.findDragon(player, 1);

        // Tick entities to allow the dragon to respond
        for (int i = 0; i < 20; i++) {
            player.tick();
            dragon.tick();
        }

        // Verify the dragon is now close to the player
        if (dragon.position().distanceTo(player.position()) > 10) {
            helper.fail("Dragon did not teleport to player when called");
        }

        helper.succeed();
    }

    /**
     * Tests that multiple dragons can be called with different whistles.
     *
     * <p>
     * This test verifies that:
     * 1. Multiple dragons can be tamed and bound to different whistles
     * 2. Each dragon can be called using its specific whistle
     * 3. The correct dragon responds to each whistle
     *
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void callMultipleDragons(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Spawn and tame multiple dragons
        var dragon1 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        var dragon2 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS.offset(2, 0, 0));

        dragon1.setBreed(DragonBreedsRegistry.getDefault());
        dragon2.setBreed(DragonBreedsRegistry.getDefault());

        dragon1.tamedFor(player, true);
        dragon2.tamedFor(player, true);

        // Setup player capability
        var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
        cap.setPlayerInstance(player);

        // Set dragons to different whistles
        DragonWhistleHandler.setDragon(player, dragon1, 0);
        DragonWhistleHandler.setDragon(player, dragon2, 1);

        // Move dragons far away
        dragon1.setPos(dragon1.getX() + 50, dragon1.getY(), dragon1.getZ() + 50);
        dragon2.setPos(dragon2.getX() - 50, dragon2.getY(), dragon2.getZ() - 50);

        dragon1.setOrderedToSit(true);
        dragon2.setOrderedToSit(true);

        // Store original positions
        var dragon1OrigPos = dragon1.position();
        var dragon2OrigPos = dragon2.position();

        // Add whistle 0 to player's hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            int whistleId = ((DragonWhistleItem) whistle.get()).getColor().getId();

            if (whistleId == 0) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                break;
            }
        }

        // Call dragon1 using whistle 0
        boolean callResult = DragonWhistleHandler.callDragon(player);

        if (!callResult) {
            helper.fail("Failed to call dragon1 with whistle 0");
        }

        dragon1 = DragonWhistleHandler.findDragon(player, 0);

        // Tick entities to allow the dragon to respond
        for (int i = 0; i < 20; i++) {
            player.tick();
            dragon1.tick();
            dragon2.tick();
        }

        // Verify dragon1 moved toward the player
        if (dragon1.position().distanceTo(player.position()) > 10) {
            helper.fail("Dragon1 did not teleport to player when called with whistle 0");
        }

        // Verify dragon2 stayed in place
        if (dragon2.position().distanceTo(dragon2OrigPos) > 5) {
            helper.fail("Dragon2 moved when whistle 0 was used");
        }

        // Move dragons far away again
        dragon1.setPos(dragon1.getX() + 50, dragon1.getY(), dragon1.getZ() + 50);
        dragon2.setPos(dragon2.getX() - 50, dragon2.getY(), dragon2.getZ() - 50);

        // Update original positions
        dragon1OrigPos = dragon1.position();

        // Add whistle 1 to player's hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            int whistleId = ((DragonWhistleItem) whistle.get()).getColor().getId();

            if (whistleId == 1) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                break;
            }
        }

        // Call dragon2 using whistle 1
        callResult = DragonWhistleHandler.callDragon(player);

        if (!callResult) {
            helper.fail("Failed to call dragon2 with whistle 1");
        }

        dragon2 = DragonWhistleHandler.findDragon(player, 1);

        // Tick entities to allow the dragon to respond
        for (int i = 0; i < 20; i++) {
            player.tick();
            dragon1.tick();
            dragon2.tick();
        }

        // Verify dragon2 moved toward the player
        if (dragon2.position().distanceTo(player.position()) > 10) {
            helper.fail("Dragon2 did not teleport to player when called with whistle 1");
        }

        // Verify dragon1 stayed in place
        if (dragon1.position().distanceTo(dragon1OrigPos) > 5) {
            helper.fail("Dragon1 moved when whistle 1 was used");
        }

        helper.succeed();
    }

    /**
     * Tests that a dragon can be called across dimensions.
     *
     * <p>
     * This test verifies that:
     * 1. A dragon can be tamed and bound to a whistle
     * 2. When the dragon is in a different dimension, it can still be called
     * 3. The dragon's inventory is transferred correctly between dimensions
     *
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void callAcrossDimensions(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Spawn and tame a dragon
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());
        dragon.tamedFor(player, true);

        // Set dragon to whistle
        DragonWhistleHandler.setDragon(player, dragon, 0);

        // Setup player capability
        var cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
        cap.setPlayerInstance(player);

        // Add a whistle to player's hand
        for (var whistle : ModItems.DRAGON_WHISTLES.values()) {
            int whistleId = ((DragonWhistleItem) whistle.get()).getColor().getId();

            if (whistleId == 0) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(whistle.get()));
                break;
            }
        }

        var netherDim = helper.getLevel().getServer().getLevel(Level.NETHER);
        var dimensionTransition = new DimensionTransition(
                netherDim, new Vec3(0, 0, 0), new Vec3(0, 0, 0), 0, 0, true, DimensionTransition.DO_NOTHING);

        var netherDragon = (TameableDragonEntity) dragon.changeDimension(dimensionTransition);

        // Give the dragon a chest with items
        netherDragon.equipChest(new ItemStack(Blocks.CHEST), SoundSource.MASTER);
        netherDragon.getInventory().setItem(0, new ItemStack(ModItems.DRAGON_ARMOR.get(), 1));

        // Call the dragon from the other dimension
        boolean callResult = DragonWhistleHandler.callDragon(player);

        // Verify the call was successful
        if (!callResult) {
            helper.fail("Failed to call dragon from another dimension");
        }

        var checkDragon = DragonWhistleHandler.findDragon(player, 0);

        if (checkDragon == null) {
            helper.fail("Dragon was not found after being called from another dimension");
        }

        // Verify the dragon's inventory was transferred
        if (!checkDragon.hasChest() || checkDragon.getInventory().getItem(0).isEmpty()) {
            helper.fail("Dragon's inventory was not transferred correctly between dimensions");
        }

        helper.succeed();
    }

    /**
     * Tests that tamed dragons don't despawn when far away.
     *
     * <p>
     * This test verifies that:
     * 1. Multiple dragons can be tamed by a player
     * 2. Tamed dragons don't despawn when far away
     * 3. The removeWhenFarAway method returns false for tamed dragons
     *
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void tamedDragonsDoNotDespawn(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Spawn and tame multiple dragons
        var dragon1 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        var dragon2 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS.offset(2, 0, 0));
        var dragon3 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS.offset(0, 0, 2));

        dragon1.setBreed(DragonBreedsRegistry.getDefault());
        dragon2.setBreed(DragonBreedsRegistry.getDefault());
        dragon3.setBreed(DragonBreedsRegistry.getDefault());

        // Tame all dragons
        dragon1.tamedFor(player, true);
        dragon2.tamedFor(player, true);
        dragon3.tamedFor(player, true);

        // Verify all dragons are tamed
        if (!dragon1.isTame() || !dragon2.isTame() || !dragon3.isTame()) {
            helper.fail("Not all dragons were tamed");
        }

        // Move dragons far away to test despawning
        dragon1.setPos(dragon1.getX() + 100, dragon1.getY(), dragon1.getZ() + 100);
        dragon2.setPos(dragon2.getX() + 100, dragon2.getY(), dragon2.getZ() - 100);
        dragon3.setPos(dragon3.getX() - 100, dragon3.getY(), dragon3.getZ() - 100);

        // Tick entities to allow potential despawning
        for (int i = 0; i < 100; i++) {
            player.tick();
            dragon1.tick();
            dragon2.tick();
            dragon3.tick();
        }

        // Verify dragons didn't despawn
        if (!dragon1.isAlive()) {
            helper.fail("Dragon1 despawned despite being tamed");
        }

        if (!dragon2.isAlive()) {
            helper.fail("Dragon2 despawned despite being tamed");
        }

        if (!dragon3.isAlive()) {
            helper.fail("Dragon3 despawned despite being tamed");
        }

        // Verify removeWhenFarAway returns false for tamed dragons
        if (dragon1.removeWhenFarAway(100) || dragon2.removeWhenFarAway(100) || dragon3.removeWhenFarAway(100)) {
            helper.fail("removeWhenFarAway returned true for tamed dragons");
        }

        helper.succeed();
    }

    /**
     * Tests that multiple dragons can be bound to different whistles.
     *
     * <p>
     * This test verifies that:
     * 1. Multiple dragons can be bound to different whistles
     * 2. Each dragon is correctly bound to its assigned whistle
     * 3. The correct whistle indices are assigned to each dragon
     *
     * @param helper The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void bindMultipleDragons(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.moveToCentre();

        // Spawn and tame multiple dragons
        var dragon1 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        var dragon2 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS.offset(2, 0, 0));

        dragon1.setBreed(DragonBreedsRegistry.getDefault());
        dragon2.setBreed(DragonBreedsRegistry.getDefault());

        // Tame all dragons
        dragon1.tamedFor(player, true);
        dragon2.tamedFor(player, true);

        // Verify all dragons are tamed
        if (!dragon1.isTame() || !dragon2.isTame()) {
            helper.fail("Not all dragons were tamed");
        }

        // Bind dragons to whistles
        var handler = PlayerStateUtils.getHandler(player);

        // Bind dragon1 to whistle index 0
        PlayerStateUtils.getHandler(player).setDragonToWhistle(dragon1, 0);

        // Verify dragon1 is bound to whistle index 0
        if (!PlayerStateUtils.getHandler(player).isBoundToWhistle(dragon1)) {
            helper.fail("Dragon1 was not bound to whistle");
        }

        // Bind dragon2 to whistle index 1
        PlayerStateUtils.getHandler(player).setDragonToWhistle(dragon2, 1);

        // Verify dragon2 is bound to whistle index 1
        if (!PlayerStateUtils.getHandler(player).isBoundToWhistle(dragon2)) {
            helper.fail("Dragon2 was not bound to whistle");
        }

        // Verify correct whistle indices
        int index1 = DragonWhistleHandler.getDragonSummonIndex(player, dragon1.getDragonUUID());
        int index2 = DragonWhistleHandler.getDragonSummonIndex(player, dragon2.getDragonUUID());

        if (index1 != 0) {
            helper.fail("Dragon1 was bound to wrong whistle index: " + index1);
        }

        if (index2 != 1) {
            helper.fail("Dragon2 was bound to wrong whistle index: " + index2);
        }

        helper.succeed();
    }
}
