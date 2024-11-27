package dmr.DragonMounts.config;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

@OnlyIn(Dist.CLIENT)
public class ClientConfig {

	public static final ModConfigSpec MOD_CONFIG_SPEC;

	public static final ModConfigSpec.BooleanValue CAMERA_FLIGHT;
	public static final ModConfigSpec.IntValue RIDING_CAMERA_OFFSET;
	public static final ModConfigSpec.BooleanValue DOUBLE_PRESS_DISMOUNT;
	public static final ModConfigSpec.BooleanValue USE_ALTERNATE_ATTACK_KEY;

	static {
		var configurator = new ModConfigSpec.Builder().comment("Client configuration settings for Dragon Mounts Reborn.");

		CAMERA_FLIGHT = configurator.comment("Should the dragon be controlled by the camera during flight?").define("camera_flight", true);

		DOUBLE_PRESS_DISMOUNT = configurator
			.comment(
				"Should dismounting the dragon require double pressing the dismount button? Disabling this will not allow using sneak or the dismount button to descend."
			)
			.define("alternate_dismount", true);

		USE_ALTERNATE_ATTACK_KEY = configurator
			.comment("Should dragon attacks require holding down the dragon attack key?")
			.define("alternate_attack_key", true);

		RIDING_CAMERA_OFFSET = configurator
			.comment("The zoom offset for the riding camera.")
			.comment("Higher values will zoom the camera out further.")
			.defineInRange("riding_camera_offset", 10, 1, 100);

		MOD_CONFIG_SPEC = configurator.build();
	}
}
