package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonAbilityRegistry;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.DragonAbilityEntry;
import dmr.DragonMounts.util.MiscUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

abstract class DragonAbilitiesComponent extends DragonTierComponent {
    protected DragonAbilitiesComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public <T extends Ability> boolean hasAbility(Class<T> abilityClass) {
        for (Ability ability : abilities) {
            if (abilityClass.isAssignableFrom(ability.getClass())) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends Ability> T getAbility(Class<T> abilityClass) {
        for (Ability ability : abilities) {
            if (abilityClass.isAssignableFrom(ability.getClass())) return (T) ability;
        }
        return null;
    }

    public int getAbilityTier(Class<? extends Ability> abilityClass) {
        for (Ability ability : abilities) {
            if (abilityClass.isAssignableFrom(ability.getClass())) return ability.getLevel();
        }
        return 0;
    }

    public int getAbilityTierById(String id) {
        for (Ability ability : abilities) {
            if (ability.type().equals(id)) return ability.getLevel();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public <T extends Ability> T getAbilityById(String id) {
        for (Ability ability : abilities) {
            if (ability.type().equals(id)) return (T) ability;
        }
        return null;
    }

    public boolean hasAbilityId(String id) {
        for (Ability ability : abilities) {
            if (ability.type().equals(id)) return true;
        }
        return false;
    }

    @Getter
    private final List<Ability> abilities = new ArrayList<>();

    @Override
    public void finalizeDragon(@Nullable TameableDragonEntity parent1, @Nullable TameableDragonEntity parent2) {
        super.finalizeDragon(parent1, parent2);

        var abilities = new ArrayList<Ability>();
        int maxAbilities = calculateMaxAbilities(); // Based on dragon traits
        var possibilities = new ArrayList<>(getBreed().getAbilities());

        // Add parent abilities if they're transferable
        Consumer<TameableDragonEntity> addParentAbilities = (parent) -> {
            for (Ability ability : parent.getAbilities()) {
                String abilityId = ability.type();
                DragonAbility definition = parent.getAbilityDefinition(abilityId);

                if (definition != null && definition.isBreedTransferable()) {
                    // Find the ability entry in the parent's breed
                    for (DragonAbilityEntry breedEntry : parent.getBreed().getAbilities()) {
                        if (breedEntry.getAbility().equals(abilityId)) {
                            possibilities.add(breedEntry);
                            break;
                        }
                    }
                }
            }
        };

        if (parent1 != null) addParentAbilities.accept(parent1);
        if (parent2 != null) addParentAbilities.accept(parent2);

        Collections.shuffle(possibilities);

        for (DragonAbilityEntry entry : possibilities) {
            if (abilities.size() >= maxAbilities) break;

            if (Math.random() < entry.getChance()) {
                DragonAbility definition = DragonAbilityRegistry.getAbilityDefinition(DMR.id(entry.getAbility()));
                Ability ability = DragonAbilityRegistry.createAbilityInstance(definition);

                if (ability != null) {
                    ability.onInitialize(getDragon());

                    // Set ability level based on weighted random up to max tier
                    int maxTier = Math.max(1, definition.getMaxTier());
                    int level = getWeightedRandomTier(maxTier);
                    ability.setLevel(level);

                    abilities.add(ability);
                }
            }
        }

        this.abilities.addAll(abilities);
    }

    private int calculateMaxAbilities() {
        // Add modifiers from traits
        return MiscUtils.randomUpperLower(
                1, ServerConfig.ENABLE_DRAGON_TIERS ? getDragon().getTier().getMaxAbilities() : 6);
    }

    /**
     * Generates a weighted random tier between 1 and maxTier.
     * Higher tiers are less likely to be selected.
     *
     * @param maxTier The maximum tier to generate
     * @return A weighted random tier between 1 and maxTier
     */
    private int getWeightedRandomTier(int maxTier) {
        if (maxTier <= 1) return 1;

        // Use a triangular distribution to make higher tiers less likely
        double random = Math.random();
        double weightedRandom = Math.sqrt(random) * maxTier;
        return Math.max(1, (int) Math.ceil(weightedRandom));
    }

    public DragonAbility getAbilityDefinition(String id) {
        return DragonAbilityRegistry.getAbilityDefinition(ResourceLocation.tryParse(id));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        // Save abilities - only save the ability IDs
        ListTag abilitiesTag = new ListTag();
        for (Ability ability : abilities) {
            CompoundTag abilityTag = new CompoundTag();
            abilityTag.putString("type", ability.type());
            abilitiesTag.add(abilityTag);
        }
        compound.put("Abilities", abilitiesTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        // Clear existing abilities
        abilities.clear();

        // Load abilities
        if (compound.contains("Abilities")) {
            ListTag abilitiesTag = compound.getList("Abilities", 10); // 10 = CompoundTag
            for (int i = 0; i < abilitiesTag.size(); i++) {
                CompoundTag abilityTag = abilitiesTag.getCompound(i);
                String type = abilityTag.getString("type");

                // Get ability definition from registry
                DragonAbility definition = DragonAbilityRegistry.getAbilityDefinition(ResourceLocation.tryParse(type));
                if (definition != null) {
                    Ability ability = DragonAbilityRegistry.createAbilityInstance(definition);
                    if (ability != null) {
                        ability.onInitialize(getDragon());
                        abilities.add(ability);
                    }
                }
            }
        }
    }

    @Override
    protected void onChangedBlock(ServerLevel level, BlockPos pos) {
        super.onChangedBlock(level, pos);
        abilities.forEach(ability -> ability.onMove(getDragon()));
    }

    public void tick() {
        super.tick();
        if (tickCount % 20 == 0) abilities.forEach(ability -> ability.tick(getDragon()));
    }
}
