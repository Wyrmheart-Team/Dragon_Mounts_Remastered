package dmr.DragonMounts.registry;

import com.mojang.serialization.Codec;
import dmr.DragonMounts.DMR;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DMR.MOD_ID);

    public static final Supplier<DataComponentType<String>> DRAGON_BREED =
            register("dragon_breed", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final Supplier<DataComponentType<String>> DRAGON_VARIANT =
            register("dragon_breed_variant", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final Supplier<DataComponentType<String>> ARMOR_TYPE =
            register("armor_type", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final Supplier<DataComponentType<Integer>> EGG_HATCH_TIME =
            register("egg_hatch_time", Codec.INT, ByteBufCodecs.INT);
    public static final Supplier<DataComponentType<String>> EGG_OWNER =
            register("egg_owner", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final Supplier<DataComponentType<CompoundTag>> EGG_OUTCOME =
            register("egg_outcome", CompoundTag.CODEC, ByteBufCodecs.COMPOUND_TAG);
    
    private static <T> Supplier<DataComponentType<T>> register(
            String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return COMPONENTS.registerComponentType(
                name, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
    }
}
