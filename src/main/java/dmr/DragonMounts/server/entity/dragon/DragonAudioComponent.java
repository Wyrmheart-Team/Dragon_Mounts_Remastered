package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Abstract class that implements dragon sound functionality.
 * This extends the dragon entity hierarchy with sound capabilities.
 */
abstract class DragonAudioComponent extends DragonMountingComponent {

    protected DragonAudioComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    /** Gets the ambient sound for the dragon. */
    public SoundEvent getAmbientSound() {
        return getBreed().getAmbientSound() != null
                ? getBreed().getAmbientSound()
                : ModSounds.DRAGON_AMBIENT_SOUND.get();
    }
    /**
     * Gets the hurt sound for the dragon.
     */
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    /** Gets the sound for when the dragon dies. */
    public SoundEvent getDeathSound() {
        return ModSounds.DRAGON_DEATH_SOUND.get();
    }
    /**
     * Gets the eating sound for the dragon.
     */
    @Override
    public SoundEvent getEatingSound(ItemStack itemStackIn) {
        return SoundEvents.GENERIC_EAT;
    }

    /** Gets the sound for when the dragon attacks. */
    public SoundEvent getAttackSound() {
        return SoundEvents.GENERIC_EAT;
    }

    /** Gets the sound for the dragon's footsteps. */
    public SoundEvent getStepSound() {
        return ModSounds.DRAGON_STEP_SOUND.get();
    }

    /** Gets the sound for the dragon's wings. */
    public SoundEvent getWingsSound() {
        return SoundEvents.ENDER_DRAGON_FLAP;
    }

    /**
     * Plays the step sound for the dragon.
     */
    @Override
    protected void playStepSound(BlockPos entityPos, BlockState state) {
        if (isInWater()) return;

        if (isHatchling()) {
            super.playStepSound(entityPos, state);
            return;
        }

        playSound(getStepSound(), 0.15F, 1.0F);
    }

    /** Plays the flap sound when the dragon flaps its wings. */
    public void onFlap() {
        if (level().isClientSide && !isSilent()) {
            level().playLocalSound(
                            getX(),
                            getY(),
                            getZ(),
                            getWingsSound(),
                            getSoundSource(),
                            2.0F,
                            0.8F + getRandom().nextFloat() * 0.3F,
                            false);
        }
    }
}
