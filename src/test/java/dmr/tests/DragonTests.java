package dmr.tests;

import dmr.DMRTestConstants;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.items.DragonArmorItem;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.Objects;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragons")
public class DragonTests {

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void tameDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TROPICAL_FISH, 64));

		for (int i = 0; i < 100; i++) {
			dragon.interact(player, InteractionHand.MAIN_HAND);
			if (dragon.isTame()) {
				helper.succeed();
				return;
			}
		}

		helper.fail("Dragon was not tamed");
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void tamedFor(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());
		dragon.tamedFor(player, true);

		if (!dragon.isTame()) {
			helper.fail("Dragon was not tamed");
		}

		if (dragon.getOwner() != null && !Objects.equals(dragon.getOwner().getId(), player.getId())) {
			helper.fail("Dragon owner is not the player");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willAttackOwnerTarget(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		var target = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

		dragon.tamedFor(player, true);
		player.attack(target);

		if (dragon.getTarget() != target) {
			helper.fail("Dragon did not attack target");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void defendWhileSitting(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		var otherPlayer = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

		dragon.tamedFor(player, true);
		dragon.setOrderedToSit(true);

		player.attack(otherPlayer);

		if (dragon.getTarget() != otherPlayer) {
			helper.fail("Dragon did not defend while sitting");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willAttackNonOwner(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		var otherPlayer = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

		dragon.tamedFor(player, true);
		otherPlayer.attack(dragon);

		if (dragon.getTarget() != otherPlayer) {
			helper.fail("Dragon did not attack non-owner");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willNotAttackOwner(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		player.attack(dragon);

		if (dragon.getTarget() == player) {
			helper.fail("Dragon attacked owner");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willNotAttackTamed(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon1 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		var dragon2 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon1.setBreed(DragonBreedsRegistry.getDefault());
		dragon2.setBreed(DragonBreedsRegistry.getDefault());

		dragon1.tamedFor(player, true);
		dragon2.tamedFor(player, true);

		if (dragon1.getTarget() == dragon2) {
			helper.fail("Tamed dragon attacked another tamed dragon");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void dontMoveWhileSitting(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		dragon.setOrderedToSit(true);
		var pos = dragon.blockPosition();

		helper.onEachTick(dragon::tick);

		helper.succeedOnTickWhen(100, () -> {
			if (dragon.getDeltaMovement().length() > 0 || !dragon.blockPosition().equals(pos)) {
				helper.fail("Dragon moved while sitting");
			}
		});
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void saddleDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);

		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
		dragon.interact(player, InteractionHand.MAIN_HAND);

		if (!dragon.isSaddled()) {
			helper.fail("Dragon was not saddled");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void armorDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);

		player.setItemInHand(InteractionHand.MAIN_HAND, DragonArmorItem.getArmorStack(DragonArmorRegistry.getDragonArmor("iron")));
		dragon.interact(player, InteractionHand.MAIN_HAND);

		if (!dragon.isWearingArmor()) {
			helper.fail("Dragon was not armored");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void giveChestToDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);

		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST));
		dragon.interact(player, InteractionHand.MAIN_HAND);

		if (!dragon.hasChest()) {
			helper.fail("Dragon did not get chest");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void dontDropWhenSelected(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());
		dragon.tamedFor(player, true);

		dragon.setChest(true);
		dragon.inventory.setItem(0, new ItemStack(Items.DIAMOND, 64));
		PlayerStateUtils.getHandler(player).setDragon(dragon, 1);

		dragon.kill();

		helper.succeedWhen(() -> helper.assertItemEntityNotPresent(Items.DIAMOND, helper.relativePos(dragon.blockPosition()), 3));
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void dropChestContentsOnDeath(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		dragon.setChest(true);
		dragon.inventory.setItem(0, new ItemStack(Items.DIAMOND, 64));

		dragon.kill();

		helper.succeedWhen(() -> helper.assertItemEntityCountIsAtLeast(Items.DIAMOND, helper.relativePos(dragon.blockPosition()), 3, 64));
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void dropSaddleOnDeath(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		dragon.setSaddled(true);

		dragon.kill();

		helper.succeedWhen(() -> helper.assertItemEntityCountIsAtLeast(Items.SADDLE, helper.relativePos(dragon.blockPosition()), 3, 1));
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void dropArmorOnDeath(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		dragon.equipArmor(player, DragonArmorItem.getArmorStack(DragonArmorRegistry.getDragonArmor("iron")));

		dragon.kill();

		helper.succeedWhen(() ->
			helper.assertItemEntityCountIsAtLeast(ModItems.DRAGON_ARMOR.get(), helper.relativePos(dragon.blockPosition()), 3, 1)
		);
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void rideDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);

		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
		dragon.interact(player, InteractionHand.MAIN_HAND);

		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		dragon.interact(player, InteractionHand.MAIN_HAND);

		if (!player.isPassenger()) {
			helper.fail("Player is not riding dragon");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void dismountDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);

		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
		dragon.interact(player, InteractionHand.MAIN_HAND);

		PlayerStateUtils.getHandler(player).shouldDismount = true;

		helper.succeedWhen(() -> {
			if (player.isPassenger()) {
				helper.fail("Player is still riding dragon");
			}
		});
	}
}
