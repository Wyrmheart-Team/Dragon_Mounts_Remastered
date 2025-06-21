package dmr.DragonMounts.registry.datapack;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.base_abilities.*;
import dmr.DragonMounts.types.abilities.generic_abilities.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public class DragonAbilityRegistry {
    // Registry for ability types (implementations of the Ability interface)
    private static final Map<String, Function<String, Ability>> ABILITY_TYPES = new HashMap<>();

    // Registry for ability definitions (from datapacks)
    private static final Map<ResourceLocation, DragonAbility> ABILITY_DEFINITIONS = new HashMap<>();

    /**
     * Initialize the registry with the minimal set of built-in ability types.
     */
    public static void init() {
        // Register the minimal set of ability types
        registerAbilityType("effect", GenericEffectAbility::new);
        registerAbilityType("attribute", GenericAttributeAbility::new);
        registerAbilityType("aura", GenericAuraAbility::new);
        registerAbilityType("monster_aura", GenericMonsterAuraAbility::new);
        registerAbilityType("conditional_effect", GenericConditionalEffectAbility::new);

        registerAbilityType("hot_feet", HotFeetAbility::new);
        registerAbilityType("infernal_pact", InfernalPactAbility::new);
        registerAbilityType("frost_walker", FrostWalkerAbility::new);
        registerAbilityType("camouflage", CamouflageAbility::new);
        registerAbilityType("floral_trail", FloralTrailAbility::new);
        registerAbilityType("ender_cloak", EnderCloakAbility::new);
        registerAbilityType("void_walker", VoidWalker::new);
        registerAbilityType("gem_guard", GemGuardAbility::new);
        registerAbilityType("crystal_harmony", CrystalHarmonyAbility::new);
        registerAbilityType("ethereal_harmony", EtherealHarmonyAbility::new);
    }

    /**
     * Register a new ability type.
     *
     * @param id The ID of the ability type
     * @param factory A function that creates a new instance of the ability with the given ID
     */
    public static void registerAbilityType(String id, Function<String, Ability> factory) {
        ABILITY_TYPES.put(id, factory);
    }

    /**
     * Register an ability definition from a datapack.
     *
     * @param definition The ability definition
     */
    public static void registerAbilityDefinition(DragonAbility definition) {
        ResourceLocation id = DMR.id(definition.getId());
        ABILITY_DEFINITIONS.put(id, definition);
    }

    public static void setAbilityDefinitions(List<DragonAbility> definitions) {
        ABILITY_DEFINITIONS.clear();
        for (DragonAbility definition : definitions) {
            registerAbilityDefinition(definition);
        }
    }

    /**
     * Get an ability definition by ID.
     *
     * @param id The ID of the ability definition
     * @return The ability definition, or null if not found
     */
    public static DragonAbility getAbilityDefinition(ResourceLocation id) {
        return ABILITY_DEFINITIONS.get(id);
    }

    /**
     * Create an ability instance from a definition.
     *
     * @param definition The ability definition
     * @return A new ability instance, or null if the ability type is not registered
     */
    public static Ability createAbilityInstance(DragonAbility definition) {
        String abilityId = definition.getId();
        String abilityType = definition.getAbilityType();

        // If ability_type is not specified, use the ID as the type
        if (abilityType == null || abilityType.isEmpty()) {
            abilityType = abilityId;
        }

        Function<String, Ability> factory = ABILITY_TYPES.get(abilityType);
        if (factory == null) {
            DMR.LOGGER.warn("Unknown ability type: {}", abilityType);
            return null;
        }

        Ability ability = factory.apply(abilityId);
        ability.initializeDefinition(definition);
        return ability;
    }
}
