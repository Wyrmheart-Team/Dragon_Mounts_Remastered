package dmr.DragonMounts.types.armor;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.datapack.DragonArmorRegistry;
import dmr.DragonMounts.types.DatapackEntry;
import dmr.DragonMounts.types.LootTableEntry;
import dmr.DragonMounts.types.LootTableProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Getter
public class DragonArmor extends DatapackEntry implements LootTableProvider {

    @SerializedName("protection")
    private int protection = 0;

    public Component getName() {
        return Component.translatable(DMR.MOD_ID + ".dragon_armor." + getId());
    }

    @SerializedName("loot_tables")
    private List<LootTableEntry> lootTable = new ArrayList<>();

    public static DragonArmor getArmorType(ItemStack stack) {
        var type = stack.get(ModComponents.ARMOR_TYPE);
        return DragonArmorRegistry.getDragonArmor(type);
    }

    public static void setArmorType(ItemStack stack, DragonArmor type) {
        if (type == null) return;
        stack.set(ModComponents.ARMOR_TYPE, type.getId());
    }
}
