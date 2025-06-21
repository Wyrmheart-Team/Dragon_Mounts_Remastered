package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.datapack.DragonAbilityRegistry;
import dmr.DragonMounts.registry.datapack.DragonAbilityTagRegistry;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.DragonAbilityEntry;
import dmr.DragonMounts.util.MiscUtils;
import java.util.*;
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

        // Improved ability selection algorithm
        selectAbilities(abilities, possibilities, maxAbilities, new HashSet<>());

        this.abilities.addAll(abilities);
    }

    /**
     * Selects abilities from the possibilities list based on their individual chances.
     * This method processes all entries, including nested tags, and adds abilities
     * based on their adjusted chances.
     *
     * @param selectedAbilities The list to add selected abilities to
     * @param possibilities The list of possible abilities to select from
     * @param maxAbilities The maximum number of abilities to select
     * @param processedTags Set of already processed tags to avoid infinite recursion
     */
    private void selectAbilities(
            ArrayList<Ability> selectedAbilities,
            List<DragonAbilityEntry> possibilities,
            int maxAbilities,
            Set<String> processedTags) {
        if (selectedAbilities.size() >= maxAbilities) return;

        // Create a copy and shuffle to randomize order for equal chances
        List<DragonAbilityEntry> entries = new ArrayList<>(possibilities);
        Collections.shuffle(entries);

        // Sort by adjusted chance (higher chance first)
        entries.sort((a, b) ->
                Float.compare(calculateAdjustedChance(b.getChance()), calculateAdjustedChance(a.getChance())));

        // Process each entry
        for (DragonAbilityEntry entry : entries) {
            if (selectedAbilities.size() >= maxAbilities) break;

            var id = entry.getAbility();

            if (id.getPath().startsWith("#")) {
                // This is a tag reference, process it recursively
                String tagName = id.getPath().substring(1);
                String fullTagId = id.getNamespace() + ":" + tagName;

                // Skip if we've already processed this tag to avoid infinite recursion
                if (processedTags.contains(fullTagId)) continue;

                // Mark this tag as processed
                processedTags.add(fullTagId);

                // Get the tag and process its entries
                var tagId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), tagName);
                var abilityTag = DragonAbilityTagRegistry.getAbilityTag(tagId);

                if (abilityTag != null) {
                    // Recursively process the tag's entries
                    selectAbilities(
                            selectedAbilities, abilityTag.getAbilities(), maxAbilities, new HashSet<>(processedTags));
                }
            } else {
                // This is a direct ability, try to add it
                tryAddAbility(selectedAbilities, id, entry.getChance());
            }
        }
    }

    /**
     * Tries to add an ability to the list based on its chance.
     *
     * @param abilities The list to add the ability to
     * @param id The ability ID
     * @param chance The base chance of getting the ability
     */
    private void tryAddAbility(ArrayList<Ability> abilities, ResourceLocation id, float chance) {
        // Adjust chance based on dragon tier
        float adjustedChance = calculateAdjustedChance(chance);
        if (Math.random() >= adjustedChance) return;

        // Get the ability definition
        DragonAbility definition = DragonAbilityRegistry.getAbilityDefinition(id);
        if (definition == null) return;

        // Create and initialize the ability
        Ability ability = DragonAbilityRegistry.createAbilityInstance(definition);
        if (ability == null) return;

        ability.onInitialize(getDragon());

        // Set ability level based on weighted random up to max tier
        int maxTier = Math.max(1, definition.getMaxTier());
        int level = getWeightedRandomTier(maxTier);
        ability.setLevel(level);

        // Add the ability
        abilities.add(ability);
    }

    /**
     * Calculates an adjusted chance based on the dragon's tier.
     * At tier 0, chances remain the same.
     * As tier increases, rare abilities become more common and common abilities become more rare.
     * At max tier, the probabilities are completely inverted.
     *
     * @param baseChance The base chance of getting the ability
     * @return The adjusted chance based on the dragon's tier
     */
    private float calculateAdjustedChance(float baseChance) {
        if (!ServerConfig.ENABLE_DRAGON_TIERS) return baseChance;

        // Get the dragon's tier level (0-4)
        int tierLevel = getDragon().getTier().getLevel();

        // At tier 0, return the original chance
        if (tierLevel == 0) return baseChance;

        // Calculate the inversion factor (0 at tier 0, 1 at max tier)
        float maxTier = 4.0f; // LEGENDARY is tier 4
        float inversionFactor = tierLevel / maxTier;

        // Invert the probability based on the inversion factor
        // As inversionFactor approaches 1, the result approaches (1 - baseChance)
        return (1.0f - inversionFactor) * baseChance + inversionFactor * (1.0f - baseChance);
    }

    private int calculateMaxAbilities() {
        // Add modifiers from traits
        return MiscUtils.randomUpperLower(
                1, ServerConfig.ENABLE_DRAGON_TIERS ? getDragon().getTier().getMaxAbilities() : 6);
    }

    /**
     * Generates a weighted random tier between 1 and maxTier.
     * The distribution is influenced by the dragon's own tier.
     * Higher tier dragons are more likely to get higher tier abilities.
     *
     * @param maxTier The maximum tier to generate
     * @return A weighted random tier between 1 and maxTier
     */
    private int getWeightedRandomTier(int maxTier) {
        if (maxTier <= 1) return 1;

        // Get the dragon's tier level (0-4)
        int dragonTierLevel =
                ServerConfig.ENABLE_DRAGON_TIERS ? getDragon().getTier().getLevel() : 0;

        // Calculate a bias factor based on the dragon's tier
        // Higher tier dragons have a higher bias towards higher ability tiers
        double tierBias = dragonTierLevel * 0.15; // 0% for COMMON, up to 60% for LEGENDARY

        // Generate a random value with the bias applied
        double random = Math.random();

        // Apply the bias - this shifts the distribution towards higher values for higher tier dragons
        random = Math.pow(random, 1.0 - tierBias);

        // Scale to the max tier
        double weightedRandom = random * maxTier;

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
