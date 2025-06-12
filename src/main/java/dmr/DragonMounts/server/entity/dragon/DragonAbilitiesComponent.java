package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.DragonAbilityEntry;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.util.MiscUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

abstract class DragonAbilitiesComponent extends DragonAttributeComponent {
    protected DragonAbilitiesComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Getter
    private final List<Ability> abilities = new ArrayList<>();
    
    
    @Override
    public void finalizeDragon(@Nullable TameableDragonEntity parent1, @Nullable TameableDragonEntity parent2) {
        super.finalizeDragon(parent1, parent2);
        
        var abilities = new ArrayList<Ability>();
        int maxAbilities = MiscUtils.randomUpperLower(1, 6); //TODO The upper limit should be a dragon trait
        var possibilities = new ArrayList<>(getBreed().getAbilities());
        
        Consumer<TameableDragonEntity> addParentAbilities = (parent) -> {
            var parentAbilities = parent.getAbilities();
            @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
            var abilityEntries = new ArrayList<DragonAbilityEntry>(); //TODO Get entries from ability list
            
            //noinspection RedundantOperationOnEmptyContainer
            possibilities.addAll(abilityEntries.stream().filter(s -> s.getAbility().isBreedTransferable()).toList());
        };
        
        if(parent1 != null) addParentAbilities.accept(parent1);
        if(parent2 != null) addParentAbilities.accept(parent2);
        
        Collections.shuffle(possibilities);
        
        for (DragonAbilityEntry dragonAbilityEntry : possibilities) {
            if(abilities.size() >= maxAbilities) break;
            
            if(Math.random() >= dragonAbilityEntry.getChance()) {
                var drAbility = dragonAbilityEntry.getAbility();
                
                //TODO: Get ability instance from ability entry
                abilities.add(null);
            }
        }
        
        this.abilities.addAll(abilities.subList(0, Math.min(abilities.size(), maxAbilities)));
        //TODO Do ability init, cant be done in finalize as some changes such as pathfindingMalus isnt stored in nbt,
        // So ability init needs to be done on each load
    }
    
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        
        //TODO Load abilities
        abilities.forEach(ability -> ability.initialize(getDragon()));
    }

    @Override
    protected void onChangedBlock(ServerLevel level, BlockPos pos) {
        super.onChangedBlock(level, pos);
        abilities.forEach(ability -> ability.onMove(getDragon()));
    }

    public void tick() {
        super.tick();
        if (tickCount % 20 == 0) abilities.forEach(ability -> ability.tick(getDragon()));
    }
}
