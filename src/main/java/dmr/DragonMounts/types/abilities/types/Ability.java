package dmr.DragonMounts.types.abilities.types;

import com.mojang.serialization.Codec;
import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface Ability {
	Codec<Ability> CODEC = Codec.STRING.dispatch(Ability::type, DragonAbilities.REGISTRY::get);
	String type();
	
	default Component getTranslatedName() {return Component.translatable("dmr.ability." + type() + ".name");}
	default Component getTranslatedDescription() {return Component.translatable("dmr.ability." + type() + ".description");}
	
	default void initialize(DMRDragonEntity dragon) {}
	default void close(DMRDragonEntity dragon) {}
	default void tick(DMRDragonEntity dragon) {}
	default void onMove(DMRDragonEntity dragon) {}
	
	default Map<ResourceLocation, Double> getAttributes() {return null;}
}
