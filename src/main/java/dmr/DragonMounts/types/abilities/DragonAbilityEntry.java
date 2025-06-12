package dmr.DragonMounts.types.abilities;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class DragonAbilityEntry {
	@SerializedName("ability")
	private DragonAbility ability;
	
	@SerializedName("chance")
	private float chance;
}
