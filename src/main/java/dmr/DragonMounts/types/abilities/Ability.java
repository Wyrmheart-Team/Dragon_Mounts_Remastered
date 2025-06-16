package dmr.DragonMounts.types.abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public abstract class Ability {
    private String id;

    @Getter
    @Setter
    private int level = 1;

    @Getter
    private String abilityType;

    public Ability(String abilityType) {
        this.abilityType = abilityType;
    }

    public String type() {
        return abilityType;
    }

    public Component getTranslatedName() {
        return Component.translatable("dmr.ability." + id + ".name");
    }

    public Component getTranslatedDescription() {
        return Component.translatable("dmr.ability." + id + ".description");
    }

    public void onInitialize(TameableDragonEntity dragon) {}

    public void initializeDefinition(DragonAbility definition) {
        this.id = definition.getId();
    }

    /**
     * Called every tick for this ability.
     * If this is a nearby ability (isNearbyAbility() returns true), it will check if the owner is nearby
     * and call tickWithOwner() if they are.
     */
    public void tick(TameableDragonEntity dragon) {
        if (isNearbyAbility()) {
            if (dragon.getOwner() instanceof Player player) {
                if (dragon.distanceTo(player) <= getRange() || dragon.getControllingPassenger() == player) {
                    tickWithOwner(dragon, player);
                }
            }
        }
    }

    /**
     * Called when the dragon moves.
     * If this is a footprint ability (isFootprintAbility() returns true), it will handle footprint placement.
     */
    public void onMove(TameableDragonEntity dragon) {
        if (isFootprintAbility()) {
            if (dragon.level.isClientSide || !dragon.isAdult() || !dragon.onGround()) return;

            var chance = getFootprintChance(dragon);
            if (chance == 0) return;

            for (int i = 0; i < 4; i++) {
                // place only if randomly selected
                if (dragon.getRandom().nextFloat() > chance) {
                    continue;
                }

                // get footprint position
                int bx = (int) (dragon.getX() + ((i % 2) * 2 - 1) * 0.25f);
                int by = (int) dragon.getY();
                int bz = (int) (dragon.getZ() + (((i / 2f) % 2) * 2 - 1) * 0.25f);
                var pos = new BlockPos(bx, by, bz);

                placeFootprint(dragon, pos);
            }
        }
    }

    /**
     * Returns whether this ability should only be active when the owner is nearby.
     * Override this method to return true for abilities that should only be active when the owner is nearby.
     */
    public boolean isNearbyAbility() {
        return false;
    }

    /**
     * Returns the range for nearby abilities.
     * Override this method to customize the range.
     */
    public int getRange() {
        return 10;
    }

    /**
     * Called when the owner is nearby or riding the dragon.
     * Override this method for abilities that should do something when the owner is nearby.
     */
    public void tickWithOwner(TameableDragonEntity dragon, Player owner) {
        // Default implementation does nothing
    }

    /**
     * Returns whether this ability leaves footprints.
     * Override this method to return true for abilities that leave footprints.
     */
    public boolean isFootprintAbility() {
        return false;
    }

    /**
     * Returns the chance of leaving a footprint.
     * Override this method to customize the chance.
     */
    public float getFootprintChance(TameableDragonEntity dragon) {
        return 0.05f;
    }

    /**
     * Called to place a footprint at the given position.
     * Override this method for abilities that leave footprints.
     */
    public void placeFootprint(TameableDragonEntity dragon, BlockPos pos) {
        // Default implementation does nothing
    }

    public Map<ResourceLocation, Double> getAttributes() {
        return Map.of();
    }
}
