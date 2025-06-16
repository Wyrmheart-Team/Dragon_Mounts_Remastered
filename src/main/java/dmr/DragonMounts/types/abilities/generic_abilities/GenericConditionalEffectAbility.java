package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.EffectData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

/**
 * A generic ability that applies status effects to the dragon and/or its owner
 * when a specific condition is met. Supports multiple effects and tier-based scaling.
 */
public class GenericConditionalEffectAbility extends Ability {
    private final List<EffectData> effects = new ArrayList<>();
    private int range = 10;
    private boolean applyToDragon = true;
    private boolean applyToOwner = true;
    private String conditionType = "none";
    private int natureCheckRadius = 2;

    public GenericConditionalEffectAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("range")) {
            range = ((Number) props.get("range")).intValue();
        }
        if (props.containsKey("apply_to_dragon")) {
            applyToDragon = (Boolean) props.get("apply_to_dragon");
        }
        if (props.containsKey("apply_to_owner")) {
            applyToOwner = (Boolean) props.get("apply_to_owner");
        }
        if (props.containsKey("condition_type")) {
            conditionType = (String) props.get("condition_type");
        }
        if (props.containsKey("nature_check_radius")) {
            natureCheckRadius = ((Number) props.get("nature_check_radius")).intValue();
        }

        // Get effects from properties using the factory method
        effects.addAll(EffectData.createFromProperties(props));
    }

    @Override
    public boolean isNearbyAbility() {
        return true;
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        if (effects.isEmpty() || dragon.level.isClientSide) {
            return;
        }

        if (checkCondition(dragon, owner)) {
            int tier = getLevel();
            for (EffectData effectData : effects) {
                effectData.apply(dragon, owner, applyToDragon, applyToOwner, tier);
            }
        }
    }

    private boolean checkCondition(TameableDragonEntity dragon, Player owner) {
        return switch (conditionType) {
            case "in_water" -> dragon.isInWater();
            case "near_nature" -> checkNearNature(dragon);
            default -> true;
        };
    }

    private boolean checkNearNature(TameableDragonEntity dragon) {
        var level = dragon.level;
        var basePos = dragon.blockPosition();
        var blocks = BlockPos.betweenClosedStream(
                        basePos.offset(natureCheckRadius, natureCheckRadius, natureCheckRadius),
                        basePos.offset(-natureCheckRadius, -natureCheckRadius, -natureCheckRadius))
                .map(level::getBlockState)
                .filter(state ->
                        state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS));

        return blocks.findAny().isPresent();
    }
}
