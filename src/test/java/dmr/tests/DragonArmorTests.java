package dmr.tests;

import dmr.DragonMounts.registry.DragonArmorRegistry;
import java.util.Objects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Dragon Armors")
public class DragonArmorTests {

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasArmors(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded
		if (DragonArmorRegistry.getDragonArmors().isEmpty()) {
			throw new GameTestAssertException("No dragon armors found");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasDefaultArmor(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		if (DragonArmorRegistry.getDefault() == null) {
			throw new GameTestAssertException("No default dragon armor found");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasFirstArmor(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		if (DragonArmorRegistry.getFirst() == null) {
			throw new GameTestAssertException("No first dragon armor found");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void getCorrectArmor(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded
		var armor = DragonArmorRegistry.getDragonArmor("test1");

		if (armor == null) {
			throw new GameTestAssertException("No test1 dragon armor found");
		}

		if (!Objects.equals(armor.getId(), "test1")) {
			throw new GameTestAssertException("Dragon armor has incorrect id");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasDragonArmor(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded
		var armor = DragonArmorRegistry.getDragonArmor("test1");

		if (armor == null) {
			throw new GameTestAssertException("No test1 dragon armor found");
		}

		helper.succeed();
	}
}
