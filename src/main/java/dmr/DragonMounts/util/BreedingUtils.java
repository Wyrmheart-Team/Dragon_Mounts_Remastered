package dmr.DragonMounts.util;

import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.habitats.Habitat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class BreedingUtils {
    static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static IDragonBreed getHabitatBreedOutcome(ServerLevel level, BlockPos pos) {
        var outcomes = getHabitatBreedOutcomes(level, pos);
        var first = outcomes.stream().findFirst();
        return first.map(Entry::getValue).orElse(null);
    }

    public static List<Entry<Integer, IDragonBreed>> getHabitatBreedOutcomes(ServerLevel level, BlockPos pos) {
        var outcomes = new ArrayList<Entry<Integer, IDragonBreed>>();

        // Add the highest habitat point breed to the list of possible outcomes
        for (IDragonBreed dragonBreed : DragonBreedsRegistry.getDragonBreeds()) {
            if (dragonBreed.isHybrid()) continue;
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

    @NotNull public static String generateCustomName(TameableDragonEntity mate1, Animal animal) {
        String p1Name = mate1.getCustomName().getString();
        String p2Name = animal.getCustomName().getString();
        String babyName;

        if (p1Name.contains(" ") || p2Name.contains(" ")) {
            // combine two words with space
            // "Tempor Invidunt Dolore" + "Magna"
            // = "Tempor Magna" or "Magna Tempor"
            String[] p1Names = p1Name.split(" ");
            String[] p2Names = p2Name.split(" ");

            p1Name = StringUtils.capitalize(p1Names[mate1.getRandom().nextInt(p1Names.length)]);
            p2Name = StringUtils.capitalize(p2Names[mate1.getRandom().nextInt(p2Names.length)]);

            babyName = mate1.getRandom().nextBoolean() ? p1Name + " " + p2Name : p2Name + " " + p1Name;
        } else {
            // scramble two words
            // "Eirmod" + "Voluptua"
            // = "Eirvolu" or "Volueir" or "Modptua" or "Ptuamod" or ...
            if (mate1.getRandom().nextBoolean()) {
                p1Name = p1Name.substring(0, (p1Name.length() - 1) / 2);
            } else {
                p1Name = p1Name.substring((p1Name.length() - 1) / 2);
            }

            if (mate1.getRandom().nextBoolean()) {
                p2Name = p2Name.substring(0, (p2Name.length() - 1) / 2);
            } else {
                p2Name = p2Name.substring((p2Name.length() - 1) / 2);
            }

            p2Name = StringUtils.capitalize(p2Name);

            babyName = mate1.getRandom().nextBoolean() ? p1Name + p2Name : p2Name + p1Name;
        }
        return babyName;
    }
}
