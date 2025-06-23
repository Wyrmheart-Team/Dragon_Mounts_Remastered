package dmr_test.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Test constants and utilities for DMR game tests.
 * This class provides common test positions, items, and configurations
 * used across multiple test classes.
 */
public class DMRTestConstants {

    // Common test positions
    public static final BlockPos TEST_POS = new BlockPos(1, 2, 1);
    public static final BlockPos SPAWN_POS = new BlockPos(0, 1, 0);
    public static final BlockPos ELEVATED_POS = new BlockPos(1, 10, 1);
    public static final BlockPos CORNER_POS = new BlockPos(15, 2, 15);

    // Test item stacks
    public static final ItemStack SADDLE_STACK = new ItemStack(Items.SADDLE);
    public static final ItemStack CHEST_STACK = new ItemStack(Items.CHEST);
    public static final ItemStack TROPICAL_FISH_STACK = new ItemStack(Items.TROPICAL_FISH, 64);
    public static final ItemStack DIAMOND_STACK = new ItemStack(Items.DIAMOND, 32);
    public static final ItemStack EMERALD_STACK = new ItemStack(Items.EMERALD, 16);

    // Test timing constants (in ticks)
    public static final int SHORT_DELAY = 20;
    public static final int MEDIUM_DELAY = 60;
    public static final int LONG_DELAY = 200;
    public static final int VERY_LONG_DELAY = 600;

    // Test distances and ranges
    public static final double CLOSE_DISTANCE = 2.0;
    public static final double MEDIUM_DISTANCE = 5.0;
    public static final double FAR_DISTANCE = 10.0;
    public static final double VERY_FAR_DISTANCE = 20.0;

    // Test configuration values
    public static final int TEST_HATCH_TIME = 100;
    public static final int TEST_GROWTH_TIME = 200;
    public static final float TEST_DRAGON_SCALE = 1.0f;
    public static final float TEST_DRAGON_HEALTH = 40.0f;

    // Test breed and ability IDs
    public static final String FIRE_BREED_ID = "fire_test";
    public static final String ICE_BREED_ID = "ice_test";
    public static final String WATER_BREED_ID = "water_test";
    public static final String FOREST_BREED_ID = "forest_test";
    public static final String TEST_ABILITY_ID = "test_ability";
    public static final String AQUATIC_GRACE_ID = "aquatic_grace";
    public static final String SWIFT_SWIM_ID = "swift_swim";
    public static final String GEM_GUARD_ID = "gem_guard";
    public static final String CRYSTAL_HARMONY_ID = "crystal_harmony";

    // Test armor IDs
    public static final String IRON_ARMOR_ID = "iron_test";
    public static final String DIAMOND_ARMOR_ID = "diamond_test";

    // Utility methods for test setup
    public static BlockPos offsetPos(BlockPos base, int x, int y, int z) {
        return base.offset(x, y, z);
    }

    public static ItemStack createTestItem(net.minecraft.world.item.Item item, int count) {
        return new ItemStack(item, count);
    }
}
