package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A generic ability that allows dragons to interact with blocks in various ways.
 * Supports crop growth acceleration, harvesting, and block transformation.
 */
public class GenericBlockInteractionAbility extends Ability {
    private String interactionType = "growth_acceleration";
    private int range = 3;
    private int intervalTicks = 100; // 5 seconds
    private float effectChance = 0.3f;
    private boolean requiresOwnerNearby = false;
    private int ownerRange = 10;
    private String targetBlockTag = "";
    private String targetBlock = "";

    private int lastInteractionTick = 0;

    public GenericBlockInteractionAbility(String type) {
        super(type);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        Map<String, Object> props = definition.getProperties();

        if (props.containsKey("interaction_type")) {
            interactionType = (String) props.get("interaction_type");
        }

        if (props.containsKey("range")) {
            range = ((Number) props.get("range")).intValue();
        }

        if (props.containsKey("interval_ticks")) {
            intervalTicks = ((Number) props.get("interval_ticks")).intValue();
        }

        if (props.containsKey("effect_chance")) {
            effectChance = ((Number) props.get("effect_chance")).floatValue();
        }

        if (props.containsKey("requires_owner_nearby")) {
            requiresOwnerNearby = (Boolean) props.get("requires_owner_nearby");
        }

        if (props.containsKey("owner_range")) {
            ownerRange = ((Number) props.get("owner_range")).intValue();
        }

        if (props.containsKey("target_block_tag")) {
            targetBlockTag = (String) props.get("target_block_tag");
        }

        if (props.containsKey("target_block")) {
            targetBlock = (String) props.get("target_block");
        }
    }

    @Override
    public boolean isNearbyAbility() {
        return requiresOwnerNearby;
    }

    @Override
    public int getRange() {
        return ownerRange;
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        if (!requiresOwnerNearby) {
            performInteraction(dragon, null);
        }
        super.tick(dragon);
    }

    @Override
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        if (requiresOwnerNearby) {
            performInteraction(dragon, owner);
        }
    }

    private void performInteraction(TameableDragonEntity dragon, Player owner) {
        if (dragon.level.isClientSide) return;

        int currentTick = dragon.tickCount;
        if (currentTick - lastInteractionTick < intervalTicks) return;

        lastInteractionTick = currentTick;

        BlockPos dragonPos = dragon.blockPosition();
        int tier = getLevel();

        // Search for blocks in range
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (dragon.getRandom().nextFloat() > effectChance * tier) continue;

                    BlockPos pos = dragonPos.offset(x, y, z);
                    BlockState state = dragon.level.getBlockState(pos);

                    if (isValidTarget(state)) {
                        switch (interactionType) {
                            case "growth_acceleration":
                                accelerateGrowth(dragon, pos, state);
                                break;
                            case "auto_harvest":
                                autoHarvest(dragon, pos, state);
                                break;
                            case "fertilize":
                                fertilize(dragon, pos, state);
                                break;
                        }
                    }
                }
            }
        }
    }

    private boolean isValidTarget(BlockState state) {
        Block block = state.getBlock();

        if (!targetBlock.isEmpty()) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            return blockId.toString().equals(targetBlock);
        }

        if (!targetBlockTag.isEmpty()) {
            ResourceLocation tagId = ResourceLocation.tryParse(targetBlockTag);
            if (tagId != null) {
                TagKey<Block> tag = BlockTags.create(tagId);
                return state.is(tag);
            }
        }

        // Default behavior based on interaction type
        switch (interactionType) {
            case "growth_acceleration":
            case "fertilize":
                return state.getBlock() instanceof BonemealableBlock || state.is(BlockTags.CROPS);
            case "auto_harvest":
                return state.getBlock() instanceof CropBlock && ((CropBlock) state.getBlock()).isMaxAge(state);
            default:
                return false;
        }
    }

    private void accelerateGrowth(TameableDragonEntity dragon, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BonemealableBlock bonemealable && dragon.level instanceof ServerLevel level) {
            if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                bonemealable.performBonemeal(level, dragon.getRandom(), pos, state);
            }
        }
    }

    private void autoHarvest(TameableDragonEntity dragon, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            // Drop items and replant
            Block.dropResources(state, dragon.level, pos);
            dragon.level.setBlockAndUpdate(pos, crop.getStateForAge(0));
        }
    }

    private void fertilize(TameableDragonEntity dragon, BlockPos pos, BlockState state) {
        // Similar to bone meal but with particles
        if (BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), dragon.level, pos)) {
            dragon.level.levelEvent(1505, pos, 0); // Bone meal particles
        }
    }
}
