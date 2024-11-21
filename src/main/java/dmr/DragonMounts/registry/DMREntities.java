package dmr.DragonMounts.registry;


import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber( modid = DragonMountsRemaster.MOD_ID, bus = Bus.MOD )
public class DMREntities
{
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, DragonMountsRemaster.MOD_ID);
	
	public static final Supplier<EntityType<DMRDragonEntity>> DRAGON_ENTITY = ENTITIES.register("dragon", () -> EntityType.Builder.of(DMRDragonEntity::new, MobCategory.CREATURE).sized(DMRDragonEntity.BASE_WIDTH, DMRDragonEntity.BASE_HEIGHT).clientTrackingRange(10).updateInterval(3).build("dragon"));
	
	@SubscribeEvent
	public static void attributeCreationEvent(EntityAttributeCreationEvent event){
		event.put(DRAGON_ENTITY.get(), DMRDragonEntity.createAttributes().build());
	}
}
