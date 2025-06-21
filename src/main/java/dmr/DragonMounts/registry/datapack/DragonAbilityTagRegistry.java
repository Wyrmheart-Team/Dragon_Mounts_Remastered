package dmr.DragonMounts.registry.datapack;

import dmr.DragonMounts.types.abilities.DragonAbilityTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class DragonAbilityTagRegistry {

    private static final HashMap<String, DragonAbilityTag> ABILITY_TAGS = new HashMap<>();

    public static void register(DragonAbilityTag abilityTag) {
        ABILITY_TAGS.put(abilityTag.getId(), abilityTag);
    }

    public static void setAbilityTags(List<DragonAbilityTag> abilityTags) {
        ABILITY_TAGS.clear();
        for (DragonAbilityTag abilityTag : abilityTags) {
            register(abilityTag);
        }
    }

    public static DragonAbilityTag getAbilityTag(ResourceLocation id) {
        return getAbilityTag(id.getPath());
    }

    public static DragonAbilityTag getAbilityTag(String name) {
        var val = ABILITY_TAGS.getOrDefault(name, null);
        return val == null ? getDefault() : val;
    }

    public static boolean hasAbilityTag(String name) {
        return ABILITY_TAGS.containsKey(name);
    }

    public static List<DragonAbilityTag> getAbilityTags() {
        return new ArrayList<>(ABILITY_TAGS.values());
    }

    public static DragonAbilityTag getFirst() {
        return getAbilityTags().stream().findFirst().orElse(new DragonAbilityTag());
    }

    public static DragonAbilityTag getDefault() {
        return hasAbilityTag("fire") ? getAbilityTag("fire") : getFirst();
    }
}
