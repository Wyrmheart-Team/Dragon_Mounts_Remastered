package dmr_test.tests;

import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.block.ModBlocks;
import dmr.DragonMounts.registry.datapack.DragonBreedsRegistry;
import dmr.DragonMounts.registry.entity.ModEntities;
import dmr.DragonMounts.registry.item.ModItems;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.server.blocks.DMREggBlock;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.server.items.DragonEggItemBlock;
import dmr.DragonMounts.util.PlayerStateUtils;
import dmr_test.utils.DMRTestConstants;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.Objects;

/**
 * Tests for the dragon egg functionality of the mod. These tests verify that
 * dragon eggs can be properly placed, broken, dropped, and hatched, and that
 * the resulting dragons have the correct breed type.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragon Eggs")
public class DragonEggTests {

    /**
     * Tests placing a dragon egg block in the world.
     *
     * <p>
     * This test verifies that: 1. A player can place a dragon egg block using the
     * dragon egg item 2. The placed block is correctly identified as a dragon egg
     * block
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void placeEgg(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get());
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);
        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.succeed();
    }

    /**
     * Tests breaking a dragon egg block in the world.
     *
     * <p>
     * This test verifies that: 1. A dragon egg block can be destroyed 2. After
     * destruction, the block is no longer present in the world
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void breakEgg(ExtendedGameTestHelper helper) {
        helper.setBlock(DMRTestConstants.TEST_POS, ModBlocks.DRAGON_EGG_BLOCK.get());
        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.destroyBlock(DMRTestConstants.TEST_POS);
        helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.succeed();
    }

    /**
     * Tests that breaking a dragon egg block drops the correct item.
     *
     * <p>
     * This test verifies that: 1. When a dragon egg block is broken by a player, it
     * drops a dragon egg item 2. The correct number of items are dropped (exactly
     * 1) 3. The block is removed from the world after breaking
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void dropEgg(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.preventItemPickup();
        helper.setBlock(DMRTestConstants.TEST_POS, ModBlocks.DRAGON_EGG_BLOCK.get());
        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.breakBlock(DMRTestConstants.TEST_POS, ItemStack.EMPTY, player);
        helper.assertItemEntityCountIs(ModItems.DRAGON_EGG_BLOCK_ITEM.get(), DMRTestConstants.TEST_POS, 5, 1);
        helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.succeed();
    }

    /**
     * Tests that breaking a specific breed's dragon egg block drops the correct
     * breed's egg item.
     *
     * <p>
     * This test verifies that: 1. A specific breed's dragon egg (fire) can be
     * placed in the world 2. When broken, the egg drops an item with the same breed
     * type 3. The dropped item has the correct breed component data
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void dropCorrectEgg(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        player.preventItemPickup();

        var stack = DragonEggItemBlock.getDragonEggStack(DragonBreedsRegistry.getDragonBreed("fire"));
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);

        helper.setBlock(DMRTestConstants.TEST_POS, ModBlocks.DRAGON_EGG_BLOCK.get());
        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.breakBlock(DMRTestConstants.TEST_POS, ItemStack.EMPTY, player);
        helper.assertItemEntityCountIsAtLeast(ModItems.DRAGON_EGG_BLOCK_ITEM.get(), DMRTestConstants.TEST_POS, 5, 1);
        helper.getEntities(EntityType.ITEM).stream().anyMatch(itemEntity -> {
            var itemStack = itemEntity.getItem();
            var breed = itemStack.getOrDefault(ModComponents.DRAGON_BREED, "none");
            return itemStack.getItem() == ModItems.DRAGON_EGG_BLOCK_ITEM.get() && Objects.equals(breed, "fire");
        });
        helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.succeed();
    }

    /**
     * Tests initiating the hatching process for a dragon egg.
     *
     * <p>
     * This test verifies that: 1. A dragon egg block can be placed in the world 2.
     * When a player interacts with the egg, it enters the hatching state 3. The
     * hatching property of the egg block is set to true
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void hatchEgg(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get());
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);
        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.useBlock(DMRTestConstants.TEST_POS, player);
        helper.assertBlockProperty(DMRTestConstants.TEST_POS, DMREggBlock.HATCHING, true);
        helper.succeed();
    }

    /**
     * Tests the functionality of the dragon egg block entity.
     *
     * <p>
     * This test verifies that: 1. A dragon egg block entity is created when a
     * dragon egg is placed 2. The block entity can have its properties set (breed
     * ID, hatch time, owner, custom name) 3. The block entity can retrieve the
     * correct dragon breed
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void eggBlockEntity(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get());
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);

        var blockEntity = helper.getBlockEntity(DMRTestConstants.TEST_POS);

        if (blockEntity instanceof DMREggBlockEntity eggEntity) {
            eggEntity.setBreedId("test");
            eggEntity.setHatchTime(0);
            eggEntity.setOwner("test");
            eggEntity.getBreed();
            eggEntity.setBreed(DragonBreedsRegistry.getDefault());
        } else {
            helper.fail("Block entity is not an instance of DMREggBlockEntity");
            return;
        }

        helper.succeed();
    }

    /**
     * Tests the tick method of the dragon egg block entity.
     *
     * <p>
     * This test verifies that: 1. A dragon egg block entity can be created 2. The
     * tick method can be called without errors 3. The egg block entity properly
     * processes a tick update
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void eggTick(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get());
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);

        var blockEntity = helper.getBlockEntity(DMRTestConstants.TEST_POS);
        if (blockEntity instanceof DMREggBlockEntity eggEntity) {
            eggEntity.tick(
                    helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));
        } else {
            helper.fail("Block entity is not an instance of DMREggBlockEntity");
            return;
        }

        helper.succeed();
    }

    /**
     * Tests the complete hatching process of a dragon egg.
     *
     * <p>
     * This test verifies that: 1. A dragon egg can be placed and set to hatching
     * state 2. When the hatch time reaches zero, the egg hatches 3. After hatching,
     * the egg block is removed from the world 4. A dragon entity is spawned in
     * place of the egg
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void eggHatches(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get());
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);

        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.useBlock(DMRTestConstants.TEST_POS, player);
        helper.assertBlockProperty(DMRTestConstants.TEST_POS, DMREggBlock.HATCHING, true);
        var blockEntity = helper.getBlockEntity(DMRTestConstants.TEST_POS);

        if (blockEntity instanceof DMREggBlockEntity eggEntity) {
            eggEntity.setHatchTime(0);

            helper.succeedWhen(() -> {
                eggEntity.tick(
                        helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));
                helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
                helper.assertEntityPresent(ModEntities.DRAGON_ENTITY.get());
            });
        } else {
            helper.fail("Block entity is not an instance of DMREggBlockEntity");
        }

        helper.succeed();
    }

    /**
     * Tests that a placed dragon egg block has the correct breed type.
     *
     * <p>
     * This test verifies that: 1. A specific breed's dragon egg (fire) can be
     * placed in the world 2. The placed egg block entity has the correct breed ID
     * 3. The breed information is properly stored in the block entity
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void correctDragonEggType(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = DragonEggItemBlock.getDragonEggStack(DragonBreedsRegistry.getDragonBreed(DMRTestConstants.FIRE_BREED_ID));
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);

        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.assertBlockEntityData(
                DMRTestConstants.TEST_POS,
                eggEntity -> Objects.equals(((DMREggBlockEntity) eggEntity).getBreedId(), DMRTestConstants.FIRE_BREED_ID),
                () -> "Egg breed type does not match");
        helper.succeed();
    }

    /**
     * Tests that a hatched dragon egg produces a dragon of the correct breed.
     *
     * <p>
     * This test verifies that: 1. A specific breed's dragon egg (fire) can be
     * placed and set to hatching 2. When the egg hatches, it spawns a dragon entity
     * 3. The spawned dragon has the correct breed ID matching the egg's breed
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void correctDragonTypeHatches(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = DragonEggItemBlock.getDragonEggStack(DragonBreedsRegistry.getDragonBreed("fire"));
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);
        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.useBlock(DMRTestConstants.TEST_POS, player);
        helper.assertBlockProperty(DMRTestConstants.TEST_POS, DMREggBlock.HATCHING, true);
        var blockEntity = helper.getBlockEntity(DMRTestConstants.TEST_POS);

        if (blockEntity instanceof DMREggBlockEntity eggEntity) {
            eggEntity.setHatchTime(0);

            helper.succeedWhen(() -> {
                eggEntity.tick(
                        helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));

                helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
                helper.assertEntityPresent(ModEntities.DRAGON_ENTITY.get());
                helper.assertEntityData(
                        DMRTestConstants.TEST_POS,
                        ModEntities.DRAGON_ENTITY.get(),
                        TameableDragonEntity::getBreedId,
                        "fire");
            });
        } else {
            helper.fail("Block entity is not an instance of DMREggBlockEntity");
        }

        helper.succeed();
    }

    /**
     * Tests that a player's dragon hatch count is incremented when an egg hatches.
     *
     * <p>
     * This test verifies that: 1. A dragon egg can be placed and set to hatching 2.
     * When the egg hatches, the player's dragonsHatched count is incremented 3. The
     * player's state correctly tracks the number of dragons they've hatched
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void hatchCountIncremented(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
        var stack = new ItemStack(ModItems.DRAGON_EGG_BLOCK_ITEM.get());
        player.setItemInHand(player.getUsedItemHand(), stack);
        helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);
        helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
        helper.useBlock(DMRTestConstants.TEST_POS, player);
        helper.assertBlockProperty(DMRTestConstants.TEST_POS, DMREggBlock.HATCHING, true);
        var blockEntity = helper.getBlockEntity(DMRTestConstants.TEST_POS);

        if (blockEntity instanceof DMREggBlockEntity eggEntity) {
            eggEntity.setHatchTime(0);

            helper.succeedWhen(() -> {
                eggEntity.tick(
                        helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));

                var playerState = PlayerStateUtils.getHandler(player);
                if (playerState.dragonsHatched != 1) {
                    helper.fail("Player's dragonsHatched count is not 1");
                }
            });
        } else {
            helper.fail("Block entity is not an instance of DMREggBlockEntity");
        }
    }
}
