package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, DMR.MOD_ID);
    public static final DeferredHolder<Attribute, Attribute> BREATH_DAMAGE = ATTRIBUTES.register(
            "breath_damage", (id) -> new RangedAttribute(id.getPath(), 0, 0, 1024).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> BREATH_COOLDOWN = ATTRIBUTES.register(
            "breath_cooldown", (id) -> new PercentageAttribute(id.getPath(), 0, 0, 100).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BITE_DAMAGE =
            ATTRIBUTES.register("bite_damage", (id) -> new RangedAttribute(id.getPath(), 0, 0, 1024).setSyncable(true));
}
