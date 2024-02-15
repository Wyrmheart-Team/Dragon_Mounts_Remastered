package dmr.DragonMounts.registry;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.blocks.DragonMountsEggBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class DMRBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, DragonMountsRemaster.MOD_ID);
	public static final Supplier<DragonMountsEggBlock> DRAGON_EGG_BLOCK = BLOCKS.register("dragon_egg", () -> new DragonMountsEggBlock(BlockBehaviour.Properties.of().mapColor(DyeColor.BLACK).strength(0f, 9f).lightLevel(s -> 1).noOcclusion()));
}
