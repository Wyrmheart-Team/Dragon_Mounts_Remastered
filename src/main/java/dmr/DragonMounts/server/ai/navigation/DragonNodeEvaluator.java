package dmr.DragonMounts.server.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;

import javax.annotation.Nullable;


public class DragonNodeEvaluator extends FlyNodeEvaluator {
	private final Mob mob;
	public boolean allowFlying = false;
	public boolean allowSwimming = false;
	
	public DragonNodeEvaluator(Mob mob)
	{
		this.mob = mob;
	}
	
	@Override
	public Node getStart()
	{
		if (allowSwimming) {
			return !this.mob.isInWater() ? super.getStart() : this.getStartNode(new BlockPos(Mth.floor(this.mob.getBoundingBox().minX),
					Mth.floor(this.mob.getBoundingBox().minY + 0.5),
					Mth.floor(this.mob.getBoundingBox().minZ)
			));
		} else {
			return super.getStart();
		}
	}
	
	@Override
	protected boolean isAmphibious()
	{
		return !mob.canDrownInFluidType(Fluids.WATER.getFluidType()) || allowSwimming;
	}
	
	@Override
	public int getNeighbors(Node[] outputArray, Node p_node)
	{
		if (allowFlying) {
			return super.getNeighbors(outputArray, p_node);
		}
		
		if (this.allowSwimming) {
			int i = super.getNeighbors(outputArray, p_node); PathType pathtype = this.getCachedPathType(p_node.x, p_node.y + 1, p_node.z);
			PathType pathtype1 = this.getCachedPathType(p_node.x, p_node.y, p_node.z); int j;
			if (this.mob.getPathfindingMalus(pathtype) >= 0.0F && pathtype1 != PathType.STICKY_HONEY) {
				j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
			} else {
				j = 0;
			}
			
			double d0 = this.getFloorLevel(new BlockPos(p_node.x, p_node.y, p_node.z));
			Node node = this.findAcceptedNode(p_node.x, p_node.y + 1, p_node.z, Math.max(0, j - 1), d0, Direction.UP, pathtype1);
			Node node1 = this.findAcceptedNode(p_node.x, p_node.y - 1, p_node.z, j, d0, Direction.DOWN, pathtype1); if (this.isVerticalNeighborValid(node, p_node)) {
				outputArray[i++] = node;
			}
			
			if (this.isVerticalNeighborValid(node1, p_node) && pathtype1 != PathType.TRAPDOOR) {
				outputArray[i++] = node1;
			}
			
			return i;
		}
		
		int i = 0; int j = 0; PathType pathtype = this.getCachedPathType(p_node.x, p_node.y + 1, p_node.z); PathType pathtype1 = this.getCachedPathType(p_node.x, p_node.y, p_node.z);
		if (this.mob.getPathfindingMalus(pathtype) >= 0.0F && pathtype1 != PathType.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
		}
		
		double d0 = this.getFloorLevel(new BlockPos(p_node.x, p_node.y, p_node.z));
		
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Node node = this.findAcceptedNode(p_node.x + direction.getStepX(), p_node.y, p_node.z + direction.getStepZ(), j, d0, direction, pathtype1);
			this.reusableNeighbors[direction.get2DDataValue()] = node; if (this.isNeighborValid(node, p_node)) {
				outputArray[i++] = node;
			}
		}
		
		for (Direction direction1 : Direction.Plane.HORIZONTAL) {
			Direction direction2 = direction1.getClockWise();
			if (this.isDiagonalValid(p_node, this.reusableNeighbors[direction1.get2DDataValue()], this.reusableNeighbors[direction2.get2DDataValue()])) {
				Node node1 = this.findAcceptedNode(p_node.x + direction1.getStepX() + direction2.getStepX(),
						p_node.y,
						p_node.z + direction1.getStepZ() + direction2.getStepZ(),
						j,
						d0,
						direction1,
						pathtype1
				); if (this.isDiagonalValid(node1)) {
					outputArray[i++] = node1;
				}
			}
		}
		
		return i;
	}
	
	private boolean isVerticalNeighborValid(
			@Nullable Node neighbor, Node node)
	{
		return this.isNeighborValid(neighbor, node) && neighbor.type == PathType.WATER;
	}
	
	@Override
	public PathType getPathType(PathfindingContext context, int x, int y, int z)
	{
		if (allowSwimming) {
			PathType pathtype = context.getPathTypeFromState(x, y, z); if (pathtype == PathType.WATER) {
				BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
				
				for (Direction direction : Direction.values()) {
					blockpos$mutableblockpos.set(x, y, z).move(direction);
					PathType pathtype1 = context.getPathTypeFromState(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ());
					if (pathtype1 == PathType.BLOCKED) {
						return PathType.WATER_BORDER;
					}
				}
				
				return PathType.WATER;
			} else {
				return super.getPathType(context, x, y, z);
			}
		} else {
			return super.getPathType(context, x, y, z);
		}
	}
}
