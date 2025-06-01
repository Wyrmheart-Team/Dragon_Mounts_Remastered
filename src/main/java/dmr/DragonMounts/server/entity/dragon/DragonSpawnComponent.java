package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed.Variant;
import dmr.DragonMounts.util.BreedingUtils;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

abstract class DragonSpawnComponent extends DragonAudioComponent {
    protected DragonSpawnComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Getter
    @Setter
    private UUID spawnGroupId;

    public boolean isNaturalSpawn() {
        return spawnGroupId != null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        if (getSpawnGroupId() != null) {
            compound.putUUID("spawnGroupId", getSpawnGroupId());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("spawnGroupId")) {
            setSpawnGroupId(compound.getUUID("spawnGroupId"));
        }
    }

    public static boolean checkDragonSpawnRules(
            EntityType<TameableDragonEntity> entityType,
            ServerLevelAccessor accessor,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        if (random.nextFloat() > 0.05f) return false;

        var level = accessor.getLevel();
        var outcomes = BreedingUtils.getHabitatBreedOutcomes(level, pos);
        return !outcomes.isEmpty() && outcomes.stream().anyMatch(s -> s.getKey() > 3);
    }

    public static class DragonGroupData extends AgeableMob.AgeableMobGroupData {
        public final IDragonBreed breed;
        public final Variant variant;
        public UUID groupId;

        public DragonGroupData(IDragonBreed breed, Variant variant) {
            super(true);
            this.breed = breed;
            this.variant = variant;
            groupId = UUID.randomUUID();
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor accessor,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData) {
        var level = accessor.getLevel();

        if (spawnGroupData instanceof DragonGroupData data) {
            setBreed(data.breed);
            setSpawnGroupId(data.groupId);
            setBaby(true); // This means its the second or more dragon

            if (!breed.getVariants().isEmpty() && random.nextFloat() < 0.2f) {
                var variant = breed.getVariants()
                        .get(random.nextInt(breed.getVariants().size()));
                setVariant(variant.id());
            }

        } else {
            var outcomes = BreedingUtils.getHabitatBreedOutcomes(level, blockPosition());

            if (!outcomes.isEmpty()) {
                var highestOutcome = outcomes.stream().max(Entry.comparingByKey());
                if (highestOutcome.isPresent()) {
                    // Get all breeds with the same value
                    var breeds = outcomes.stream()
                            .filter(s -> Objects.equals(
                                    s.getKey(), highestOutcome.get().getKey()))
                            .toList();
                    // Get random breed from the list of breeds with the same value
                    var breed = breeds.get(random.nextInt(breeds.size())).getValue();
                    Variant variant = null;
                    setBreed(breed);

                    if (!breed.getVariants().isEmpty() && random.nextFloat() < 0.2f) {
                        variant = breed.getVariants()
                                .get(random.nextInt(breed.getVariants().size()));
                        setVariant(variant.id());
                    }

                    if (spawnGroupData == null) {
                        var data = new DragonGroupData(breed, variant);
                        spawnGroupData = data;
                        setSpawnGroupId(data.groupId);
                    }
                }
            }
        }

        return super.finalizeSpawn(accessor, difficulty, spawnType, spawnGroupData);
    }
}
