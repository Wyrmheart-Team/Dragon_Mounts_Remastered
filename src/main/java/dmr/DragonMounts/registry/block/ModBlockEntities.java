package dmr.DragonMounts.registry.block;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.blockentities.DMRBlankEggBlockEntity;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DMR.MOD_ID);
    public static final Supplier<BlockEntityType<DMREggBlockEntity>> DRAGON_EGG_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "dragon_egg", () -> BlockEntityType.Builder.of(DMREggBlockEntity::new, ModBlocks.DRAGON_EGG_BLOCK.get())
                    .build(null));
    public static final Supplier<BlockEntityType<DMRBlankEggBlockEntity>> BLANK_EGG_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("blank_egg", () -> BlockEntityType.Builder.of(
                            DMRBlankEggBlockEntity::new, ModBlocks.BLANK_EGG_BLOCK.get())
                    .build(null));
}
