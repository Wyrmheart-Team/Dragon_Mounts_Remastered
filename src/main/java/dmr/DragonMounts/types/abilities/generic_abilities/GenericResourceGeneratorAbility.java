package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A generic ability that generates resources over time.
 * Supports item generation with configurable rates, quantities, and conditions.
 */
public class GenericResourceGeneratorAbility extends Ability {
    private final List<ResourceEntry> resources = new ArrayList<>();
    private int generationInterval = 1200; // 60 seconds in ticks
    private boolean requiresOwnerNearby = true;
    private int range = 10;
    private String condition = "none";

    private int lastGenerationTick = 0;

    public GenericResourceGeneratorAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("generation_interval")) {
            generationInterval = ((Number) props.get("generation_interval")).intValue();
        }

        if (props.containsKey("requires_owner_nearby")) {
            requiresOwnerNearby = (Boolean) props.get("requires_owner_nearby");
        }

        if (props.containsKey("range")) {
            range = ((Number) props.get("range")).intValue();
        }

        if (props.containsKey("condition")) {
            condition = (String) props.get("condition");
        }

        // Parse resources array
        if (props.containsKey("resources")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resourceList = (List<Map<String, Object>>) props.get("resources");

            for (Map<String, Object> resourceData : resourceList) {
                String itemId = (String) resourceData.get("item");
                int minCount =
                        resourceData.containsKey("min_count") ? ((Number) resourceData.get("min_count")).intValue() : 1;
                int maxCount = resourceData.containsKey("max_count")
                        ? ((Number) resourceData.get("max_count")).intValue()
                        : minCount;
                float chance =
                        resourceData.containsKey("chance") ? ((Number) resourceData.get("chance")).floatValue() : 1.0f;

                ResourceLocation itemLocation = ResourceLocation.tryParse(itemId);
                if (itemLocation != null) {
                    Item item = BuiltInRegistries.ITEM.get(itemLocation);
                    if (item != null) {
                        resources.add(new ResourceEntry(item, minCount, maxCount, chance));
                    }
                }
            }
        }
    }

    @Override
    public boolean isNearbyAbility() {
        return requiresOwnerNearby;
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        if (!requiresOwnerNearby) {
            checkAndGenerate(dragon, null);
        }
        super.tick(dragon);
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        if (requiresOwnerNearby) {
            checkAndGenerate(dragon, owner);
        }
    }

    private void checkAndGenerate(TameableDragonEntity dragon, Player owner) {
        if (dragon.level.isClientSide || resources.isEmpty()) return;

        int currentTick = dragon.tickCount;
        if (currentTick - lastGenerationTick < generationInterval) return;

        if (!checkCondition(dragon)) return;

        lastGenerationTick = currentTick;

        int tier = getLevel();
        for (ResourceEntry resource : resources) {
            if (dragon.getRandom().nextFloat() <= resource.chance * tier) {
                int count = resource.minCount;
                if (resource.maxCount > resource.minCount) {
                    count += dragon.getRandom().nextInt(resource.maxCount - resource.minCount + 1);
                }

                ItemStack stack = new ItemStack(resource.item, Math.min(count * tier, 64));

                // Try to add to dragon inventory first, then drop if full
                if (!dragon.getInventory().addItem(stack).isEmpty()) {
                    dragon.spawnAtLocation(stack);
                }
            }
        }
    }

    private boolean checkCondition(TameableDragonEntity dragon) {
        return switch (condition) {
            case "in_water" -> dragon.isInWater();
            case "on_ground" -> dragon.onGround();
            case "flying" -> !dragon.onGround() && !dragon.isInWater();
            case "day" -> dragon.level.isDay();
            case "night" -> !dragon.level.isDay();
            default -> true;
        };
    }

    private static class ResourceEntry {
        final Item item;
        final int minCount;
        final int maxCount;
        final float chance;

        ResourceEntry(Item item, int minCount, int maxCount, float chance) {
            this.item = item;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.chance = chance;
        }
    }
}
