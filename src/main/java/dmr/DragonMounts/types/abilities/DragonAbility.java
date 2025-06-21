package dmr.DragonMounts.types.abilities;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.types.DatapackEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a dragon ability definition loaded from datapacks.
 * This class defines the properties of an ability without implementing behavior.
 */
@Getter
public class DragonAbility extends DatapackEntry {
    @SerializedName("max_tier")
    private int maxTier;

    @SerializedName("breed_transferable")
    private boolean breedTransferable;

    @SerializedName("ability_type")
    private String abilityType;

    @SerializedName("properties")
    private Map<String, Object> properties = new HashMap<>();

    @SerializedName("attributes")
    private Map<ResourceLocation, Double> attributes = new HashMap<>();

    @SerializedName("particles")
    private List<String> particles = new ArrayList<>();
}
