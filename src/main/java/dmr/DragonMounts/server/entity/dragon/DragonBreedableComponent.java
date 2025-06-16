package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.network.packets.DragonAgeSyncPacket;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModBlocks;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.server.blocks.DMREggBlock;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract class that implements dragon breeding functionality.
 * This extends the dragon entity hierarchy with breeding capabilities.
 */
abstract class DragonBreedableComponent extends DragonBreedComponent {

    protected DragonBreedableComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    /** Checks if the dragon can mate with another animal. */
    public boolean canMate(Animal mate) {
        if (mate == this) {
            return false;
        } else if (!(mate instanceof TameableDragonEntity)) {
            return false;
        } else if (!canReproduce()) return false;

        TameableDragonEntity dragonMate = (TameableDragonEntity) mate;

        if (!dragonMate.isTame()) {
            return false;
        } else if (!dragonMate.canReproduce()) {
            return false;
        } else {
            return isInLove() && dragonMate.isInLove();
        }
    }

    /** Spawns a child from breeding with another animal. */
    public void spawnChildFromBreeding(ServerLevel level, Animal animal) {
        if (!(animal instanceof TameableDragonEntity mate)) return;

        // pick a breed to inherit from, and place hatching.
        var state = ModBlocks.DRAGON_EGG_BLOCK.get().defaultBlockState().setValue(DMREggBlock.HATCHING, true);
        var eggOutcomes = DragonBreedsRegistry.getEggOutcomes(getDragon(), level, mate);

        // Pick a random breed from the list to use as the offspring
        var offSpringBreed = eggOutcomes.get(getRandom().nextInt(eggOutcomes.size()));
        var variant = !offSpringBreed.getVariants().isEmpty()
                ? offSpringBreed
                        .getVariants()
                        .get(getRandom().nextInt(offSpringBreed.getVariants().size()))
                : null;
        var tag = new CompoundTag();

        var tempEntity = ModEntities.DRAGON_ENTITY.get().create(level);
        tempEntity.setBreed(offSpringBreed);
        tempEntity.setVariant(variant != null ? variant.id() : null);
        tempEntity.finalizeDragon(getDragon(), mate);
        tempEntity.addAdditionalSaveData(tag);

        level.setBlock(blockPosition(), state, Block.UPDATE_ALL);
        var egg = (DMREggBlockEntity) level.getBlockEntity(blockPosition());

        egg.setBreed(offSpringBreed);
        egg.setVariantId(variant != null ? variant.id() : null);
        egg.setHatchTime(offSpringBreed.getHatchTime());
        egg.setDragonOutcomeTag(tag);
        getDragon().updateOwnerData();
    }

    /** Gets the offspring when breeding with another mob. */
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        var offspring = ModEntities.DRAGON_ENTITY.get().create(level);
        offspring.setBreed(getBreed());
        return offspring;
    }

    /** Checks if the dragon can reproduce. */
    public boolean canReproduce() {
        return isTame();
    }

    /** Checks if an item is a food item for the dragon. */
    public boolean isFoodItem(ItemStack stack) {
        var food = stack.getItem().getFoodProperties(stack, this);
        return food != null && stack.is(ItemTags.MEAT);
    }

    /** Sets the age of the dragon. */
    public void setAge(int pAge) {
        super.setAge(pAge);
        // Don't call dragon.setAge(pAge) here to avoid stack overflow
        // The TameableDragonEntity.setAge already calls super.setAge(pAge)
        if (!level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(this, new DragonAgeSyncPacket(getId(), pAge));
        }
        updateAgeProperties();
    }

    /** Updates the age-related properties of the dragon. */
    public void updateAgeProperties() {
        refreshDimensions();
        getDragon().updateAgeAttributes();
    }

    /**
     * Gets the age progress of the dragon (0.0 to 1.0).
     */
    public float getAgeProgress() {
        float growth = -(getDragon().getBreed().getGrowthTime() * 20);
        float min = Math.min(getAge(), 0) * 20;
        float ageProgress = 1 - (min / growth);
        return Mth.clamp(ageProgress, 0, 1);
    }

    public boolean isAdult() {
        return getAgeProgress() >= 1f;
    }

    /**
     * Checks if the dragon is a baby.
     */
    @Override
    public boolean isBaby() {
        return !isAdult();
    }

    /**
     * Checks if the dragon is a juvenile.
     */
    public boolean isJuvenile() {
        return getAgeProgress() >= 0.5f && getAgeProgress() < 1f;
    }

    public boolean isHatchling() {
        return getAgeProgress() < 0.5f;
    }

    /**
     * Sets whether the dragon is a baby.
     */
    @Override
    public void setBaby(boolean baby) {
        setAge(baby ? -getDragon().getBreed().getGrowthTime() : 0);
        updateAgeProperties();
    }

    /** Gets the scale of the dragon based on its age and breed. */
    public float getScale() {
        var scale = getBreed() != null ? getBreed().getSizeModifier() : 1;
        return scale * (isBaby() ? 0.5f : 1f);
    }

    /** Checks if an item is food for the dragon. */
    public boolean isFood(ItemStack stack) {
        var list = getBreed().getBreedingItems();
        return (!stack.isEmpty()
                && (list != null && !list.isEmpty() ? list.contains(stack.getItem()) : stack.is(ItemTags.FISHES)));
    }

    @Override
    public void setInLove(@Nullable Player player) {
        super.setInLove(player);
        getDragon().stopSitting();
        getDragon().setWanderTarget(Optional.of(GlobalPos.of(level.dimension(), blockPosition())));
    }
}
