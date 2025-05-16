package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.network.packets.RequestDragonInventoryPacket;
import dmr.DragonMounts.server.container.DragonContainerMenu;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler.DragonInventory;
import dmr.DragonMounts.server.items.DragonArmorItem;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.util.PlayerStateUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Abstract class that implements dragon inventory functionality.
 * This extends the dragon entity hierarchy with inventory capabilities.
 */
abstract class DragonInventoryComponent extends DragonCombatComponent {
    protected static final EntityDataAccessor<Boolean> idChestDataAccessor =
            SynchedEntityData.defineId(DragonInventoryComponent.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> saddledDataAccessor =
            SynchedEntityData.defineId(DragonInventoryComponent.class, EntityDataSerializers.BOOLEAN);

    private static final ResourceLocation ARMOR_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "armor_attribute");

    protected DragonInventoryComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(saddledDataAccessor, false);
        builder.define(idChestDataAccessor, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putBoolean(NBTConstants.SADDLED, isSaddled());
        compound.putBoolean(NBTConstants.CHEST, hasChest());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(NBTConstants.SADDLED)) {
            setSaddled(compound.getBoolean(NBTConstants.SADDLED));
        }

        if (compound.contains(NBTConstants.CHEST)) {
            setChest(compound.getBoolean(NBTConstants.CHEST));
        }

