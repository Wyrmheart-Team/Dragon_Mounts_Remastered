package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.server.entity.DragonAgroState;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/**
 * Abstract class that implements core dragon functionality.
 * This is the final layer in the abstract dragon entity hierarchy.
 */
public abstract class AbstractDragonEntity extends DragonAudioComponent {

    protected static final EntityDataAccessor<String> uuidDataAccessor =
            SynchedEntityData.defineId(AbstractDragonEntity.class, EntityDataSerializers.STRING);

    @Setter
    @Getter
    protected DragonAgroState agroState = DragonAgroState.NEUTRAL;

    protected AbstractDragonEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        setDragonUUID(UUID.randomUUID());
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);

        if (!level.isClientSide) {
            getInventory().removeListener(this);
        }
    }

    public boolean isServer() {
        return !level.isClientSide;
    }

    @Override
    public double getTick(Object o) {
        return tickCount;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public void ageUp(int p_146741_, boolean p_146742_) {
        super.ageUp(p_146741_, p_146742_);
        updateAgeProperties();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putString("agroState", agroState.name());

        if (getDragonUUID() != null) {
            compound.putString(NBTConstants.DRAGON_UUID, getDragonUUID().toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("agroState")) {
            agroState = DragonAgroState.valueOf(compound.getString("agroState"));
        }

        if (compound.contains(NBTConstants.DRAGON_UUID)) {
            setDragonUUID(UUID.fromString(compound.getString(NBTConstants.DRAGON_UUID)));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(uuidDataAccessor, "");
    }

    /**
     * Gets the dragon's UUID.
     */
    public UUID getDragonUUID() {
        String id = entityData.get(uuidDataAccessor);
        return !id.isBlank() ? UUID.fromString(id) : null;
    }

    /**
     * Sets the dragon's UUID.
     */
    public void setDragonUUID(UUID uuid) {
        entityData.set(uuidDataAccessor, uuid.toString());
    }
}
