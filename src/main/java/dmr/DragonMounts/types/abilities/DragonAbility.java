package dmr.DragonMounts.types.abilities;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
public class DragonAbility {
    @Setter
    private String id;

    @SerializedName("max_tier")
    private int maxTier;
    
    @SerializedName("breed_transferable")
    private boolean breedTransferable;
}
