package fi.dy.masa.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;

public class PositionHelper
{
	public double posX;
	public double posY;
	public double posZ;

	public PositionHelper(MovingObjectPosition mop)
	{
		this(mop, 0.0d, 0.0d, 0.0d);
	}

	public PositionHelper(MovingObjectPosition mop, Entity e)
	{
		this(mop, e.posX, e.posY, e.posZ);
	}

	public PositionHelper(MovingObjectPosition mop, double x, double y, double z)
	{
		// Hit an entity
		if (mop.entityHit != null)
		{
			this.posX = mop.entityHit.posX;
			this.posY = mop.entityHit.posY;
			this.posZ = mop.entityHit.posZ;
		}
		// Hit a block
		else if (mop.hitVec != null)
		{
			this.posX = mop.hitVec.xCoord;
			this.posY = mop.hitVec.yCoord;
			this.posZ = mop.hitVec.zCoord;
		}
		else
		{
			this.posX = x;
			this.posY = y;
			this.posZ = z;
		}
	}
}
