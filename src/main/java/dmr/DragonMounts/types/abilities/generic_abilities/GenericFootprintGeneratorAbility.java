package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A generic ability that creates footprints or trails when the dragon moves.
 * Supports permanent block placement with configurable patterns.
 */
public class GenericFootprintGeneratorAbility extends Ability {
    private float footprintChance = 0.1f;
    private String placementBlock = "";
    private String replacementBlock = "";
    private boolean replaceAir = true;
    private boolean replaceWater = false;
    private boolean replaceLava = false;
    private String pattern = "single"; // single, cross, circle
    private boolean requiresAdult = true;
    private boolean requiresOnGround = true;

    public GenericFootprintGeneratorAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("footprint_chance")) {
            footprintChance = ((Number) props.get("footprint_chance")).floatValue();
        }

        if (props.containsKey("placement_block")) {
            placementBlock = (String) props.get("placement_block");
        }

        if (props.containsKey("replacement_block")) {
            replacementBlock = (String) props.get("replacement_block");
        }

        if (props.containsKey("replace_air")) {
            replaceAir = (Boolean) props.get("replace_air");
        }

        if (props.containsKey("replace_water")) {
            replaceWater = (Boolean) props.get("replace_water");
        }

        if (props.containsKey("replace_lava")) {
            replaceLava = (Boolean) props.get("replace_lava");
        }

        if (props.containsKey("pattern")) {
            pattern = (String) props.get("pattern");
        }

        if (props.containsKey("requires_adult")) {
            requiresAdult = (Boolean) props.get("requires_adult");
        }

        if (props.containsKey("requires_on_ground")) {
            requiresOnGround = (Boolean) props.get("requires_on_ground");
        }
    }

    @Override
    public boolean isFootprintAbility() {
        return true;
    }

    @Override
    public float getFootprintChance(TameableDragonEntity dragon) {
        int tier = getLevel();
        return Math.min(footprintChance * tier, 1.0f);
    }

    @Override
    public void placeFootprint(TameableDragonEntity dragon, BlockPos pos) {
        if (dragon.level.isClientSide) return;
        if (requiresAdult && !dragon.isAdult()) return;
        if (requiresOnGround && !dragon.onGround()) return;

        switch (pattern) {
            case "single":
                placeSingleFootprint(dragon, pos);
                break;
            case "cross":
                placeCrossFootprint(dragon, pos);
                break;
            case "circle":
                placeCircleFootprint(dragon, pos);
                break;
        }
    }

    private void placeSingleFootprint(TameableDragonEntity dragon, BlockPos pos) {
        placeBlockAt(dragon, pos);
    }

    private void placeCrossFootprint(TameableDragonEntity dragon, BlockPos pos) {
        placeBlockAt(dragon, pos);
        placeBlockAt(dragon, pos.north());
        placeBlockAt(dragon, pos.south());
        placeBlockAt(dragon, pos.east());
        placeBlockAt(dragon, pos.west());
    }

    private void placeCircleFootprint(TameableDragonEntity dragon, BlockPos pos) {
        int radius = Math.max(1, getLevel());
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    placeBlockAt(dragon, pos.offset(x, 0, z));
                }
            }
        }
    }

    private void placeBlockAt(TameableDragonEntity dragon, BlockPos pos) {
        BlockState currentState = dragon.level.getBlockState(pos);

        if (!canReplaceBlock(currentState)) return;

        BlockState newState = getBlockToPlace(dragon, pos, currentState);
        if (newState == null) return;

        dragon.level.setBlockAndUpdate(pos, newState);
    }

    private boolean canReplaceBlock(BlockState state) {
        if (replaceAir && state.isAir()) return true;
        if (replaceWater && state.getFluidState().isSource()) return true;
        if (replaceLava && state.getBlock().toString().contains("lava")) return true;

        if (!replacementBlock.isEmpty()) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            return blockId.toString().equals(replacementBlock);
        }

        return false;
    }

    private BlockState getBlockToPlace(TameableDragonEntity dragon, BlockPos pos, BlockState currentState) {
        if (placementBlock.isEmpty()) return null;

        ResourceLocation blockLocation = ResourceLocation.tryParse(placementBlock);
        if (blockLocation == null) return null;

        Block block = BuiltInRegistries.BLOCK.get(blockLocation);
        if (block == null) return null;

        BlockState newState = block.defaultBlockState();

        // Check if the new block can survive at this position
        if (!newState.canSurvive(dragon.level, pos)) return null;

        return newState;
    }
}
