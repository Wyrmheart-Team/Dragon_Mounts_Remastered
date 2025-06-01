package dmr.DragonMounts.config;

import dmr.DragonMounts.config.annotations.Config;
import dmr.DragonMounts.config.annotations.RangeConstraint;
import dmr.DragonMounts.config.annotations.SyncedConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

@OnlyIn(Dist.CLIENT)
public class ClientConfig {

    public static final ModConfigSpec MOD_CONFIG_SPEC;

    @Config(key = "camera_flight", comment = "Should the dragon be controlled by the camera during flight?")
    @SyncedConfig
    public static boolean CAMERA_FLIGHT = true;

    @Config(
            key = "alternate_dismount",
            comment =
                    "Should dismounting the dragon require double pressing the dismount button? Disabling this will not allow using sneak or the dismount button to descend.")
    @SyncedConfig
    public static boolean DOUBLE_PRESS_DISMOUNT = true;

    @Config(key = "alternate_attack_key", comment = "Should dragon attacks require holding down the dragon attack key?")
    public static boolean USE_ALTERNATE_ATTACK_KEY = false;

    @Config(
            key = "riding_camera_offset",
            comment = "The zoom offset for the riding camera. Higher values will zoom the camera out further.")
    @RangeConstraint(min = 1, max = 100)
    public static int RIDING_CAMERA_OFFSET = 10;

    @Config(key = "render_hatching_egg", comment = "Should the dragon egg render the hatching animation?")
    public static boolean RENDER_HATCHING_EGG = true;

    @Config(
            key = "colored_whistle_menu",
            comment = "Should the dragon whistle command menu display colors matching the whistle's color?")
    public static boolean COLORED_WHISTLE_MENU = true;

    // Initialize the config
    static {
        MOD_CONFIG_SPEC = ConfigProcessor.processConfig(ClientConfig.class);
    }
}
