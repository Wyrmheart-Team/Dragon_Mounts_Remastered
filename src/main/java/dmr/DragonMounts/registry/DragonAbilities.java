package dmr.DragonMounts.registry;

import com.mojang.serialization.MapCodec;
import dmr.DragonMounts.types.abilities.dragon_types.aether_dragon.QuickFlight;
import dmr.DragonMounts.types.abilities.dragon_types.amethyst_dragon.CrystalHarmonyAbility;
import dmr.DragonMounts.types.abilities.dragon_types.amethyst_dragon.GemGuardAbility;
import dmr.DragonMounts.types.abilities.dragon_types.end_dragon.EnderCloakAbility;
import dmr.DragonMounts.types.abilities.dragon_types.end_dragon.VoidWalker;
import dmr.DragonMounts.types.abilities.dragon_types.fire_dragon.EmberAuraAbility;
import dmr.DragonMounts.types.abilities.dragon_types.fire_dragon.FireProofAbility;
import dmr.DragonMounts.types.abilities.dragon_types.fire_dragon.HotFeetAbility;
import dmr.DragonMounts.types.abilities.dragon_types.forest_dragon.CamouflageAbility;
import dmr.DragonMounts.types.abilities.dragon_types.forest_dragon.NatureBlessingAbility;
import dmr.DragonMounts.types.abilities.dragon_types.ghost_dragon.EtherealHarmonyAbility;
import dmr.DragonMounts.types.abilities.dragon_types.ice_dragon.FrostAuraAbility;
import dmr.DragonMounts.types.abilities.dragon_types.ice_dragon.FrostWalkerAbility;
import dmr.DragonMounts.types.abilities.dragon_types.lush_dragon.FloralTrailAbility;
import dmr.DragonMounts.types.abilities.dragon_types.nether_dragon.InfernalPactAbility;
import dmr.DragonMounts.types.abilities.dragon_types.skulk_dragon.EchoSenseAbility;
import dmr.DragonMounts.types.abilities.dragon_types.water_dragon.AquaticGraceAbility;
import dmr.DragonMounts.types.abilities.dragon_types.water_dragon.SwiftSwimAbility;
import dmr.DragonMounts.types.abilities.types.Ability;
import java.util.HashMap;
import java.util.Map;

public class DragonAbilities {

	public static final Map<String, MapCodec<? extends Ability>> REGISTRY = new HashMap<>();

	public static final FireProofAbility FIRE_PROOF = register(new FireProofAbility());
	public static final HotFeetAbility HOT_FEET = register(new HotFeetAbility());
	public static final EmberAuraAbility EMBER_AURA = register(new EmberAuraAbility());

	public static final InfernalPactAbility INFERNAL_PACT_ABILITY = register(new InfernalPactAbility());

	public static final FrostWalkerAbility FROST_WALKER = register(new FrostWalkerAbility());
	public static final FrostAuraAbility FROST_AURA = register(new FrostAuraAbility());

	public static final NatureBlessingAbility HEALING_ABILITY = register(new NatureBlessingAbility());
	public static final CamouflageAbility CAMOUFLAGE_ABILITY = register(new CamouflageAbility());

	public static final AquaticGraceAbility AQUATIC_GRACE_ABILITY = register(new AquaticGraceAbility());
	public static final SwiftSwimAbility SWIFT_SWIM = register(new SwiftSwimAbility());

	public static final FloralTrailAbility FLORAL_TRAIL = register(new FloralTrailAbility());

	public static final EnderCloakAbility ENDER_CLOAK = register(new EnderCloakAbility());
	public static final VoidWalker VOID_WALKER = register(new VoidWalker());

	public static final GemGuardAbility GEM_GUARD = register(new GemGuardAbility());
	public static final CrystalHarmonyAbility CRYSTAL_HARMONY = register(new CrystalHarmonyAbility());

	public static final EchoSenseAbility ECHO_SENSE = register(new EchoSenseAbility());

	public static final EtherealHarmonyAbility ETHEREAL_HARMONY = register(new EtherealHarmonyAbility());

	public static final QuickFlight QUICK_FLIGHT = register(new QuickFlight());

	public static <T extends Ability> T register(T ability) {
		register(ability.type(), MapCodec.unit(ability));
		return ability;
	}

	public static String register(String name, MapCodec<? extends Ability> codec) {
		REGISTRY.put(name, codec);
		return name;
	}
}
