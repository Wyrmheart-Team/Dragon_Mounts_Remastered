package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed.Variant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/**
 * Abstract class that provides structure for dragon breed functionality.
 * This is the first layer in the dragon entity hierarchy.
 */
abstract class DragonBreedComponent extends DragonBreathComponent {

    protected static final EntityDataAccessor<String> breedDataAccessor =
            SynchedEntityData.defineId(DragonBreedComponent.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<String> origBreedDataAccessor =
            SynchedEntityData.defineId(DragonBreedComponent.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<String> variantDataAccessor =
            SynchedEntityData.defineId(DragonBreedComponent.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Boolean> wasHatchedDataAccessor =
            SynchedEntityData.defineId(DragonBreedComponent.class, EntityDataSerializers.BOOLEAN);

    protected DragonBreedComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(breedDataAccessor, "");
        builder.define(origBreedDataAccessor, "");
        builder.define(variantDataAccessor, "");
        builder.define(wasHatchedDataAccessor, false);
    }

    /**
     * Updates properties based on the dragon's age.
     * This method should be implemented by subclasses.
     */
    public abstract void updateAgeProperties();

    /**
     * Checks if the dragon was hatched from an egg.
     */
    public boolean wasHatched() {
        return entityData.get(wasHatchedDataAccessor);
    }

    /**
     * Sets whether the dragon was hatched from an egg.
     */
    public void setHatched(boolean wasHatched) {
        entityData.set(wasHatchedDataAccessor, wasHatched);
    }

    private boolean breedIsSet = false;

    protected IDragonBreed breed;

    /** Gets the current breed of the dragon. */
    public IDragonBreed getBreed() {
        var origBreed = getEntityData().get(origBreedDataAccessor);

        // If there is a original breed stored, try to fetch the breed from that
        if (!origBreed.isBlank() && DragonBreedsRegistry.hasDragonBreed(origBreed)) {
            return DragonBreedsRegistry.getDragonBreed(origBreed);
        }

        return DragonBreedsRegistry.getDragonBreed(getBreedId());
    }

    /** Sets the breed of the dragon. */
    public void setBreed(IDragonBreed dragonBreed) {
        if (breed != dragonBreed || !breedIsSet) { // prevent loops, unnecessary work, etc.
            if (dragonBreed == null
                    || dragonBreed.getId() == null
                    || dragonBreed.getId().isBlank()) {
                return;
            }

            if (dragonBreed == DragonBreedsRegistry.getDefault() && breed != dragonBreed) {
                return;
            }

            breedIsSet = true;

            if (breed != null) breed.close(getDragon());
            breed = dragonBreed;
            dragonBreed.initialize(getDragon());
            getEntityData().set(breedDataAccessor, dragonBreed.getId());

            if (getEntityData().get(origBreedDataAccessor).isBlank()) {
                getEntityData().set(origBreedDataAccessor, dragonBreed.getId());
            }
        }
    }

    /** Gets the breed ID as a string. */
    public String getBreedId() {
        return getEntityData().get(breedDataAccessor);
    }

    /** Gets the current variant of the dragon. */
    public Variant getVariant() {
        var id = getVariantId();
        return getBreed().getVariants().stream()
                .filter(v -> v.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    /** Sets the variant of the dragon. */
    public void setVariant(String variant) {
        getEntityData().set(variantDataAccessor, variant != null ? variant : "");
    }

    /** Gets the variant ID as a string. */
    public String getVariantId() {
        return getEntityData().get(variantDataAccessor);
    }

    /** Checks if the dragon has a variant. */
    public boolean hasVariant() {
        return (!getVariantId().isBlank()
                && getBreed().getVariants().stream().anyMatch(v -> v.id().equals(getVariantId())));
    }

    public void tick() {
        if (tickCount % 20 == 0) getBreed().tick(getDragon());
    }

    public void baseTick() {
        super.baseTick();

        if (!breedIsSet && getBreed() != null) {
            if (getBreed() != null) getBreed().close(getDragon());
            setBreed(getBreed());

            if (getBreed() != null) {
                getBreed().initialize(getDragon());
                breedIsSet = true;
            }
        }
    }

    /** Saves breed-related data to NBT. */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        if (getBreed() != null && getBreed().getId() != null) {
            compound.putString(NBTConstants.BREED, getBreed().getId());
        }
        if (getEntityData().get(origBreedDataAccessor) != null) {
            compound.putString("orig_" + NBTConstants.BREED, getEntityData().get(origBreedDataAccessor));
        }
        if (getEntityData().get(variantDataAccessor) != null) {
            compound.putString(NBTConstants.VARIANT, getEntityData().get(variantDataAccessor));
        }
        compound.putBoolean("breedIsSet", breedIsSet);
    }

    /** Loads breed-related data from NBT. */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("orig_" + NBTConstants.BREED)) {
            getEntityData().set(origBreedDataAccessor, compound.getString("orig_" + NBTConstants.BREED));
        }

        if (compound.contains(NBTConstants.BREED)) {
            var breedKey = compound.getString(NBTConstants.BREED);
            var breed = DragonBreedsRegistry.getDragonBreed(breedKey);

            setBreed(breed);
        }

        if (compound.contains(NBTConstants.VARIANT)) {
            getEntityData().set(variantDataAccessor, compound.getString(NBTConstants.VARIANT));
        }

        if (compound.contains("breedIsSet")) {
            breedIsSet = compound.getBoolean("breedIsSet");
        }
    }

    /**
     * Handles synced data updates for breed-related data.
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (breedDataAccessor.equals(data)) {
            var breedId = getEntityData().get(breedDataAccessor);
            var dragonBreed = DragonBreedsRegistry.getDragonBreed(breedId);

            setBreed(dragonBreed);
            updateAgeProperties();
        }
    }
}
