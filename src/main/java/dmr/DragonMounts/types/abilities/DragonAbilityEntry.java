package dmr.DragonMounts.types.abilities;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents an entry in a dragon breed's ability list.
 * Each entry specifies an ability and the chance of a dragon getting it.
 */
@Getter
public class DragonAbilityEntry {
    
    //TODO: Add ability groups to be able to have reuseable groups of abilities
    @SerializedName("ability")
    private ResourceLocation ability;

    @SerializedName("chance")
    private float chance = 1.0f;
}
