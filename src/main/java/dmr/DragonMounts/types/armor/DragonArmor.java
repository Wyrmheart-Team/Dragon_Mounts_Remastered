package dmr.DragonMounts.types.armor;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.registry.DragonArmorRegistry;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.types.LootTableEntry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DragonArmor {

    @Setter
    private String id;

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
