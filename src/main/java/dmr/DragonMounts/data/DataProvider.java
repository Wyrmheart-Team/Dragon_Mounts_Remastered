package dmr.DragonMounts.data;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.registry.entity.ModEntities;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.common.Tags.Biomes;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddSpawnsBiomeModifier;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = DMR.MOD_ID, bus = Bus.MOD)
public class DataProvider {

    public static final ResourceKey<BiomeModifier> DRAGON_SPAWNS_OVERWORLD = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "dragon_spawns_overworld"));
    public static final ResourceKey<BiomeModifier> DRAGON_SPAWNS_NETHER = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "dragon_spawns_nether"));
    public static final ResourceKey<BiomeModifier> DRAGON_SPAWNS_END = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "dragon_spawns_end"));

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        CompletableFuture<Provider> registries = event.getLookupProvider();

        var BUILDER = new RegistrySetBuilder();

        BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
            HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);

            var spawners = List.of(new SpawnerData(ModEntities.DRAGON_ENTITY.get(), 1, 1, 2));
            // Register the biome modifiers.
            bootstrap.register(
                    DRAGON_SPAWNS_OVERWORLD,
                    new AddSpawnsBiomeModifier(biomes.getOrThrow(Biomes.IS_OVERWORLD), spawners));
            bootstrap.register(
                    DRAGON_SPAWNS_NETHER, new AddSpawnsBiomeModifier(biomes.getOrThrow(Biomes.IS_NETHER), spawners));
            bootstrap.register(
                    DRAGON_SPAWNS_END, new AddSpawnsBiomeModifier(biomes.getOrThrow(Biomes.IS_END), spawners));
        });

        var builtinEntriesProvider =
                new DatapackBuiltinEntriesProvider(output, registries, BUILDER, Set.of(DMR.MOD_ID));
        generator.addProvider(event.includeServer(), builtinEntriesProvider);

        generator.addProvider(
                event.includeServer(),
                new BlockTagProvider(output, event.getLookupProvider(), DMR.MOD_ID, existingFileHelper));
        generator.addProvider(
                event.includeServer(),
                new EntityTagProvider(output, event.getLookupProvider(), DMR.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeServer(), new DMRRecipeProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new DMRItemModelProvider(output, DMR.MOD_ID, existingFileHelper));

        // Add config translation provider
        // Generated language file will overwrite built in file, so only use in dev
        //        generator.addProvider(event.includeServer(), new ConfigTranslationProvider(output));
    }
}
