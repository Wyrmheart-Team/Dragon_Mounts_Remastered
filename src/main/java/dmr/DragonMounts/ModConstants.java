package dmr.DragonMounts;

public class ModConstants {

    public static final String VARIANT_DIVIDER = "$";

    public static class DragonConstants {

        // Defines the threshold distance as a multiple of BASE_FOLLOW_RANGE at which dragons will teleport to their
        // player instead of walking
        public static final double FOLLOW_RANGE_MULTIPLIER = 2.0;

        // Configuration values for dragon whistle sound effects
        public static final float WHISTLE_BASE_PITCH = 1.4f;
        public static final float WHISTLE_PITCH_DIVISOR = 3.0f;

        // Maximum distance at which dragons can be detected by player commands
        public static final double DRAGON_SEARCH_RADIUS = 100.0;

        // Network packet identifier for dragon follow state
        public static final int DRAGON_STATE_FOLLOW = 1;

        // Health threshold to ensure dragons have at least this much health when summoned
        public static final float MIN_DRAGON_HEALTH = 1.0f;
    }

    public static class NBTConstants {

        public static final String BREED = "breed";
        public static final String SADDLED = "saddle";
        public static final String DRAGON_UUID = "dragonUUID";
        public static final String WANDERING_POS = "wanderingPosition";
        public static final String CHEST = "chest";
        public static final String VARIANT = "variant";
        public static final String ORDERED_TO_SIT = "OrderedToSit";
        public static final String WAS_HATCHED = "wasHatched";
    }
}
