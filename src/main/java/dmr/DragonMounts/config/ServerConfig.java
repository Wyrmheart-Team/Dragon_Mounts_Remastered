package dmr.DragonMounts.config;

import dmr.DragonMounts.config.annotations.Config;
import dmr.DragonMounts.config.annotations.RangeConstraint;
import dmr.DragonMounts.server.entity.DragonConstants;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec MOD_CONFIG_SPEC;

    @Config(key = "hatch_time", comment = "Time in seconds for a dragon egg to hatch.")
    @RangeConstraint(min = 0, max = Integer.MAX_VALUE)
    public static Long HATCH_TIME_CONFIG = DragonConstants.HATCH_TIME;

    @Config(key = "growth_time", comment = "Time in seconds for a dragon to grow.")
    @RangeConstraint(min = 0, max = Integer.MAX_VALUE)
    public static Long GROWTH_TIME_CONFIG = DragonConstants.GROWTH_TIME;

    @Config(
            key = "allow_egg_override",
            comment = {
                "Allow the vanilla ender egg to be interacted with? (Hatchable)",
                "Useful to help with mod compatibility"
            },
            category = "eggs")
    public static boolean ALLOW_EGG_OVERRIDE = true;

    @Config(
            key = "replenish_eggs",
            comment = {
                "Should Ender Dragon Eggs replenish on the exit portal after a respawned dragon is defeated?",
                "Useful for multiplayer scenarios."
            },
            category = "eggs")
    public static boolean REPLENISH_EGGS = true;

    @Config(key = "allow_hybridization", comment = "Allow hybridization between dragons.", category = "eggs")
    public static boolean ALLOW_HYBRIDIZATION = true;

    @Config(
            key = "habitat_offspring",
            comment = "Offspring from breeding can turn into dragon type matching current environment.",
            category = "eggs")
    public static boolean HABITAT_OFFSPRING = true;

    @Config(
            key = "enable_blank_egg",
            comment = "Enable blank dragon eggs which changes based on the environment.",
            category = "eggs")
    public static boolean ENABLE_BLANK_EGG = false;

    @Config(key = "enable_natural_dragon_spawns", comment = "Enable or disable natural dragon spawns.")
    public static boolean ENABLE_NATURAL_DRAGON_SPAWNS = false;

    @Config(
            key = "dragon_history_size",
            comment =
                    "The maximum number of dragons to keep track of in the dragon history. This allows recalling missing dragons through commands. Larger values may increase world save size.")
    @RangeConstraint(min = 1, max = Integer.MAX_VALUE)
    public static int DRAGON_HISTORY_SIZE = 20;

    @Config(key = "base_health", comment = "Base health of all dragons.", category = "base_stats")
    @RangeConstraint(min = 1.0)
    public static double BASE_HEALTH = DragonConstants.BASE_HEALTH;

    @Config(key = "health_regen", comment = "Passive health regen value for dragons.", category = "base_stats")
    @RangeConstraint(min = 0)
    public static double HEALTH_REGEN = 1.0;

    @Config(key = "base_damage", comment = "Base damage of all dragons.", category = "base_stats")
    @RangeConstraint(min = 1.0)
    public static double BASE_DAMAGE = DragonConstants.BASE_DAMAGE;

    /** @deprecated Use {@code base_walking_speed} instead. */
    @Config(key = "base_speed", comment = "Base movement speed for all dragons.", category = "base_stats")
    @RangeConstraint(min = 0)
    @Deprecated()
    public static double BASE_SPEED = 1.0;

    @Config(key = "base_walking_speed", comment = "Base walking speed for all dragons.", category = "base_stats")
    @RangeConstraint(min = 0)
    public static double BASE_WALKING_SPEED = 1.0;

    @Config(key = "base_flying_speed", comment = "Base flying speed for all dragons.", category = "base_stats")
    @RangeConstraint(min = 0)
    public static double BASE_FLYING_SPEED = 1.0;

    @Config(key = "base_swimming_speed", comment = "Base swimming speed for all dragons.", category = "base_stats")
    @RangeConstraint(min = 0)
    public static double BASE_SWIMMING_SPEED = 1.0;

    @Config(key = "size_modifier", comment = "Size modifier for all dragons.", category = "base_stats")
    @RangeConstraint(min = 0.01)
    public static double SIZE_MODIFIER = 1.0;

    @Config(
            key = "enable_random_stats",
            comment = "Whether to enable random stats for dragons.",
            category = {"base_stats", "random_stats"})
    public static boolean ENABLE_RANDOM_STATS = true;

    @Config(
            key = "upper_max_health",
            comment = "The maximum health bonus for dragons with random stats.",
            category = {"base_stats", "random_stats"})
    @RangeConstraint(min = 0)
    public static int UPPER_MAX_HEALTH = 10;

    @Config(
            key = "upper_damage",
            comment = "The maximum damage bonus for dragons with random stats.",
            category = {"base_stats", "random_stats"})
    @RangeConstraint(min = 0)
    public static double UPPER_DAMAGE = 5.0;

    @Config(
            key = "upper_speed",
            comment = "The maximum speed bonus for dragons with random stats.",
            category = {"base_stats", "random_stats"})
    @RangeConstraint(min = 0)
    public static double UPPER_SPEED = 0.2;

    @Config(
            key = "lower_max_health",
            comment = "The minimum health penalty for dragons with random stats.",
            category = {"base_stats", "random_stats"})
    @RangeConstraint(min = -Integer.MAX_VALUE, max = -1)
    public static int LOWER_MAX_HEALTH = -5;

    @Config(
            key = "lower_damage",
            comment = "The minimum damage penalty for dragons with random stats.",
            category = {"base_stats", "random_stats"})
    @RangeConstraint(min = -Double.MAX_VALUE, max = -1)
    public static double LOWER_DAMAGE = -2.5;

    @Config(
            key = "lower_speed",
            comment = "The minimum speed penalty for dragons with random stats.",
            category = {"base_stats", "random_stats"})
    @RangeConstraint(min = -Double.MAX_VALUE, max = 0)
    public static double LOWER_SPEED = -0.1;

    @Config(key = "whistle_cooldown", comment = "The cooldown for using the whistle ability.", category = "whistle")
    @RangeConstraint(min = 0, max = Long.MAX_VALUE)
    public static long WHISTLE_COOLDOWN_CONFIG = DragonConstants.WHISTLE_COOLDOWN;

    @Config(
            key = "whistle_check_space",
            comment = "Check if there is enough space to call the dragon before calling it.",
            category = "whistle")
    public static boolean CALL_CHECK_SPACE = true;

    @Config(key = "allow_respawn", comment = "Allow dragons to respawn after being killed.", category = "whistle")
    public static boolean ALLOW_RESPAWN = true;

    @Config(
            key = "respawn_time",
            comment = "Time in seconds for a dragon to respawn after being killed.",
            category = "whistle")
    @RangeConstraint(min = 0, max = Integer.MAX_VALUE)
    public static int RESPAWN_TIME = 60;

    @Config(
            key = "dragon_egg_spawn_chance",
            comment =
                    "Multiplier for dragon egg spawn chances in loot sources. 0 disables spawning, 1 is default rate, 2 doubles the chance.",
            category = "eggs",
            worldRestart = true)
    @RangeConstraint(min = 0.0, max = 100.0)
    public static double DRAGON_EGG_SPAWN_CHANCE = 1.0;

    @Config(
            key = "min_follow_distance",
            comment = "Minimum distance a dragon will maintain when following its owner.",
            category = "behavior")
    @RangeConstraint(min = 1, max = 16)
    public static int MIN_FOLLOW_DISTANCE = 4;

    @Config(
            key = "max_follow_distance",
            comment = "Maximum distance before a dragon will start following its owner.",
            category = "behavior")
    @RangeConstraint(min = 2, max = 64)
    public static int MAX_FOLLOW_DISTANCE = 8;

    // Initialize the config
    static {
        MOD_CONFIG_SPEC = ConfigProcessor.processConfig(ServerConfig.class);
    }
}
