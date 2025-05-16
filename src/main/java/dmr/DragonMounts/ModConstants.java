package dmr.DragonMounts;

public class ModConstants {

    public static final String VARIANT_DIVIDER = "$";

    public static class DragonConstants {

        // Multiplier for BASE_FOLLOW_RANGE when determining if a dragon should walk or
        // teleport to
        // player
        public static final double FOLLOW_RANGE_MULTIPLIER = 2.0;

        // Sound pitch values for dragon whistle
        public static final float WHISTLE_BASE_PITCH = 1.4f;
        public static final float WHISTLE_PITCH_DIVISOR = 3.0f;

        // Search radius for finding dragons near player
        public static final double DRAGON_SEARCH_RADIUS = 100.0;

        // Dragon state packet values
        public static final int DRAGON_STATE_FOLLOW = 1;

        // Minimum dragon health when summoned
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
