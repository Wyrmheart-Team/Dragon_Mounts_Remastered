package dmr.DragonMounts.types.abilities.generic_abilities;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * A generic ability that modifies dragon attributes.
 */
public class GenericAttributeAbility extends Ability {
    private Map<ResourceLocation, Double> attributes;

    public GenericAttributeAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        attributes = definition.getAttributes();
    }

    @Override
    public void onInitialize(TameableDragonEntity dragon) {
        // Apply attribute modifiers to the dragon
        if (attributes != null) {
            for (Map.Entry<ResourceLocation, Double> entry : attributes.entrySet()) {
                var holderKey = BuiltInRegistries.ATTRIBUTE.getHolder(entry.getKey());

                // Scale the attribute value based on the ability level
                double scaledValue = entry.getValue() * getLevel();

                holderKey.ifPresent(attributeReference -> dragon.getAttribute(attributeReference)
                        .addPermanentModifier(
                                new AttributeModifier(DMR.id("Ability_" + type()), scaledValue, Operation.ADD_VALUE)));
            }
        }
    }

    @Override
    public Map<ResourceLocation, Double> getAttributes() {
        return attributes;
    }
}
