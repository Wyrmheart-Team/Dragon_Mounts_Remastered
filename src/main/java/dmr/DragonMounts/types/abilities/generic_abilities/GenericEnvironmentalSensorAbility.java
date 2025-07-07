package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

/**
 * A generic ability that applies actions based on environmental conditions.
 * Supports time_based, weather_based, biome_based, dimension_based conditions.
 */
public class GenericEnvironmentalSensorAbility extends GenericActionAbility {
    private String conditionType = "time_based";

    // Time-based properties
    private long minTime = 0;
    private long maxTime = 24000;

    // Weather-based properties
    private boolean requiresRain = false;
    private boolean requiresThunder = false;
    private boolean requiresClear = false;

    // Biome-based properties
    private String requiredBiome = "";
    private String biomeTag = "";

    // Dimension-based properties
    private String requiredDimension = "";

    public GenericEnvironmentalSensorAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("condition_type")) {
            conditionType = (String) props.get("condition_type");
        }

        // Time-based properties
        if (props.containsKey("min_time")) {
            minTime = ((Number) props.get("min_time")).longValue();
        }
        if (props.containsKey("max_time")) {
            maxTime = ((Number) props.get("max_time")).longValue();
        }

        // Weather-based properties
        if (props.containsKey("requires_rain")) {
            requiresRain = (Boolean) props.get("requires_rain");
        }
        if (props.containsKey("requires_thunder")) {
            requiresThunder = (Boolean) props.get("requires_thunder");
        }
        if (props.containsKey("requires_clear")) {
            requiresClear = (Boolean) props.get("requires_clear");
        }

        // Biome-based properties
        if (props.containsKey("required_biome")) {
            requiredBiome = (String) props.get("required_biome");
        }
        if (props.containsKey("biome_tag")) {
            biomeTag = (String) props.get("biome_tag");
        }

        // Dimension-based properties
        if (props.containsKey("required_dimension")) {
            requiredDimension = (String) props.get("required_dimension");
        }
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        if (checkCondition(dragon)) {
            if (!isNearbyAbility()) {
                executeActions(dragon, null);
            }
        }
        super.tick(dragon);
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        if (checkCondition(dragon)) {
            executeActions(dragon, owner);
        }
    }

    private boolean checkCondition(TameableDragonEntity dragon) {
        switch (conditionType) {
            case "time_based":
                long time = dragon.level.getDayTime() % 24000;
                return time >= minTime && time <= maxTime;

            case "weather_based":
                boolean isRaining = dragon.level.isRaining();
                boolean isThundering = dragon.level.isThundering();

                if (requiresClear && (isRaining || isThundering)) return false;
                if (requiresRain && !isRaining) return false;
                if (requiresThunder && !isThundering) return false;
                return true;

            case "biome_based":
                Holder<Biome> biome = dragon.level.getBiome(dragon.blockPosition());

                if (!requiredBiome.isEmpty()) {
                    ResourceLocation biomeId = biome.unwrapKey().orElse(null).location();
                    if (biomeId == null || !biomeId.toString().equals(requiredBiome)) {
                        return false;
                    }
                }

                if (!biomeTag.isEmpty()) {
                    ResourceLocation tagId = ResourceLocation.tryParse(biomeTag);
                    if (tagId != null) {
                        var tag = dragon.level
                                .registryAccess()
                                .registryOrThrow(Registries.BIOME)
                                .getTag(TagKey.create(Registries.BIOME, tagId));

                        if (tag.isEmpty() || !tag.get().contains(biome)) {
                            return false;
                        }
                    }
                }
                return true;

            case "dimension_based":
                ResourceLocation dimensionId = dragon.level.dimension().location();
                return requiredDimension.isEmpty() || dimensionId.toString().equals(requiredDimension);

            default:
                return true;
        }
    }
}
