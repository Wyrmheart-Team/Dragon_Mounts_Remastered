package dmr.DragonMounts.types.abilities;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Represents an entry in a dragon breed's ability list.
 * Each entry specifies an ability and the chance of a dragon getting it.
 */
@Getter
public class DragonAbilityEntry {
    @SerializedName("ability")
    private String ability;

    @SerializedName("chance")
    private float chance;
}
