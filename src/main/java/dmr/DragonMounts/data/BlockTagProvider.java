package dmr.DragonMounts.data;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.types.abilities.dragon_types.fire_dragon.HotFeetAbility;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

class BlockTagProvider extends BlockTagsProvider {

	static final TagKey<Block> AETHER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("aether_dragon_habitat_blocks"));
	static final TagKey<Block> FIRE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("fire_dragon_habitat_blocks"));
	static final TagKey<Block> FOREST_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("forest_dragon_habitat_blocks"));
	static final TagKey<Block> ICE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("ice_dragon_habitat_blocks"));
	static final TagKey<Block> NETHER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("nether_dragon_habitat_blocks"));
	static final TagKey<Block> WATER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("water_dragon_habitat_blocks"));
	static final TagKey<Block> AMETHYST_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("amethyst_dragon_habitat_blocks"));
	public final TagKey<Block> SCULK_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("sculk_dragon_habitat_blocks"));
	public final TagKey<Block> LUSH_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("lush_dragon_habitat_blocks"));
	public final TagKey<Block> END_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("end_dragon_habitat_blocks"));
	public final TagKey<Block> GHOST_DRAGON_HABITAT_BLOCKS = BlockTags.create(DMR.id("ghost_dragon_habitat_blocks"));

	public BlockTagProvider(
		PackOutput output,
		CompletableFuture<Provider> lookupProvider,
		String modId,
		ExistingFileHelper existingFileHelper
	) {
		super(output, lookupProvider, modId, existingFileHelper);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(AETHER_DRAGON_HABITAT_BLOCKS).add(Blocks.WHITE_WOOL);
		tag(GHOST_DRAGON_HABITAT_BLOCKS).add(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);

		tag(FIRE_DRAGON_HABITAT_BLOCKS).add(Blocks.FIRE, Blocks.LAVA, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE);

		tag(FOREST_DRAGON_HABITAT_BLOCKS).addTags(BlockTags.LEAVES, BlockTags.SAPLINGS, BlockTags.FLOWERS).add(Blocks.VINE);

		tag(ICE_DRAGON_HABITAT_BLOCKS).addTags(BlockTags.ICE, BlockTags.SNOW);

		tag(NETHER_DRAGON_HABITAT_BLOCKS)
			.addTags(BlockTags.SOUL_FIRE_BASE_BLOCKS, BlockTags.BASE_STONE_NETHER, BlockTags.WARPED_STEMS, BlockTags.CRIMSON_STEMS)
			.add(
				Blocks.GLOWSTONE,
				Blocks.SOUL_FIRE,
				Blocks.SOUL_CAMPFIRE,
				Blocks.SOUL_TORCH,
				Blocks.NETHER_GOLD_ORE,
				Blocks.NETHER_QUARTZ_ORE,
				Blocks.ANCIENT_DEBRIS,
				Blocks.TWISTING_VINES_PLANT,
				Blocks.WEEPING_VINES_PLANT,
				Blocks.SHROOMLIGHT,
				Blocks.NETHER_WART,
				Blocks.NETHER_WART_BLOCK
			);

		tag(WATER_DRAGON_HABITAT_BLOCKS)
			.addTags(BlockTags.CORALS, BlockTags.WALL_CORALS, BlockTags.CORAL_BLOCKS)
			.add(
				Blocks.SEAGRASS,
				Blocks.TALL_SEAGRASS,
				Blocks.KELP,
				Blocks.KELP_PLANT,
				Blocks.PRISMARINE,
				Blocks.SEA_LANTERN,
				Blocks.SEA_PICKLE,
				Blocks.WATER,
				Blocks.BUBBLE_COLUMN,
				Blocks.BUBBLE_CORAL
			);

		tag(AMETHYST_DRAGON_HABITAT_BLOCKS).add(Blocks.AMETHYST_CLUSTER, Blocks.BUDDING_AMETHYST, Blocks.AMETHYST_BLOCK);

		tag(SCULK_DRAGON_HABITAT_BLOCKS)
			.add(Blocks.SCULK_SENSOR, Blocks.SCULK_CATALYST, Blocks.SCULK_SHRIEKER, Blocks.SCULK_VEIN, Blocks.SCULK)
			.addTags(BlockTags.SCULK_REPLACEABLE);

		tag(LUSH_DRAGON_HABITAT_BLOCKS).add(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, Blocks.CAVE_VINES, Blocks.GLOW_LICHEN);

		tag(END_DRAGON_HABITAT_BLOCKS)
			.add(Blocks.END_STONE, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_SLAB, Blocks.PURPUR_STAIRS)
			.add(Blocks.END_ROD, Blocks.CHORUS_PLANT, Blocks.CHORUS_FLOWER);

		tag(HotFeetAbility.BURNABLES_TAG).addTags(BlockTags.FLOWERS, BlockTags.SAPLINGS, BlockTags.FLOWERS);
	}
}
