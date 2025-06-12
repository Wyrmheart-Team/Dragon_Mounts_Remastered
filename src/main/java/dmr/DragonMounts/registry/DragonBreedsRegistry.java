package dmr.DragonMounts.registry;

import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.util.BreedingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.server.level.ServerLevel;

public class DragonBreedsRegistry {

    private static final HashMap<String, DragonBreed> DRAGON_TYPES = new HashMap<>();

    public static void register(DragonBreed breed) {
        DRAGON_TYPES.put(breed.getId(), breed);
    }

    public static void setBreeds(List<DragonBreed> breeds) {
        DRAGON_TYPES.clear();
        for (DragonBreed breed : breeds) {
            register(breed);
        }
    }

    public static DragonBreed getDragonBreed(String name) {
        var val = DRAGON_TYPES.getOrDefault(name, null);
        return val == null ? getDefault() : val;
    }

    public static boolean hasDragonBreed(String name) {
        return DRAGON_TYPES.containsKey(name);
    }

    public static List<DragonBreed> getDragonBreeds() {
        return new ArrayList<>(DRAGON_TYPES.values());
    }

    public static DragonBreed getFirst() {
        return getDragonBreeds().stream().findFirst().orElse(new DragonBreed());
    }

    public static DragonBreed getDefault() {
        return hasDragonBreed("end") ? getDragonBreed("end") : getFirst();
    }

    public static ArrayList<DragonBreed> getEggOutcomes(
            TameableDragonEntity tameableDragonEntity, ServerLevel level, TameableDragonEntity mate) {
        var eggOutcomes = new ArrayList<DragonBreed>();

        eggOutcomes.addAll(getBreeds(tameableDragonEntity));
        eggOutcomes.addAll(getBreeds(mate));

        if (ServerConfig.HABITAT_OFFSPRING) {
            DragonBreed highestBreed1 =
                    BreedingUtils.getHabitatBreedOutcome(level, tameableDragonEntity.blockPosition());
            DragonBreed highestBreed2 = BreedingUtils.getHabitatBreedOutcome(level, mate.blockPosition());

            if (highestBreed1 != null) {
                if (!eggOutcomes.contains(highestBreed1)) eggOutcomes.add(highestBreed1);
            }

            if (highestBreed2 != null) {
                if (!eggOutcomes.contains(highestBreed2)) eggOutcomes.add(highestBreed2);
            }
        }
        return eggOutcomes;
    }

    public static List<DragonBreed> getBreeds(TameableDragonEntity tameableDragonEntity) {
        List<DragonBreed> breeds = new ArrayList<>();
        breeds.add(tameableDragonEntity.getBreed());
        return breeds;
    }
}
