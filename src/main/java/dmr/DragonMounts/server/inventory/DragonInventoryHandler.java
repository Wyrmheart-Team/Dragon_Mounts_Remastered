package dmr.DragonMounts.server.inventory;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.common.capability.types.NBTInterface;
import dmr.DragonMounts.network.packets.ClearDragonInventoryPacket;
import dmr.DragonMounts.network.packets.RequestDragonInventoryPacket;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.worlddata.DragonWorldData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = DMR.MOD_ID)
public class DragonInventoryHandler {

	// This is a client-side cache of dragon inventories which will be overwritten by the server whenever it changes or starts tracking a dragon
	public static Map<UUID, DragonInventory> clientSideInventories = new HashMap<>();

	@SubscribeEvent
	public static void startTracking(PlayerEvent.StartTracking startTracking) {
		if (startTracking.getEntity().level.isClientSide()) {
			return;
		}

		if (startTracking.getTarget() instanceof DMRDragonEntity dragon) {
			var dragonInventory = getOrCreateInventory(dragon);
			PacketDistributor.sendToPlayer(
				(ServerPlayer) startTracking.getEntity(),
				new RequestDragonInventoryPacket(dragon.getDragonUUID(), dragonInventory.writeNBT())
			);
		}
	}

	@SubscribeEvent
	public static void stopTracking(PlayerEvent.StopTracking stopTracking) {
		if (stopTracking.getEntity().level.isClientSide()) {
			return;
		}

		if (stopTracking.getTarget() instanceof DMRDragonEntity dragon) {
			PacketDistributor.sendToPlayer((ServerPlayer) stopTracking.getEntity(), new ClearDragonInventoryPacket(dragon.getDragonUUID()));
		}
	}

	public static DragonInventory getOrCreateInventory(DMRDragonEntity dragon) {
		return getOrCreateInventory(dragon.level, dragon.getDragonUUID());
	}

	public static DragonInventory getOrCreateInventory(Level level, UUID uuid) {
		if (level.isClientSide()) {
			clientSideInventories.computeIfAbsent(uuid, id -> new DragonInventory(level));
			return clientSideInventories.get(uuid);
		}

		DragonWorldData data = DragonWorldData.getInstance(level);

		if (!data.dragonInventories.containsKey(uuid)) {
			data.dragonInventories.put(uuid, new DragonInventory(level));
			data.setDirty();
		}

		return data.dragonInventories.get(uuid);
	}

	public static class DragonInventory implements NBTInterface, ContainerListener {

		@Getter
		@Setter
		boolean isDirty = false;

		public DragonInventory(Level level) {
			this.registryAccess = level.registryAccess();
			this.inventory.addListener(this);
		}

		public DragonInventory(HolderLookup.Provider registryAccess) {
			this.registryAccess = registryAccess;
			this.inventory.addListener(this);
		}

		public static final int SADDLE_SLOT = 0;
		public static final int ARMOR_SLOT = 1;
		public static final int CHEST_SLOT = 2;
		public static final int INVENTORY_SIZE = 9 * 3;

		private final HolderLookup.Provider registryAccess;
		public SimpleContainer inventory = new SimpleContainer(getInventorySize());

		public int getInventorySize() {
			return 3 + DragonInventory.INVENTORY_SIZE;
		}

		@Override
		public CompoundTag writeNBT() {
			CompoundTag compound = new CompoundTag();
			ListTag listtag = new ListTag();
			for (int i = 0; i < this.inventory.getContainerSize(); i++) {
				ItemStack itemstack = this.inventory.getItem(i);
				if (!itemstack.isEmpty()) {
					CompoundTag compoundtag = new CompoundTag();
					compoundtag.putByte("Slot", (byte) (i));
					listtag.add(itemstack.save(registryAccess, compoundtag));
				}
			}

			compound.put("Items", listtag);
			return compound;
		}

		@Override
		public void readNBT(CompoundTag compound) {
			ListTag listtag = new ListTag();
			if (compound.contains("Items")) {
				listtag = compound.getList("Items", 10);
			}

			for (int i = 0; i < listtag.size(); i++) {
				CompoundTag compoundtag = listtag.getCompound(i);
				int j = compoundtag.getByte("Slot") & 255;
				if (j < this.inventory.getContainerSize()) {
					this.inventory.setItem(j, ItemStack.parse(registryAccess, compoundtag).orElse(ItemStack.EMPTY));
				}
			}
		}

		@Override
		public void containerChanged(Container container) {
			setDirty(true);
		}
	}
}
