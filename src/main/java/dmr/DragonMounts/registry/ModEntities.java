package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.server.entity.DragonConstants;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = DMR.MOD_ID, bus = Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, DMR.MOD_ID);

    public static final Supplier<EntityType<TameableDragonEntity>> DRAGON_ENTITY =
            ENTITIES.register("dragon", () -> EntityType.Builder.of(TameableDragonEntity::new, MobCategory.AMBIENT)
                    .sized(DragonConstants.BASE_WIDTH, DragonConstants.BASE_HEIGHT)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("dragon"));

    @SubscribeEvent
    public static void attributeCreationEvent(EntityAttributeCreationEvent event) {
        event.put(DRAGON_ENTITY.get(), TameableDragonEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawns(RegisterSpawnPlacementsEvent event) {
        event.register(
                DRAGON_ENTITY.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::canSpawnDragon,
                Operation.OR);
    }

    private static boolean canSpawnDragon(
            EntityType<TameableDragonEntity> entityType,
            ServerLevelAccessor serverLevel,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        if (spawnType == MobSpawnType.CHUNK_GENERATION)
            return false; // Dragon spawning relies on the worldgen to decide breed.
        if (!ServerConfig.MOD_CONFIG_SPEC.isLoaded() || !ServerConfig.ENABLE_NATURAL_DRAGON_SPAWNS)
            return false;
        return TameableDragonEntity.checkDragonSpawnRules(entityType, serverLevel, spawnType, pos, random);
    }
}
