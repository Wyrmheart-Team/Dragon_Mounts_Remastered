package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.blocks.BlankEggBlock;
import dmr.DragonMounts.server.blocks.DMREggBlock;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, DMR.MOD_ID);
    public static final Supplier<DMREggBlock> DRAGON_EGG_BLOCK = BLOCKS.register(
            "dragon_egg",
            () -> new DMREggBlock(BlockBehaviour.Properties.of()
                    .mapColor(DyeColor.BLACK)
                    .strength(0f, 9f)
                    .lightLevel(s -> 1)
                    .noOcclusion()));
    public static final Supplier<Block> BLANK_EGG_BLOCK = BLOCKS.register(
            "blank_egg",
            () -> new BlankEggBlock(BlockBehaviour.Properties.of()
                    .mapColor(DyeColor.BLACK)
                    .strength(0f, 9f)
                    .lightLevel(s -> 1)
                    .noOcclusion()));
}
