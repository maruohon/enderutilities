package fi.dy.masa.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class PositionHelper
{
	public double posX;
	public double posY;
	public double posZ;

	public PositionHelper(MovingObjectPosition mop)
	{
		this.initPos(mop, 0.0d, 0.0d, 0.0d);
	}

	public PositionHelper(MovingObjectPosition mop, Entity entity)
	{
		if (entity != null)
		{
			this.initPos(mop, entity.posX, entity.posY, entity.posZ);
		}
		else
		{
			this.initPos(mop, 0.0d, 0.0d, 0.0d);
		}
	}

	public void initPos(MovingObjectPosition mop, double x, double y, double z)
	{
		// Hit a block
		if (mop.typeOfHit == MovingObjectType.BLOCK && mop.hitVec != null)
		{
			this.posX = mop.hitVec.xCoord;
			this.posY = mop.hitVec.yCoord;
			this.posZ = mop.hitVec.zCoord;
		}
		// Hit an entity
		else if (mop.typeOfHit == MovingObjectType.ENTITY)
		{
			if (mop.hitVec != null)
			{
				this.posX = mop.hitVec.xCoord;
				this.posY = mop.hitVec.yCoord;
				this.posZ = mop.hitVec.zCoord;
			}
			else if (mop.entityHit != null)
			{
				this.posX = mop.entityHit.posX;
				this.posY = mop.entityHit.posY;
				this.posZ = mop.entityHit.posZ;
			}
			else
			{
				this.posX = x;
				this.posY = y;
				this.posZ = z;
			}
		}
		else
		{
			this.posX = x;
			this.posY = y;
			this.posZ = z;
		}
	}
}