        // Legacy support for old dragon inventories
        if (compound.contains("Items")) {
            ListTag listtag = compound.getList("Items", 10);

            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundtag = listtag.getCompound(i);
                int j = compoundtag.getByte("Slot") & 255;
                if (j < this.getInventory().getContainerSize()) {
                    this.getInventory()
                            .setItem(
                                    j,
                                    ItemStack.parse(this.registryAccess(), compoundtag)
                                            .orElse(ItemStack.EMPTY));
                }
            }
        }
    }

    public void equipChest(ItemStack stack, SoundSource source) {
        getInventory().setItem(DragonInventory.CHEST_SLOT, stack);
        setChest(true);
        //		level()
        //			.playSound(
        //				null,
        //				getX(),
        //				getY(),
        //				getZ(),
        //				SoundEvents.CHEST_OPEN,
        //				getSoundSource(),
        //				1,
        //				1
        //			);
    }

    /**
     * Equips a saddle on the dragon.
     */
    public void equipSaddle(ItemStack stack, SoundSource source) {
        getDragon().setSaddled(true);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_SADDLE, getSoundSource(), 1, 1);
        getInventory().setItem(DragonInventory.SADDLE_SLOT, stack);
    }

    /**
     * Gets the item that would be picked when the dragon is picked.
     */
    public ItemStack getPickedResult(HitResult target) {
        return DragonSpawnEgg.create(getBreed());
    }

    /**
     * Equips armor on the dragon.
     */
    public void equipArmor(Player pPlayer, ItemStack pArmor) {
        if (!isWearingArmor()) {
            setItemSlot(EquipmentSlot.BODY, pArmor.copyWithCount(1));
            if (!pPlayer.getAbilities().instabuild) {
                pArmor.shrink(1);
            }
            setArmor();
        }
    }

    /**
     * Sets the armor attributes for the dragon.
     */
    public void setArmor() {
        ItemStack itemstack = getBodyArmorItem();
        if (!level().isClientSide) {
            getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER);
            if (isArmor(itemstack)) {
                DragonArmor armor = DragonArmor.getArmorType(itemstack);
                if (armor != null) {
                    int i = armor.getProtection();
                    if (i != 0) {
                        getAttribute(Attributes.ARMOR)
                                .addTransientModifier(new AttributeModifier(ARMOR_MODIFIER, i, Operation.ADD_VALUE));
                    }
                }
            }
        }
    }

    /**
     * Checks if an item is dragon armor.
     */
    public boolean isArmor(ItemStack pStack) {
        return pStack.getItem() instanceof DragonArmorItem;
    }

    /**
     * Handles container changes (inventory updates).
     */
    public void containerChanged(Container pContainer) {
        setArmor();

        if (!level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(
                    this,
                    new RequestDragonInventoryPacket(
                            getDragon().getDragonUUID(), getDragonInventory().writeNBT()));
        }
    }

    /**
     * Opens the dragon's inventory screen for a player.
     */
    public void openCustomInventoryScreen(Player pPlayer) {
        pPlayer.openMenu(
                new SimpleMenuProvider((pId, pInventory, pPlayer1) -> createMenu(pId, pInventory), getDisplayName()),
                buf -> buf.writeInt(getId()));
    }

    /**
     * Creates a menu for the dragon's inventory.
     */
    private DragonContainerMenu createMenu(int pId, Inventory pInventory) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeInt(getId());
        return new DragonContainerMenu(pId, pInventory, buffer);
    }

    /**
     * Drops the dragon's equipment when it dies.
     */
    public void dropEquipment() {
        // Don't drop equipment if the dragon is selected and can be summoned again
        if (getOwner() instanceof Player player) {
            DragonOwnerCapability capability = PlayerStateUtils.getHandler(player);
            if (capability.isBoundToWhistle(getDragon())) {
                return;
            }
        }

        // Call the parent class's dropEquipment method
        super.dropEquipment();

        for (int i = 0; i < getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = getInventory().getItem(i);
            DMR.LOGGER.warn("Dropping item: {}", itemstack);
            if (!itemstack.isEmpty()
                    && !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                spawnAtLocation(itemstack);
            }
        }
    }

    /**
     * Checks if the dragon is wearing armor.
     */
    public boolean isWearingArmor() {
        return !getBodyArmorItem().isEmpty();
    }

    /**
     * Gets the dragon's inventory.
     */
    public DragonInventory getDragonInventory() {
        return DragonInventoryHandler.getOrCreateInventory(level, getDragon().getDragonUUID());
    }

    /**
     * Gets the dragon's inventory container.
     */
    public SimpleContainer getInventory() {
        return getDragonInventory().inventory;
    }

    /**
     * Checks if the specified inventory container has any changes.
     */
    public boolean hasInventoryChanged(Container pInventory) {
        return getInventory() != pInventory;
    }

    /**
     * Updates the dragon's equipment in its inventory container.
     */
    public void updateContainerEquipment() {
        if (!level().isClientSide) {
            setSaddled(!getInventory().getItem(DragonInventory.SADDLE_SLOT).isEmpty()
                    && getInventory().getItem(DragonInventory.SADDLE_SLOT).is(Items.SADDLE));
            setChest(!getInventory().getItem(DragonInventory.CHEST_SLOT).isEmpty()
                    && (getInventory().getItem(DragonInventory.CHEST_SLOT).is(Items.CHEST)
                            || getInventory()
                                    .getItem(DragonInventory.CHEST_SLOT)
                                    .is(Items.ENDER_CHEST)));
        }
    }

    /**
     * Checks if the dragon's inventory is empty.
     */
    public boolean inventoryEmpty() {
        for (int i = 3; i < getInventory().getContainerSize(); ++i) {
            if (!getInventory().getItem(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the dragon is saddled.
     */
    public boolean isSaddled() {
        return entityData.get(saddledDataAccessor)
                || getInventory().getItem(DragonInventory.SADDLE_SLOT).is(Items.SADDLE);
    }

    /**
     * Sets whether the dragon is saddled.
     */
    public void setSaddled(boolean saddled) {
        entityData.set(saddledDataAccessor, saddled);
    }

    @Override
    public boolean isSaddleable() {
        return isAlive() && !isHatchling() && isTame();
    }

    /**
     * Checks if the dragon has a chest.
     */
    public boolean hasChest() {
        return entityData.get(idChestDataAccessor)
                || getInventory().getItem(DragonInventory.CHEST_SLOT).is(Items.ENDER_CHEST)
                || getInventory().getItem(DragonInventory.CHEST_SLOT).is(Items.CHEST);
    }

    /**
     * Sets whether the dragon has a chest.
     */
    public void setChest(boolean hasChest) {
        entityData.set(idChestDataAccessor, hasChest);
    }

    @Override
    public ItemStack getBodyArmorItem() {
        return getInventory().getItem(DragonInventory.ARMOR_SLOT);
    }

    @Override
    public void setBodyArmorItem(ItemStack stack) {
        getInventory().setItem(DragonInventory.ARMOR_SLOT, stack);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot) {
            case BODY -> getBodyArmorItem();
            default -> super.getItemBySlot(slot);
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        this.verifyEquippedItem(stack);
        if (slot == EquipmentSlot.BODY) {
            ItemStack itemstack = getItemBySlot(slot);
            setBodyArmorItem(stack);
            this.onEquipItem(slot, itemstack, stack);
        }
    }
}
