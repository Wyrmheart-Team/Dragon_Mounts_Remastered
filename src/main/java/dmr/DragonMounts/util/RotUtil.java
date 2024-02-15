package dmr.DragonMounts.util;

import net.minecraft.util.Mth;

public class RotUtil
{
	//MoveController.java - 1.19.2
	public static float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
		float f = Mth.wrapDegrees(pTargetAngle - pSourceAngle);
		if (f > pMaximumChange) {
			f = pMaximumChange;
		}
		
		if (f < -pMaximumChange) {
			f = -pMaximumChange;
		}
		
		float f1 = pSourceAngle + f;
		if (f1 < 0.0F) {
			f1 += 360.0F;
		} else if (f1 > 360.0F) {
			f1 -= 360.0F;
		}
		
		return f1;
	}
}
