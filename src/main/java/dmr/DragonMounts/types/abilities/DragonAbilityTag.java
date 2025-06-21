package dmr.DragonMounts.types.abilities;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.types.DatapackEntry;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class DragonAbilityTag extends DatapackEntry {
    @SerializedName("abilities")
    private List<DragonAbilityEntry> abilities = new ArrayList<>();
}
