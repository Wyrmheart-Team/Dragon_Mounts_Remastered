package dmr.DragonMounts.config;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.concurrent.TimeUnit;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {

	public static final ModConfigSpec MOD_CONFIG_SPEC;

	public static final int HATCH_TIME = (int) TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES); // (10 minutes))
	public static final int GROWTH_TIME = (int) TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES); // (10 minutes))
	public static final float BASE_SIZE_MODIFIER = 1.0f;

	public static final ModConfigSpec.BooleanValue ALLOW_EGG_OVERRIDE;
	public static final ModConfigSpec.LongValue WHISTLE_COOLDOWN_CONFIG;
	public static final ModConfigSpec.BooleanValue CALL_CHECK_SPACE;
	public static final ModConfigSpec.IntValue HATCH_TIME_CONFIG;
	public static final ModConfigSpec.IntValue GROWTH_TIME_CONFIG;
	public static final ModConfigSpec.DoubleValue SIZE_MODIFIER;
	public static final ModConfigSpec.BooleanValue REPLENISH_EGGS;
	public static final ModConfigSpec.BooleanValue ALLOW_HYBRIDIZATION;
	public static final ModConfigSpec.BooleanValue HABITAT_OFFSPRING;
	public static final ModConfigSpec.DoubleValue BASE_HEALTH;
	public static final ModConfigSpec.DoubleValue BASE_DAMAGE;
	public static final ModConfigSpec.BooleanValue ALLOW_RESPAWN;
	public static final ModConfigSpec.IntValue RESPAWN_TIME;
	public static final ModConfigSpec.IntValue DRAGON_HISTORY_SIZE;

	public static final ModConfigSpec.BooleanValue ENABLE_BLANK_EGG;

	private static final Long WHISTLE_COOLDOWN = TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS); // 5 minutes

	public static final ModConfigSpec.BooleanValue ENABLE_DRAGON_BREATH;

	static {
		var configurator = new ModConfigSpec.Builder();

		ALLOW_EGG_OVERRIDE = configurator
			.comment("Allow the vanilla ender egg to be interacted with? (Hatchable)", "Useful to help with mod compatability")
			.translation("dmr.config.server.allow_egg_override")
			.define("allow_egg_override", true);

		REPLENISH_EGGS = configurator
			.comment(
				"Should Ender Dragon Eggs replenish on the exit portal after a respawned dragon is deafeated?",
				"Useful for multiplayer scenarios."
			)
			.translation("dmr.config.server.replenish_eggs")
			.define("replenish_eggs", true);

		ENABLE_BLANK_EGG = configurator
			.comment("Enable blank dragon eggs which changes based on the environment.")
			.translation("dmr.config.server.enable_blank_egg")
			.define("enable_blank_egg", false);

		HATCH_TIME_CONFIG = configurator
			.comment("Time in seconds for a dragon egg to hatch.")
			.translation("dmr.config.server.hatch_time")
			.defineInRange("hatch_time", HATCH_TIME, 0, Integer.MAX_VALUE);

		GROWTH_TIME_CONFIG = configurator
			.comment("Time in seconds for a dragon to grow.")
			.translation("dmr.config.server.growth_time")
			.defineInRange("growth_time", GROWTH_TIME, 0, Integer.MAX_VALUE);

		SIZE_MODIFIER = configurator
			.comment("Size modifier for all dragons.")
			.translation("dmr.config.server.size_modifier")
			.defineInRange("size_modifier", BASE_SIZE_MODIFIER, 0.0, Double.MAX_VALUE);

		ALLOW_HYBRIDIZATION = configurator
			.comment("Allow hybridization between dragons.")
			.translation("dmr.config.server.allow_hybridization")
			.define("allow_hybridization", true);

		HABITAT_OFFSPRING = configurator
			.comment("Offspring from breeding can turn into dragon type matching current environment.")
			.translation("dmr.config.server.habitat_offspring")
			.define("habitat_offspring", true);

		WHISTLE_COOLDOWN_CONFIG = configurator
			.comment("The cooldown for using the whistle ability.")
			.translation("dmr.config.server.whistle_cooldown")
			.defineInRange("whistle_cooldown", WHISTLE_COOLDOWN, 0L, Long.MAX_VALUE);

		CALL_CHECK_SPACE = configurator
			.comment("Should the dragon whistle check for available space?")
			.translation("dmr.config.server.whistle_check_space")
			.define("whistle_check_space", true);

		BASE_HEALTH = configurator
			.comment("Base health for all dragons.")
			.translation("dmr.config.server.base_health")
			.defineInRange("base_health", DMRDragonEntity.BASE_HEALTH, 1.0, Double.MAX_VALUE);

		BASE_DAMAGE = configurator
			.comment("Base damage for all dragons.")
			.translation("dmr.config.server.base_damage")
			.defineInRange("base_damage", DMRDragonEntity.BASE_DAMAGE, 1.0, Double.MAX_VALUE);

		ALLOW_RESPAWN = configurator
			.comment("Allow dragons to respawn after death.")
			.translation("dmr.config.server.allow_respawn")
			.define("allow_respawn", true);

		RESPAWN_TIME = configurator
			.comment("Time in seconds for a dragon to respawn.")
			.translation("dmr.config.server.respawn_time")
			.defineInRange("respawn_time", 60, 0, Integer.MAX_VALUE);

		DRAGON_HISTORY_SIZE = configurator
			.comment(
				"The maximum number of dragons to keep track of in the dragon history. This for being able to recall missing dragons through commands. Bigger numbers could increase world save size."
			)
			.translation("dmr.config.server.dragon_history_size")
			.defineInRange("dragon_history_size", 20, 1, Integer.MAX_VALUE);

		ENABLE_DRAGON_BREATH = configurator
			.comment(
				"Enable dragon breath attacks, this is still highly WIP and is not representative of the final version. Issues and bug reports will not be accepted for this feature in the current state."
			)
			.translation("dmr.config.server.enable_dragon_breath")
			.define("enable_dragon_breath", false);

		MOD_CONFIG_SPEC = configurator.build();
	}
}
