package dmr_test.tests;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.registry.datapack.DragonAbilityRegistry;
import dmr.DragonMounts.registry.datapack.DragonBreedsRegistry;
import dmr.DragonMounts.registry.entity.ModEntities;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.EventType;
import dmr_test.utils.DMRTestConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

/**
 * Comprehensive tests for generic dragon ability system functionality.
 * These tests verify that the ability system works correctly for all ability types
 * and conditions using production abilities to test the generic system.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "GenericAbilities")
public class GenericAbilityTests {

    /**
     * Tests the GenericConditionalEffectAbility system with in_lava condition.
     * Uses the lava_immunity ability to verify conditional effects work correctly.
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testConditionalEffectInLava(ExtendedGameTestHelper helper) {
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Add lava immunity ability to test conditional effects in lava
        DragonAbility lavaImmunityDef = DragonAbilityRegistry.getAbilityDefinition(DMR.id("lava_immunity"));
        if (lavaImmunityDef == null) {
            helper.fail("Lava immunity ability not found - cannot test conditional effects");
            return;
        }
        
        Ability lavaImmunityAbility = DragonAbilityRegistry.createAbilityInstance(lavaImmunityDef);
        if (lavaImmunityAbility == null) {
            helper.fail("Failed to create lava immunity ability instance");
            return;
        }
        
        lavaImmunityAbility.onInitialize(dragon);
        dragon.getAbilities().add(lavaImmunityAbility);

        // Verify dragon doesn't have effect initially
        if (dragon.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            helper.fail("Dragon should not have fire resistance initially");
            return;
        }

        // Create lava environment
        BlockPos dragonPos = dragon.blockPosition();
        helper.setBlock(dragonPos.below(), Blocks.LAVA);
        helper.setBlock(dragonPos, Blocks.LAVA);
        dragon.setPos(dragonPos.getX() + 0.5, dragonPos.getY(), dragonPos.getZ() + 0.5);

        helper.onEachTick(() -> {
            dragon.tick();
        });

        // Wait for ability to trigger and check for fire resistance
        helper.runAtTickTime(60, () -> {
            if (!dragon.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                helper.fail("Dragon should have fire resistance when in lava (conditional effect system failed)");
            }
            
            // Test condition change - remove lava
            helper.setBlock(dragonPos, Blocks.AIR);
            helper.setBlock(dragonPos.below(), Blocks.STONE);
            dragon.setPos(dragonPos.getX() + 0.5, dragonPos.getY(), dragonPos.getZ() + 0.5);
        });

        // Verify effect is removed when condition is no longer met
        helper.runAtTickTime(120, () -> {
            if (dragon.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                helper.fail("Dragon should not have fire resistance when not in lava (condition cleanup failed)");
            }
            helper.succeed();
        });
    }

    /**
     * Tests the GenericConditionalEffectAbility system with in_water condition.
     * Uses the tidal_strength ability to verify water-based conditional effects.
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testConditionalEffectInWater(ExtendedGameTestHelper helper) {
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Add tidal strength ability to test conditional effects in water
        DragonAbility tidalStrengthDef = DragonAbilityRegistry.getAbilityDefinition(DMR.id("tidal_strength"));
        if (tidalStrengthDef == null) {
            helper.fail("Tidal strength ability not found - cannot test water conditional effects");
            return;
        }
        
        Ability tidalStrengthAbility = DragonAbilityRegistry.createAbilityInstance(tidalStrengthDef);
        if (tidalStrengthAbility == null) {
            helper.fail("Failed to create tidal strength ability instance");
            return;
        }
        
        tidalStrengthAbility.onInitialize(dragon);
        dragon.getAbilities().add(tidalStrengthAbility);

        // Create water environment
        BlockPos dragonPos = dragon.blockPosition();
        helper.setBlock(dragonPos.below(), Blocks.WATER);
        helper.setBlock(dragonPos, Blocks.WATER);
        dragon.setPos(dragonPos.getX() + 0.5, dragonPos.getY(), dragonPos.getZ() + 0.5);

        helper.onEachTick(() -> {
            dragon.tick();
        });

        // Wait for ability to trigger and check for strength effect
        helper.runAtTickTime(60, () -> {
            if (!dragon.hasEffect(MobEffects.DAMAGE_BOOST)) {
                helper.fail("Dragon should have strength when in water (conditional effect system failed)");
            }
            helper.succeed();
        });
    }

    /**
     * Tests the GenericConditionalEffectAbility system with near_trees condition.
     * Uses the bark_skin ability to verify tree proximity conditional effects.
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testConditionalEffectNearTrees(ExtendedGameTestHelper helper) {
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Add bark skin ability to test conditional effects near trees
        DragonAbility barkSkinDef = DragonAbilityRegistry.getAbilityDefinition(DMR.id("bark_skin"));
        if (barkSkinDef == null) {
            helper.fail("Bark skin ability not found - cannot test tree proximity conditional effects");
            return;
        }
        
        Ability barkSkinAbility = DragonAbilityRegistry.createAbilityInstance(barkSkinDef);
        if (barkSkinAbility == null) {
            helper.fail("Failed to create bark skin ability instance");
            return;
        }
        
        barkSkinAbility.onInitialize(dragon);
        dragon.getAbilities().add(barkSkinAbility);

        // Place tree blocks near dragon
        BlockPos dragonPos = dragon.blockPosition();
        helper.setBlock(dragonPos.offset(1, 0, 0), Blocks.OAK_LOG);
        helper.setBlock(dragonPos.offset(-1, 0, 0), Blocks.OAK_LOG);
        helper.setBlock(dragonPos.offset(0, 1, 1), Blocks.OAK_LEAVES);
        helper.setBlock(dragonPos.offset(0, 1, -1), Blocks.OAK_LEAVES);

        helper.onEachTick(() -> {
            dragon.tick();
        });

        // Wait for ability to trigger and check for resistance effect
        helper.runAtTickTime(60, () -> {
            if (!dragon.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                helper.fail("Dragon should have resistance when near trees (conditional effect system failed)");
            }
            helper.succeed();
        });
    }

    /**
     * Tests the GenericEventTriggerAbility system with on_attack event.
     * Uses the chill_touch ability to verify event-based ability triggers.
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testEventTriggerOnAttack(ExtendedGameTestHelper helper) {
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Add chill touch ability to test event trigger system
        DragonAbility chillTouchDef = DragonAbilityRegistry.getAbilityDefinition(DMR.id("chill_touch"));
        if (chillTouchDef == null) {
            helper.fail("Chill touch ability not found - cannot test event trigger system");
            return;
        }
        
        Ability chillTouchAbility = DragonAbilityRegistry.createAbilityInstance(chillTouchDef);
        if (chillTouchAbility == null) {
            helper.fail("Failed to create chill touch ability instance");
            return;
        }
        
        chillTouchAbility.onInitialize(dragon);
        dragon.getAbilities().add(chillTouchAbility);

        // Spawn a zombie target
        var zombie = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, 
                                  DMRTestConstants.TEST_POS.offset(2, 0, 0));

        // Trigger the attack event directly
        dragon.triggerEventAbilities(EventType.ON_ATTACK, zombie);

        // Check if zombie got slowness effect from event trigger
        if (!zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            helper.fail("Zombie should have slowness after dragon attack event (event trigger system failed)");
        }
        
        helper.succeed();
    }

    /**
     * Tests that multiple abilities can work simultaneously without conflicts.
     * Verifies the ability system can handle multiple abilities of different types.
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testMultipleAbilitiesSimultaneous(ExtendedGameTestHelper helper) {
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Add multiple abilities to test system robustness
        DragonAbility lavaImmunityDef = DragonAbilityRegistry.getAbilityDefinition(DMR.id("lava_immunity"));
        DragonAbility barkSkinDef = DragonAbilityRegistry.getAbilityDefinition(DMR.id("bark_skin"));

        if (lavaImmunityDef == null || barkSkinDef == null) {
            helper.fail("Required abilities not found for multiple ability test");
            return;
        }

        Ability lavaImmunityAbility = DragonAbilityRegistry.createAbilityInstance(lavaImmunityDef);
        Ability barkSkinAbility = DragonAbilityRegistry.createAbilityInstance(barkSkinDef);

        if (lavaImmunityAbility == null || barkSkinAbility == null) {
            helper.fail("Failed to create ability instances for multiple ability test");
            return;
        }

        lavaImmunityAbility.onInitialize(dragon);
        barkSkinAbility.onInitialize(dragon);
        dragon.getAbilities().add(lavaImmunityAbility);
        dragon.getAbilities().add(barkSkinAbility);

        // Create environment that should trigger both abilities
        BlockPos dragonPos = dragon.blockPosition();
        helper.setBlock(dragonPos.below(), Blocks.LAVA);
        helper.setBlock(dragonPos, Blocks.LAVA);
        helper.setBlock(dragonPos.offset(1, 0, 0), Blocks.OAK_LOG);
        dragon.setPos(dragonPos.getX() + 0.5, dragonPos.getY(), dragonPos.getZ() + 0.5);

        helper.onEachTick(() -> {
            dragon.tick();
        });

        // Check that both abilities work simultaneously
        helper.runAtTickTime(60, () -> {
            boolean hasFireResistance = dragon.hasEffect(MobEffects.FIRE_RESISTANCE);
            boolean hasResistance = dragon.hasEffect(MobEffects.DAMAGE_RESISTANCE);

            if (!hasFireResistance) {
                helper.fail("Dragon should have fire resistance from lava immunity (multiple abilities failed)");
            }
            if (!hasResistance) {
                helper.fail("Dragon should have resistance from bark skin (multiple abilities failed)");
            }
            helper.succeed();
        });
    }

    /**
     * Tests that abilities work correctly with different tiers and levels.
     * Verifies the ability system supports tier/level progression.
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testAbilityTierAndLevelSystem(ExtendedGameTestHelper helper) {
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);
        dragon.setBreed(DragonBreedsRegistry.getDefault());

        // Test ability with different levels
        DragonAbility lavaImmunityDef = DragonAbilityRegistry.getAbilityDefinition(DMR.id("lava_immunity"));
        if (lavaImmunityDef == null) {
            helper.fail("Lava immunity ability not found for tier test");
            return;
        }
        
        Ability lavaImmunityAbility = DragonAbilityRegistry.createAbilityInstance(lavaImmunityDef);
        if (lavaImmunityAbility == null) {
            helper.fail("Failed to create ability instance for tier test");
            return;
        }
        
        // Test with level 2
        lavaImmunityAbility.setLevel(2);
        lavaImmunityAbility.onInitialize(dragon);
        dragon.getAbilities().add(lavaImmunityAbility);

        // Verify ability max tier matches definition
        if (lavaImmunityDef.getMaxTier() < 2) {
            helper.fail("Lava immunity should support at least tier 2 for testing");
            return;
        }

        // Create lava environment
        BlockPos dragonPos = dragon.blockPosition();
        helper.setBlock(dragonPos.below(), Blocks.LAVA);
        helper.setBlock(dragonPos, Blocks.LAVA);
        dragon.setPos(dragonPos.getX() + 0.5, dragonPos.getY(), dragonPos.getZ() + 0.5);

        helper.onEachTick(() -> {
            dragon.tick();
        });

        // Verify ability still works at higher level
        helper.runAtTickTime(60, () -> {
            if (!dragon.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                helper.fail("Dragon should have fire resistance at ability level 2 (tier system failed)");
            }
            helper.succeed();
        });
    }
}