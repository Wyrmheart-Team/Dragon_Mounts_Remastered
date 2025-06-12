package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.types.breath.DragonBreathType;
import net.minecraft.resources.ResourceLocation;

public record DragonVariant(
        @SerializedName("id") String id,
        @SerializedName("texture") ResourceLocation skinTexture,
        @SerializedName("saddle_texture") ResourceLocation saddleTexture,
        @SerializedName("glow_texture") ResourceLocation glowTexture,
        @SerializedName("egg_texture") ResourceLocation eggTexture,
        @SerializedName("breath_type") DragonBreathType breathType,
        @SerializedName("primary_color") String primaryColor,
        @SerializedName("secondary_color") String secondaryColor,
        @SerializedName("size_modifier") float sizeModifier) {
    public int getPrimaryColor() {
        return primaryColor == null ? 0 : Integer.parseInt(primaryColor, 16);
    }

    public int getSecondaryColor() {
        return secondaryColor == null ? 0 : Integer.parseInt(secondaryColor, 16);
    }
}
