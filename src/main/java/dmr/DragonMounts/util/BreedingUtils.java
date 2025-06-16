package dmr.DragonMounts.util;

import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.habitats.Habitat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class BreedingUtils {
    public static DragonBreed getHabitatBreedOutcome(ServerLevel level, BlockPos pos) {
        var outcomes = getHabitatBreedOutcomes(level, pos);
        var first = outcomes.stream().findFirst();
        return first.map(Entry::getValue).orElse(null);
    }

    public static List<Entry<Integer, DragonBreed>> getHabitatBreedOutcomes(ServerLevel level, BlockPos pos) {
        var outcomes = new ArrayList<Entry<Integer, DragonBreed>>();

        // Evaluate each dragon breed's habitat compatibility with the current location
        // Breeds with higher habitat points have better compatibility and are prioritized in breeding outcomes
        for (DragonBreed dragonBreed : DragonBreedsRegistry.getDragonBreeds()) {
            if (dragonBreed.getHabitats() == null || dragonBreed.getHabitats().isEmpty()) continue;

            var points = 0;
            for (Habitat habitat : dragonBreed.getHabitats()) {
                if (habitat == null) continue;

                points += Math.max(0, habitat.getHabitatPoints(level, pos));
            }

            if (points > 0) {
                outcomes.add(new SimpleEntry<>(points, dragonBreed));
            }
        }

        outcomes.sort(Comparator.comparingInt(Entry::getKey));
        Collections.reverse(outcomes);

        return outcomes;
    }
}
