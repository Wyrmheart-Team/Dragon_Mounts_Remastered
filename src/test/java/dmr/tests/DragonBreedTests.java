package dmr.tests;

import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
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
@ForEachTest(groups = "Dragon Breeds")
public class DragonBreedTests {

	//succeedWhen is used in these tests due to the loadingtime of the data pack

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasBreeds(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		if (DragonBreedsRegistry.getDragonBreeds().isEmpty()) {
			throw new GameTestAssertException("No dragon breeds found");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasDefaultBreed(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		if (DragonBreedsRegistry.getDefault() == null) {
			throw new GameTestAssertException("No default dragon breed found");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasFirstBreed(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		if (DragonBreedsRegistry.getFirst() == null) {
			throw new GameTestAssertException("No first dragon breed found");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void getCorrectBreed(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		var breed = DragonBreedsRegistry.getDragonBreed("test1");
		if (breed == null) {
			throw new GameTestAssertException("No dragon breed found with id 'test1'");
		}

		if (!Objects.equals(breed.getId(), "test1")) {
			throw new GameTestAssertException("Dragon breed has incorrect id");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasDragonBreed(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		var breed = DragonBreedsRegistry.getDragonBreed("test1");
		if (breed == null) {
			throw new GameTestAssertException("No dragon breed found with id 'test1'");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasHybrids(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		if (DragonBreedsRegistry.getDragonBreeds().stream().noneMatch(IDragonBreed::isHybrid)) {
			throw new GameTestAssertException("No dragon hybrids found");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hasHybridBreed(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		var breed1 = DragonBreedsRegistry.getDragonBreed("test1");
		var breed2 = DragonBreedsRegistry.getDragonBreed("test2");
		var hybrid = DragonBreedsRegistry.getHybridBreed(breed1, breed2);
		if (hybrid == null || !hybrid.isHybrid()) {
			throw new GameTestAssertException("No hybrid breed found with parents 'test1' and 'test2'");
		}

		helper.succeed();
	}

	@EmptyTemplate
	@GameTest
	@TestHolder
	public static void hybridIsCorrect(ExtendedGameTestHelper helper) {
		helper.makeTickingMockServerPlayerInCorner(GameType.DEFAULT_MODE); //Required for data packs to be loaded

		var breed1 = DragonBreedsRegistry.getDragonBreed("test1");
		var breed2 = DragonBreedsRegistry.getDragonBreed("test2");
		var hybrid = DragonBreedsRegistry.getHybridBreed(breed1, breed2);
		if (hybrid == null || !hybrid.isHybrid()) {
			throw new GameTestAssertException("No hybrid breed found with parents 'test1' and 'test2'");
		}

		if (!(hybrid instanceof DragonHybridBreed hybridBreed)) {
			throw new GameTestAssertException("Hybrid breed is not an instance of DragonHybridBreed");
		}

		if (!hybridBreed.parent1.getId().equals("test1") || !hybridBreed.parent2.getId().equals("test2")) {
			throw new GameTestAssertException("Hybrid breed has incorrect parents");
		}

		if (!hybridBreed.getId().equals("hybrid_test1_test2")) {
			throw new GameTestAssertException("Hybrid breed has incorrect id");
		}

		helper.succeed();
	}
}
