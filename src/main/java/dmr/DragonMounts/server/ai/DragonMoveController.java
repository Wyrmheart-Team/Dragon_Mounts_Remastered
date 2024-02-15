package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DragonMoveController extends MoveControl
{
    private final DMRDragonEntity dragon;

    public DragonMoveController(DMRDragonEntity dragon)
    {
        super(dragon);
        this.dragon = dragon;
        this.speedModifier = 4f;
    }
    
    @Override
    public void tick()
    {
//        System.out.println("Operation: " + this.operation);
        
        // original movement behavior if the entity isn't flying
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            double xDif = this.wantedX - this.mob.getX();
            double zDif = this.wantedZ - this.mob.getZ();
            double yDif = this.wantedY - this.mob.getY();
            
            double sq = xDif * xDif + yDif * yDif + zDif * zDif;
            if (sq < (double)2.5000003E-7F) {
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
                return;
            }
            
            BlockPos blockpos = this.mob.blockPosition();
            BlockState blockstate = this.mob.level.getBlockState(blockpos);
            
            var shouldFly = !blockstate.isSolid();
            dragon.setNoGravity(shouldFly);
            dragon.setFlying(shouldFly);
            
            float yaw = (float)(Mth.atan2(zDif, xDif) * (double)(180F / (float)Math.PI)) - 90.0F;
            float angleDifference = Math.abs(yaw - this.mob.getYRot());
            
            if (angleDifference > 15.0F || (shouldFly || dragon.isFlying())) {  // 5.0F is the deadzone angle
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yaw, shouldFly || dragon.isFlying() ? 30f : 10.0F));
            }
            
            float speed;
            
            if (this.mob.onGround()) {
                speed = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                speed = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
            }
            
            this.mob.setSpeed(speed);
            
            double d4 = Math.sqrt(xDif * xDif + zDif * zDif);
            if (Math.abs(yDif) > 1.0E-5F || Math.abs(d4) > 1.0E-5F) {
                this.mob.setYya(yDif > 0.0 ? speed : -speed);
            }
        }else if(this.operation == Operation.WAIT){
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
            this.mob.setXxa(0.0F);
        } else{
            super.tick();
        }
    }
}