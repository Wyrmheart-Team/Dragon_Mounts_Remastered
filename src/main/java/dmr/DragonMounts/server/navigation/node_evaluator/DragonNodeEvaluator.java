package dmr.DragonMounts.server.navigation.node_evaluator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;


public class DragonNodeEvaluator extends FlyNodeEvaluator
{
	private final Mob mob;
	
	public DragonNodeEvaluator(Mob mob)
	{
		this.mob = mob;
	}
	
	@Override
	protected boolean isAmphibious()
	{
		return !mob.canDrownInFluidType(Fluids.WATER.getFluidType());
	}
	
	@Override
	public int getNeighbors(Node[] outputArray, Node p_node) {
		int i = 0;
		int j = 0;
		PathType pathtype = this.getCachedPathType(p_node.x, p_node.y + 1, p_node.z);
		PathType pathtype1 = this.getCachedPathType(p_node.x, p_node.y, p_node.z);
		if (this.mob.getPathfindingMalus(pathtype) >= 0.0F && pathtype1 != PathType.STICKY_HONEY) {
			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
		}
		
		double d0 = this.getFloorLevel(new BlockPos(p_node.x, p_node.y, p_node.z));
		
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Node node = this.findAcceptedNode(p_node.x + direction.getStepX(), p_node.y, p_node.z + direction.getStepZ(), j, d0, direction, pathtype1);
			this.reusableNeighbors[direction.get2DDataValue()] = node;
			if (this.isNeighborValid(node, p_node)) {
				outputArray[i++] = node;
			}
		}
		
		for (Direction direction1 : Direction.Plane.HORIZONTAL) {
			Direction direction2 = direction1.getClockWise();
			if (this.isDiagonalValid(p_node, this.reusableNeighbors[direction1.get2DDataValue()], this.reusableNeighbors[direction2.get2DDataValue()])) {
				Node node1 = this.findAcceptedNode(
						p_node.x + direction1.getStepX() + direction2.getStepX(),
						p_node.y,
						p_node.z + direction1.getStepZ() + direction2.getStepZ(),
						j,
						d0,
						direction1,
						pathtype1
				);
				if (this.isDiagonalValid(node1)) {
					outputArray[i++] = node1;
				}
			}
		}
		
		Node[] neighbors = new Node[32];
		var num = super.getNeighbors(neighbors, p_node);
		var curLeftCount = outputArray.length - i;

		for(int k = 0; k < Math.min(num, curLeftCount-1); k++) {
			if(neighbors[k] == null) break;

			neighbors[k].costMalus = Math.max(neighbors[k].costMalus, 1) * 1.5f; //Make flying more expensive
			outputArray[i++] = neighbors[k];
		}
	
		return i;
	}
	
//	@Override
//	public int getNeighbors(Node[] pOutputArray, Node pNode) {
//		int i = 0;
//		int j = 0;
//		PathType blockpathtypes = this.getCachedPathType(pNode.x, pNode.y + 1, pNode.z);
//		PathType blockpathtypes1 = this.getCachedPathType(pNode.x, pNode.y, pNode.z);
//		if (this.mob.getPathfindingMalus(blockpathtypes) >= 0.0F && blockpathtypes1 != PathType.STICKY_HONEY) {
//			j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
//		}
//
//		double d0 = this.getFloorLevel(new BlockPos(pNode.x, pNode.y, pNode.z));
//		Node node = this.findAcceptedNode(pNode.x, pNode.y, pNode.z + 1, j, d0, Direction.SOUTH, blockpathtypes1);
//		if (this.isNeighborValid(node, pNode)) {
//			pOutputArray[i++] = node;
//		}
//
//		Node node1 = this.findAcceptedNode(pNode.x - 1, pNode.y, pNode.z, j, d0, Direction.WEST, blockpathtypes1);
//		if (this.isNeighborValid(node1, pNode)) {
//			pOutputArray[i++] = node1;
//		}
//
//		Node node2 = this.findAcceptedNode(pNode.x + 1, pNode.y, pNode.z, j, d0, Direction.EAST, blockpathtypes1);
//		if (this.isNeighborValid(node2, pNode)) {
//			pOutputArray[i++] = node2;
//		}
//
//		Node node3 = this.findAcceptedNode(pNode.x, pNode.y, pNode.z - 1, j, d0, Direction.NORTH, blockpathtypes1);
//		if (this.isNeighborValid(node3, pNode)) {
//			pOutputArray[i++] = node3;
//		}
//
//
//		Node node4 = this.findAcceptedNode(pNode.x - 1, pNode.y, pNode.z - 1, j, d0, Direction.NORTH, blockpathtypes1);
//		if (this.isDiagonalValid(pNode, node1, node3, node4)) {
//			pOutputArray[i++] = node4;
//		}
//
//		Node node5 = this.findAcceptedNode(pNode.x + 1, pNode.y, pNode.z - 1, j, d0, Direction.NORTH, blockpathtypes1);
//		if (this.isDiagonalValid(pNode, node2, node3, node5)) {
//			pOutputArray[i++] = node5;
//		}
//
//		Node node6 = this.findAcceptedNode(pNode.x - 1, pNode.y, pNode.z + 1, j, d0, Direction.SOUTH, blockpathtypes1);
//		if (this.isDiagonalValid(pNode, node1, node, node6)) {
//			pOutputArray[i++] = node6;
//		}
//
//		Node node7 = this.findAcceptedNode(pNode.x + 1, pNode.y, pNode.z + 1, j, d0, Direction.SOUTH, blockpathtypes1);
//		if (this.isDiagonalValid(pNode, node2, node, node7)) {
//			pOutputArray[i++] = node7;
//		}
//
//		Node[] neighbors = new Node[32];
//		var num = super.getNeighbors(neighbors, pNode);
//		var curLeftCount = pOutputArray.length - i;
//
//		for(int k = 0; k < Math.min(num, curLeftCount-1); k++) {
//			if(neighbors[k] == null) break;
//
//			neighbors[k].costMalus = Math.max(neighbors[k].costMalus, 1) * 1.5f; //Make flying more expensive
//			pOutputArray[i++] = neighbors[k];
//		}
//
//		return i;
//	}
}
