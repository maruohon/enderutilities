package fi.dy.masa.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class PositionHelper
{
    public double posX;
    public double posY;
    public double posZ;

    public PositionHelper(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public PositionHelper(Entity entity)
    {
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
    }

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

    /**
     * Adjust the position so that the given Entity's bounding box is against the block bound
     * of a block on the given side. Note that the entity may still be colliding on other sides.
     * @param entity
     * @param side
     */
    public void adjustPositionToTouchFace(Entity entity, int side)
    {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        this.posX += (dir.offsetX * entity.width / 2);
        this.posZ += (dir.offsetZ * entity.width / 2);

        // Bottom side
        if (dir.equals(ForgeDirection.DOWN))
        {
            this.posY -= entity.height;
        }
    }
}
