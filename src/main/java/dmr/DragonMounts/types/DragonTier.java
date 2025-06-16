package dmr.DragonMounts.types;

import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.Collections;

@Getter
public enum DragonTier {
    COMMON(0, ChatFormatting.WHITE, 1.0, 2),
    UNCOMMON(1, ChatFormatting.GREEN, 0.5, 3),
    RARE(2, ChatFormatting.BLUE, 0.25, 4),
    EPIC(3, ChatFormatting.DARK_PURPLE, 0.1, 5),
    LEGENDARY(4, ChatFormatting.GOLD, 0.05, 6);

    private final int level;
    private final ChatFormatting color;
    private final double spawnChance;
    private final int maxAbilities;

    DragonTier(int level, ChatFormatting color, double spawnChance, int maxAbilities) {
        this.level = level;
        this.color = color;
        this.spawnChance = spawnChance;
        this.maxAbilities = maxAbilities;
    }

    public Component getDisplayName() {
        return Component.translatable("dmr.dragon_tier." + name().toLowerCase()).setStyle(Style.EMPTY.withColor(color));
    }

    public static DragonTier getRandomTier() {
        double random = Math.random();
        
        var tiers = Arrays.asList(values());
        Collections.reverse(tiers);
        
        for (DragonTier tier : tiers) {
            if (random <= tier.getSpawnChance()) {
                return tier;
            }
        }
        return COMMON;
    }

    public static DragonTier fromLevel(int level) {
        for (DragonTier tier : values()) {
            if (tier.getLevel() == level) {
                return tier;
            }
        }
        return COMMON;
    }
}
