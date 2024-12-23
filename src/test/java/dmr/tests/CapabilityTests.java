package dmr.tests;

import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Capabilities")
public class CapabilityTests {

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void getCapability(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = PlayerStateUtils.getHandler(player);
		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void hasPlayerBeenSet(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = PlayerStateUtils.getHandler(player);

		if (handler.getPlayerInstance() == null) {
			throw new GameTestAssertException("Player has not been set");
		}

		helper.succeed();
	}

	@EmptyTemplate(floor = true)
	@GameTest
	@TestHolder
	public static void setPlayer(ExtendedGameTestHelper helper) {
		var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);
		var handler = player.getData(ModCapabilities.PLAYER_CAPABILITY);
		handler.setPlayerInstance(player);
		helper.succeed();
	}

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
			throw new GameTestAssertException("Expected 1 key, got " + List.of(handler.dragonNBTs.keys()).size());
		}

		if (!handler.dragonNBTs.containsKey(1)) {
			throw new GameTestAssertException("Expected key 1, got " + List.of(handler.dragonNBTs.keys()));
		}

		helper.succeed();
	}

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
			throw new GameTestAssertException("Dragon was not created");
		}

		helper.succeed();
	}
}
