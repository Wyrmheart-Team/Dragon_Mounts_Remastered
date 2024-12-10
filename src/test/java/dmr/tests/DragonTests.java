package dmr.tests;

import dmr.DMRTestConstants;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.items.DragonArmorItem;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.Objects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragons")
public class DragonTests {

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void tameDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		helper.succeedWhen(() -> {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TROPICAL_FISH, 64));
			dragon.interact(player, InteractionHand.MAIN_HAND);

			if (!dragon.isTame()) {
				helper.fail("Dragon was not tamed");
			}
		});
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void tamedFor(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
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
	public static void willLookAtPlayer(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());
		dragon.tamedFor(player, true);
		helper.succeedWhen(() ->
			dragon
				.getBrain()
				.getMemory(MemoryModuleType.LOOK_TARGET)
				.ifPresent(lookTarget -> {
					if (lookTarget.currentPosition().distanceTo(player.position()) > 0.5) {
						helper.fail("Dragon did not look at player");
					}
				})
		);
	}

	/*
	@EmptyTemplate(floor = true)
	@GameTest(setupTicks = 100L)
	@TestHolder
	public static void willAttackOwnerTarget(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();

		var target = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		target.moveToCorner();

		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		player.attack(target);

		helper.onEachTick(() -> {
			player.tick();
			target.tick();
			dragon.tick();
		});

		helper.succeedWhen(() -> {
			var dragonTarget = dragon.getTarget();
			if (dragonTarget == null || (!dragonTarget.is(target) && dragonTarget.getId() != target.getId())) {
				helper.fail("Dragon did not attack owner's target. Target was: " + dragonTarget);
			}
		});
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willDefendOwner(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();

		var target = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		target.moveToCorner();

		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		target.attack(player);

		helper.onEachTick(() -> {
			player.tick();
			target.tick();
			dragon.tick();
		});

		helper.succeedWhen(() -> {
			var dragonTarget = dragon.getTarget();
			if (dragonTarget == null || (!dragonTarget.is(target) && dragonTarget.getId() != target.getId())) {
				helper.fail("Dragon did not defend owner. Target was: " + dragonTarget);
			}
		});
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willAttackNonOwner(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		var otherPlayer = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

		dragon.tamedFor(player, true);
		player.attack(otherPlayer);

		helper.onEachTick(() -> {
			player.tick();
			otherPlayer.tick();
			dragon.tick();
		});

		helper.succeedWhen(() -> {
			var target = dragon.getTarget();
			if (target == null || (!target.is(otherPlayer) && target.getId() != otherPlayer.getId())) {
				helper.fail("Dragon did not attack non-owner. Target was: " + target);
			}
		});
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willNotAttackOwner(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		player.attack(dragon);

		player.tick();
		dragon.tick();

		var target = dragon.getTarget();
		if (target != null && (target.is(player) || target.getId() == player.getId())) {
			helper.fail("Dragon attacked owner");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void willNotAttackTamed(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
		var dragon1 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		var dragon2 = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon1.setBreed(DragonBreedsRegistry.getDefault());
		dragon2.setBreed(DragonBreedsRegistry.getDefault());

		dragon1.tamedFor(player, true);
		dragon2.tamedFor(player, true);

		player.attack(dragon2);

		helper.onEachTick(() -> {
			player.tick();
			dragon1.tick();
			dragon2.tick();
		});

		helper.succeedWhen(() -> {
			var target = dragon1.getTarget();

			if (target != null && (target.is(dragon2) || target.getId() == dragon2.getId())) {
				helper.fail("Dragon attacked tamed dragon. Target was: " + target);
			}
		});
	}
	

	@EmptyTemplate(floor = true, value = "9x9x9") //Larger area to ensure dragon can move
	@GameTest
	@TestHolder
	public static void dontMoveWhileSitting(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());

		dragon.tamedFor(player, true);
		dragon.setOrderedToSit(true);
		var pos = dragon.blockPosition();

		dragon
			.getBrain()
			.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(helper.relativePos(dragon.blockPosition().offset(5, 0, 5)), 1f, 0));

		for (int i = 0; i < 100; i++) {
			dragon.tick();
		}

		if (!dragon.blockPosition().equals(pos)) {
			helper.fail("Dragon moved while sitting");
		}

		helper.succeed();
	}
 

	@EmptyTemplate(floor = true, value = "9x9x9") //Larger area to ensure dragon can move
	@GameTest
	@TestHolder
	public static void wanderWhenNotSitting(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
		var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
		dragon.setBreed(DragonBreedsRegistry.getDefault());
		dragon.tamedFor(player, true);
		dragon.setOrderedToSit(false);
		var pos = dragon.blockPosition();

		dragon
			.getBrain()
			.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(helper.relativePos(dragon.blockPosition().offset(5, 0, 5)), 1f, 0));

		helper.succeedWhen(() -> {
			dragon.tick();

			if (dragon.blockPosition().equals(pos)) {
				helper.fail("Dragon did not move while not sitting");
			}
		});
	}
*/

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void saddleDragon(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		player.moveToCentre();
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
		player.moveToCentre();
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
		player.moveToCentre();
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
		player.moveToCentre();
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
		player.moveToCentre();
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
		player.moveToCentre();
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
		player.moveToCentre();
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
		player.moveToCentre();
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
		player.moveToCentre();
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
