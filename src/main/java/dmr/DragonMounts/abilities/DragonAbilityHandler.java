package dmr.DragonMounts.abilities;

import com.mojang.logging.LogUtils;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.abilities.scripting.LuaFunctions;
import dmr.DragonMounts.registry.DragonAbilityRegistry;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.Optional;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class DragonAbilityHandler {

	public static void initAbilities(DMRDragonEntity dragon) {
		var breed = dragon.getBreed();

		for (String ability : breed.getAbilities()) {
			DragonAbility dragonAbility = DragonAbilityRegistry.getDragonAbility(ability);

			if (DragonAbilityRegistry.hasScript(ability)) {
				DragonAbilityRegistry.callScript(ability, LuaFunctions.init, dragon);
			} else if (dragonAbility != null && dragonAbility.getCodeAbility() != null) {
				dragonAbility.getCodeAbility().initialize(dragon);
			} else if (dragonAbility == null) {
				LogUtils.getLogger().error("Ability {} not found, Unable to init!", ability);
			}
		}
	}

	public static void closeAbilities(DMRDragonEntity dragon) {
		var breed = dragon.getBreed();

		for (String ability : breed.getAbilities()) {
			DragonAbility dragonAbility = DragonAbilityRegistry.getDragonAbility(ability);

			if (DragonAbilityRegistry.hasScript(ability)) {
				DragonAbilityRegistry.callScript(ability, LuaFunctions.close, dragon);
			} else if (dragonAbility != null && dragonAbility.getCodeAbility() != null) {
				dragonAbility.getCodeAbility().close(dragon);
			} else if (dragonAbility == null) {
				LogUtils.getLogger().error("Ability {} not found, Unable to close!", ability);
			}
		}
	}

	public static void tickAbilities(DMRDragonEntity dragon) {
		var breed = dragon.getBreed();

		for (String ability : breed.getAbilities()) {
			DragonAbility dragonAbility = DragonAbilityRegistry.getDragonAbility(ability);

			if (DragonAbilityRegistry.hasScript(ability)) {
				DragonAbilityRegistry.callScript(ability, LuaFunctions.onTick, dragon);
			} else if (dragonAbility != null && dragonAbility.getCodeAbility() != null) {
				dragonAbility.getCodeAbility().tick(dragon);
			} else if (dragonAbility == null) {
				LogUtils.getLogger().error("Ability {} not found, Unable to tick!", ability);
			}
		}
	}

	public static void onMove(DMRDragonEntity dragon) {
		var breed = dragon.getBreed();

		for (String ability : breed.getAbilities()) {
			DragonAbility dragonAbility = DragonAbilityRegistry.getDragonAbility(ability);

			if (DragonAbilityRegistry.hasScript(ability)) {
				DragonAbilityRegistry.callScript(ability, LuaFunctions.onMove, dragon);
			} else if (dragonAbility != null && dragonAbility.getCodeAbility() != null) {
				dragonAbility.getCodeAbility().onMove(dragon);
			} else if (dragonAbility == null) {
				LogUtils.getLogger().error("Ability {} not found, Unable to onMove!", ability);
			}
		}
	}

	public static void applyAttributes(DMRDragonEntity dragon) {
		var breed = dragon.getBreed();

		for (String ability : breed.getAbilities()) {
			if (DragonAbilityRegistry.hasDragonAbility(ability)) {
				DragonAbilityRegistry.getDragonAbility(ability)
					.getAttributes()
					.forEach((att, value) -> {
						var key = BuiltInRegistries.ATTRIBUTE.getKey(att);
						Optional<Reference<Attribute>> attr = BuiltInRegistries.ATTRIBUTE.getHolder(key);
						if (attr.isPresent()) {
							AttributeInstance inst = dragon.getAttribute(attr.get());
							if (inst != null) {
								inst.addPermanentModifier(
									new AttributeModifier(
										ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, ability),
										value,
										Operation.ADD_VALUE
									)
								);
							}
						}
					});
			}
		}
	}
}
