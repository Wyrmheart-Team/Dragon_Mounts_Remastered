package dmr.DragonMounts.server.items.dev;

import dmr.DragonMounts.server.items.DMRDevItem;
import dmr.DragonMounts.util.BreedingUtils;
import java.util.StringJoiner;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HabitatOutcomeCheck extends DMRDevItem {

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pIsSelected && pEntity instanceof Player pPlayer) {
            if (!pLevel.isClientSide && pPlayer.tickCount % 10 == 0) {
                var outcomes = BreedingUtils.getHabitatBreedOutcomes((ServerLevel) pLevel, pPlayer.blockPosition());
                if (outcomes.size() > 0) {
                    StringJoiner joiner = new StringJoiner(", ");
                    outcomes.stream()
                            .limit(3)
                            .forEach(outcome -> joiner.add(outcome.getValue().getId() + ": " + outcome.getKey()));
                    pPlayer.displayClientMessage(Component.literal(joiner.toString()), true);
                } else {
                    pPlayer.displayClientMessage(Component.literal("None"), true);
                }
            }
        }
    }
}
