package dmr.tests;

import dmr.DMRTestConstants;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.server.blocks.DMREggBlock;
import dmr.DragonMounts.server.entity.AbstractDMRDragonEntity;
import dmr.DragonMounts.server.items.DragonEggItemBlock;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragon Eggs")
public class DragonEggTests {

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
		helper
			.getEntities(EntityType.ITEM)
			.stream()
			.anyMatch(itemEntity -> {
				var itemStack = itemEntity.getItem();
				var breed = itemStack.getOrDefault(ModComponents.DRAGON_BREED, "none");
				return itemStack.getItem() == ModItems.DRAGON_EGG_BLOCK_ITEM.get() && Objects.equals(breed, "fire");
			});
		helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
		helper.succeed();
	}

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
			eggEntity.setCustomName(null);
			eggEntity.getBreed();
			eggEntity.setBreed(DragonBreedsRegistry.getDefault());
		} else {
			helper.fail("Block entity is not an instance of DMREggBlockEntity");
			return;
		}

		helper.succeed();
	}

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
			eggEntity.tick(helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));
		} else {
			helper.fail("Block entity is not an instance of DMREggBlockEntity");
			return;
		}

		helper.succeed();
	}

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
				eggEntity.tick(helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));
				helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
				helper.assertEntityPresent(ModEntities.DRAGON_ENTITY.get());
			});
		} else {
			helper.fail("Block entity is not an instance of DMREggBlockEntity");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void correctDragonEggType(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var stack = DragonEggItemBlock.getDragonEggStack(DragonBreedsRegistry.getDragonBreed("fire"));
		player.setItemInHand(player.getUsedItemHand(), stack);
		helper.placeAt(player, stack, DMRTestConstants.TEST_POS.below(), Direction.UP);

		helper.assertBlockPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
		helper.assertBlockEntityData(
			DMRTestConstants.TEST_POS,
			eggEntity -> Objects.equals(((DMREggBlockEntity) eggEntity).getBreedId(), "fire"),
			() -> "Egg breed type does not match"
		);
		helper.succeed();
	}

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
				eggEntity.tick(helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));

				helper.assertBlockNotPresent(ModBlocks.DRAGON_EGG_BLOCK.get(), DMRTestConstants.TEST_POS);
				helper.assertEntityPresent(ModEntities.DRAGON_ENTITY.get());
				helper.assertEntityData(
					DMRTestConstants.TEST_POS,
					ModEntities.DRAGON_ENTITY.get(),
					AbstractDMRDragonEntity::getBreedId,
					"fire"
				);
			});
		} else {
			helper.fail("Block entity is not an instance of DMREggBlockEntity");
		}

		helper.succeed();
	}

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
				eggEntity.tick(helper.getLevel(), DMRTestConstants.TEST_POS, helper.getBlockState(DMRTestConstants.TEST_POS));

				var playerState = PlayerStateUtils.getHandler(player);
				if (playerState.dragonsHatched != 1) {
					throw new GameTestAssertException("Player's dragonsHatched count is not 1");
				}
			});
		} else {
			helper.fail("Block entity is not an instance of DMREggBlockEntity");
		}
	}
}
