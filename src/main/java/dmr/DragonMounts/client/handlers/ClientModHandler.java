package dmr.DragonMounts.client.handlers;

import dmr.DragonMounts.client.model.DragonModel;
import dmr.DragonMounts.client.renderer.BlankEggRenderer;
import dmr.DragonMounts.client.renderer.DragonEggRenderer;
import dmr.DragonMounts.client.renderer.DragonRenderer;
import dmr.DragonMounts.registry.ModBlockEntities;
import dmr.DragonMounts.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class ClientModHandler {

    public static DragonModel MODEL_INSTANCE = new DragonModel();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.DRAGON_ENTITY.get(), manager -> new DragonRenderer(manager, MODEL_INSTANCE));
        event.registerBlockEntityRenderer(
                ModBlockEntities.DRAGON_EGG_BLOCK_ENTITY.get(), manager -> new DragonEggRenderer());
        event.registerBlockEntityRenderer(
                ModBlockEntities.BLANK_EGG_BLOCK_ENTITY.get(), manager -> new BlankEggRenderer());
    }
}
