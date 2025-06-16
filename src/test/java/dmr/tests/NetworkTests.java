package dmr.tests;

import dmr.DMRTestConstants;
import dmr.DragonMounts.network.packets.DragonStatePacket;
import dmr.DragonMounts.registry.entity.ModEntities;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

/**
 * Tests for the network packet handling functionality of the mod. These tests
 * verify that network packets can be properly encoded, decoded, and processed
 * to ensure reliable communication between client and server.
 */
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "Network")
public class NetworkTests {

    /**
     * Tests the DragonStatePacket encoding and decoding functionality.
     *
     * <p>
     * This test verifies that: 1. A DragonStatePacket can be created with the
     * correct entity ID and state 2. The packet can be properly encoded to a byte
     * buffer 3. The packet can be properly decoded from a byte buffer 4. The
     * decoded packet contains the same values as the original packet
     *
     * @param helper
     *               The game test helper
     */
    @EmptyTemplate(floor = true)
    @GameTest
    @TestHolder
    public static void testDragonStatePacket(ExtendedGameTestHelper helper) {
        // Create a player
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.DEFAULT_MODE);

        // Create a dragon
        var dragon = helper.spawn(ModEntities.DRAGON_ENTITY.get(), DMRTestConstants.TEST_POS);

        // Create a packet
        var packet = new DragonStatePacket(dragon.getId(), 1);

        // Test encoding and decoding
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        DragonStatePacket.STREAM_CODEC.encode(buffer, packet);
        DragonStatePacket decodedPacket = DragonStatePacket.STREAM_CODEC.decode(buffer);

        // Verify the packet has the correct values
        if (packet.getEntityId() != dragon.getId()) {
            helper.fail("Packet entity ID doesn't match expected value");
        }

        if (packet.getState() != 1) {
            helper.fail("Packet state doesn't match expected value");
        }

        // Verify the decoded packet has the correct values
        if (decodedPacket.getEntityId() != dragon.getId()) {
            helper.fail("Decoded packet entity ID doesn't match expected value");
        }

        if (decodedPacket.getState() != 1) {
            helper.fail("Decoded packet state doesn't match expected value");
        }

        helper.succeed();
    }
}
