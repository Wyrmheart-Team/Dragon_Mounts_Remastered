package dmr.DragonMounts.types.abilities;

/**
 * Enum representing different event types that can trigger abilities.
 */
public enum EventType {
    ON_ATTACK("on_attack"),
    ON_DAMAGE("on_damage"),
    ON_MOUNT("on_mount"),
    ON_DISMOUNT("on_dismount"),
    ON_KILL("on_kill"),
    ON_HEAL("on_heal"),
    ON_BREATH("on_breath"),
    ON_LAND("on_land"),
    ON_TAKEOFF("on_takeoff");

    private final String jsonKey;

    EventType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getJsonKey() {
        return jsonKey;
    }

    public static EventType fromJsonKey(String key) {
        for (EventType type : values()) {
            if (type.jsonKey.equals(key)) {
                return type;
            }
        }
        return null;
    }
}
