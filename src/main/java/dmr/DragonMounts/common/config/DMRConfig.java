package dmr.DragonMounts.common.config;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.concurrent.TimeUnit;

public class DMRConfig
{
	public static final ModConfigSpec CLIENT;
	public static final ModConfigSpec COMMON;
	public static final ModConfigSpec SERVER;
	
	public static final int HATCH_TIME = (int)TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES); // (10 minutes))
	public static final int GROWTH_TIME = (int)TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES); // (10 minutes))
	public static final int BASE_REPRO_LIMIT = 3;
	public static final float BASE_SIZE_MODIFIER = 1.0f;
	private static final Long WHISTLE_COOLDOWN = TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS); // 5 minutes
	
	public static final ModConfigSpec.BooleanValue CAMERA_FLIGHT;
	public static final ModConfigSpec.IntValue RIDING_CAMERA_OFFSET;
	
	public static final ModConfigSpec.BooleanValue DOUBLE_PRESS_DISMOUNT;
	public static final ModConfigSpec.BooleanValue USE_ALTERNATE_ATTACK_KEY;
	
	static //Client
	{
		var configurator = new ModConfigSpec.Builder();
		
		CAMERA_FLIGHT = configurator.comment("Should the dragon be controlled by the camera during flight?").define("camera_flight", true);
		
		DOUBLE_PRESS_DISMOUNT = configurator.comment("Should dismounting the dragon require double pressing the dismount button? Disabling this will not allow using sneak or the dismount button to descend.").define("alternate_dismount", true);
		
		USE_ALTERNATE_ATTACK_KEY = configurator.comment("Should dragon attacks require holding down the dragon attack key?").define("alternate_attack_key", true);
		
		RIDING_CAMERA_OFFSET = configurator.comment("The zoom offset for the riding camera.").comment("Higher values will zoom the camera out further.").defineInRange("riding_camera_offset", 10, 1, 100);
		
		CLIENT = configurator.build();
	}
	
	public static final ModConfigSpec.BooleanValue ALLOW_EGG_OVERRIDE;
	
	static //Common
	{
		var configurator = new ModConfigSpec.Builder();
		
		ALLOW_EGG_OVERRIDE = configurator.comment("Allow the vanilla ender egg to be interacted with? (Hatchable)", "Useful to help with mod compatability").define("allow_egg_override", true);
		
		COMMON = configurator.build();
	}
	
	public static final ModConfigSpec.LongValue WHISTLE_COOLDOWN_CONFIG;
	public static final ModConfigSpec.BooleanValue CALL_CHECK_SPACE;
	
	public static final ModConfigSpec.IntValue HATCH_TIME_CONFIG;
	public static final ModConfigSpec.IntValue GROWTH_TIME_CONFIG;
	public static final ModConfigSpec.DoubleValue SIZE_MODIFIER;
	public static final ModConfigSpec.BooleanValue REPLENISH_EGGS;
	public static final ModConfigSpec.IntValue REPRO_LIMIT;
	public static final ModConfigSpec.BooleanValue ALLOW_HYBRIDIZATION;
	public static final ModConfigSpec.BooleanValue HABITAT_OFFSPRING;
	
	public static final ModConfigSpec.DoubleValue BASE_HEALTH;
	public static final ModConfigSpec.DoubleValue BASE_DAMAGE;
	
	public static final ModConfigSpec.BooleanValue ALLOW_RESPAWN;
	public static final ModConfigSpec.IntValue RESPAWN_TIME;
	
	static //Server
	{
		var configurator = new ModConfigSpec.Builder();
		
		REPLENISH_EGGS = configurator.comment("Should Ender Dragon Eggs replenish on the exit portal after a respawned dragon is deafeated?", "Useful for multiplayer scenarios.").define("replenish_eggs", true);
		
		REPRO_LIMIT = configurator.comment("Number of times a dragon is able to breed.").defineInRange("breed_limit", BASE_REPRO_LIMIT, 0, Integer.MAX_VALUE);
		
		HATCH_TIME_CONFIG = configurator.comment("Time in seconds for a dragon egg to hatch.").defineInRange("hatch_time", HATCH_TIME, 0, Integer.MAX_VALUE);
		
		GROWTH_TIME_CONFIG = configurator.comment("Time in seconds for a dragon to grow.").defineInRange("growth_time", GROWTH_TIME, 0, Integer.MAX_VALUE);
		
		SIZE_MODIFIER = configurator.comment("Size modifier for all dragons.").defineInRange("size_modifier", BASE_SIZE_MODIFIER, 0.0, Double.MAX_VALUE);
		
		ALLOW_HYBRIDIZATION = configurator.comment("Allow hybridization between dragons.").define("allow_hybridization", true);
		
		HABITAT_OFFSPRING = configurator.comment("Offspring from breeding can turn into dragon type matching current environment.").define("habitat_offspring", true);
		
		WHISTLE_COOLDOWN_CONFIG = configurator.comment("The cooldown for using the whistle ability.").defineInRange("whistle_cooldown", WHISTLE_COOLDOWN, 0L, Long.MAX_VALUE);
		
		CALL_CHECK_SPACE = configurator.comment("Should the dragon whistle check for available space?").define("whistle_check_space", true);
		
		BASE_HEALTH = configurator.comment("Base health for all dragons.").defineInRange("base_health", DMRDragonEntity.BASE_HEALTH, 1.0, Double.MAX_VALUE);
		BASE_DAMAGE = configurator.comment("Base damage for all dragons.").defineInRange("base_damage", DMRDragonEntity.BASE_DAMAGE, 1.0, Double.MAX_VALUE);
		
		ALLOW_RESPAWN = configurator.comment("Allow dragons to respawn after death.").define("allow_respawn", true);
		
		RESPAWN_TIME = configurator.comment("Time in seconds for a dragon to respawn.").defineInRange("respawn_time", 60, 0, Integer.MAX_VALUE);
		
		SERVER = configurator.build();
	}
}
