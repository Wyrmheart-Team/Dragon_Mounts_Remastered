package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FloralTrailAbility extends Ability {
    private int grassLightThreshold = 4;

    public FloralTrailAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        if (definition.getProperties().containsKey("grass_light_threshold")) {
            this.grassLightThreshold = ((Number) definition.getProperties().get("grass_light_threshold")).intValue();
        }
    }

    @Override
    public boolean isFootprintAbility() {
        return true;
    }

    @Override
    public void placeFootprint(TameableDragonEntity dragon, BlockPos pos) {
        var level = dragon.level;
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);

        if (steppingOn.is(Blocks.DIRT) && level.getLightEmission(pos) >= grassLightThreshold) {
            level.setBlockAndUpdate(groundPos, Blocks.GRASS_BLOCK.defaultBlockState());
            return;
        }
        if (!level.getBlockState(pos).isAir()) {
            return; // place only on empty space
        }

        BlockState placing = null;

        if (steppingOn.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            placing =
                    (level.getRandom().nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM).defaultBlockState();
        } else if (steppingOn.is(BlockTags.DIRT)) { // different from the actual dirt block. Could be grass or moss
            placing = level.registryAccess()
                    .registryOrThrow(Registries.BLOCK)
                    .getTag(BlockTags.SMALL_FLOWERS)
                    .flatMap(tag -> tag.getRandomElement(dragon.getRandom()))
                    .map(Holder::value)
                    .filter(b -> b != Blocks.WITHER_ROSE)
                    .orElse(Blocks.DANDELION)
                    .defaultBlockState();
        }

        if (placing != null && placing.canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, placing);
            ((ServerLevel) level)
                    .sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            dragon.getRandom().nextInt(4) + 2,
                            0.5,
                            0.5,
                            0.5,
                            1);
        }
    }
}
