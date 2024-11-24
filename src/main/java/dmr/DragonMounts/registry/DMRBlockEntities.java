package dmr.DragonMounts.registry;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.blockentities.DragonEggBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DMRBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DragonMountsRemaster.MOD_ID);
	public static final Supplier<BlockEntityType<DragonEggBlockEntity>> DRAGON_EGG_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("dragon_egg", () -> BlockEntityType.Builder.of(DragonEggBlockEntity::new, DMRBlocks.DRAGON_EGG_BLOCK.get()).build(null));
}
