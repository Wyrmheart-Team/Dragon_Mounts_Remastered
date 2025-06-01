package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.network.PacketHelper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registry for all packet types in the mod.
 * <p>
 * This class is responsible for registering all packet types with the event system.
 * It utilizes the package-private constructors of packet classes to create instances
 * for registration.
 */
public class PacketRegistry {

    /**
     * Registers all packet types with the event system.
     *
     * @param event The registration event
     */
    public static void registerEvent(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(DMR.MOD_ID);
        registerPackets(registrar);
    }

    /**
     * Registers all packet types with the registrar.
     *
     * @param registrar The payload registrar
     */
    private static void registerPackets(PayloadRegistrar registrar) {
        // Dragon state and control packets
        PacketHelper.registerPacket(registrar, new DragonStatePacket());
        PacketHelper.registerPacket(registrar, new DragonCommandPacket());
        PacketHelper.registerPacket(registrar, new SummonDragonPacket());
        PacketHelper.registerPacket(registrar, new DismountDragonPacket());

        // Dragon data sync packets
        PacketHelper.registerPacket(registrar, new SyncDataPackPacket());
        PacketHelper.registerPacket(registrar, new CompleteDataSync());
        PacketHelper.registerPacket(registrar, new DragonAgeSyncPacket());
        PacketHelper.registerPacket(registrar, new DragonRespawnDelayPacket());
        PacketHelper.registerPacket(registrar, new DragonNBTSync());

        // Dragon action packets
        PacketHelper.registerPacket(registrar, new DragonAttackPacket());
        PacketHelper.registerPacket(registrar, new DragonBreathPacket());
        PacketHelper.registerPacket(registrar, new DragonBreathTargetSyncPacket());

        // Client configuration packets
        PacketHelper.registerPacket(registrar, new ConfigSyncPacket());

        // Egg and inventory packets
        PacketHelper.registerPacket(registrar, new BlankEggSyncPacket());
        PacketHelper.registerPacket(registrar, new RequestDragonInventoryPacket());
        PacketHelper.registerPacket(registrar, new ClearDragonInventoryPacket());
    }
}
