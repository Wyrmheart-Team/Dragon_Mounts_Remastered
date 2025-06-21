package dmr.DragonMounts.registry.datapack;

import dmr.DragonMounts.types.breath.DragonBreathType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DragonBreathRegistry {

    private static final HashMap<String, DragonBreathType> BREATH_TYPES = new HashMap<>();

    public static void register(DragonBreathType breathType) {
        BREATH_TYPES.put(breathType.getId(), breathType);
    }

    public static void setBreathTypes(List<DragonBreathType> breathTypes) {
        BREATH_TYPES.clear();
        for (DragonBreathType breathType : breathTypes) {
            register(breathType);
        }
    }

    public static DragonBreathType getBreathType(String name) {
        var val = BREATH_TYPES.getOrDefault(name, null);
        return val == null ? getDefault() : val;
    }

    public static boolean hasBreathType(String name) {
        return BREATH_TYPES.containsKey(name);
    }

    public static List<DragonBreathType> getBreathTypes() {
        return new ArrayList<>(BREATH_TYPES.values());
    }

    public static DragonBreathType getFirst() {
        return getBreathTypes().stream().findFirst().orElse(new DragonBreathType());
    }

    public static DragonBreathType getDefault() {
        return hasBreathType("fire") ? getBreathType("fire") : getFirst();
    }
}
