package dmr.DragonMounts.server.entity;

import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DragonConstants {
    public Long HATCH_TIME = TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES); // (10 minutes))
    public Long GROWTH_TIME = TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES); // (10 minutes))
    public Long WHISTLE_COOLDOWN = TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS); // 5 minutes

    public double BASE_SPEED_GROUND = 0.3;
    public double BASE_SPEED_WATER = 0.3;
    public double BASE_SPEED_FLYING = 0.2;
    public double BASE_DAMAGE = 8;
    public double BASE_HEALTH = 60;
    public double BASE_FOLLOW_RANGE = 32;
    public int BASE_KB_RESISTANCE = 1;
    public float BASE_WIDTH = 2.75f;
    public float BASE_HEIGHT = 2.75f;
}
