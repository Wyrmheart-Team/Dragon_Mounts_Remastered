package dmr.DragonMounts.types;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

public record LootTableEntry(
        @SerializedName("table") ResourceLocation table,
        @SerializedName("chance") float chance,
        @SerializedName("min") int minAmount,
        @SerializedName("max") int maxAmount) {}
