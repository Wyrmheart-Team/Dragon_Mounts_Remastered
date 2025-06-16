package dmr.DragonMounts.registry;

import dmr.DragonMounts.types.breath.DragonBreathType;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonVariant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.world.item.ItemStack;

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

    public static DragonBreed getDragonType(ItemStack stack) {
        var breedId = stack.get(ModComponents.DRAGON_BREED);
        return DragonBreedsRegistry.getDragonBreed(breedId);
    }

    public static DragonVariant getDragonTypeVariant(ItemStack stack) {
        var breed = getDragonType(stack);
        var variantId = stack.get(ModComponents.DRAGON_VARIANT);
        if (breed != null) {
            return breed.getVariants().stream()
                    .filter(variant -> variant.id().equals(variantId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static void setDragonType(ItemStack stack, DragonBreed type) {
        if (stack == null || type == null) return;
        stack.set(ModComponents.DRAGON_BREED, type.getId());
    }

    public static void setDragonTypeVariant(ItemStack stack, DragonBreed type, DragonVariant variant) {
        setDragonType(stack, type);
        stack.set(ModComponents.DRAGON_VARIANT, variant.id());
    }
}
