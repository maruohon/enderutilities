package fi.dy.masa.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

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

    public PositionHelper(RayTraceResult rayTraceResult)
    {
        this.initPos(rayTraceResult, 0.0d, 0.0d, 0.0d);
    }

    public PositionHelper(RayTraceResult rayTraceResult, Entity entity)
    {
        if (entity != null)
        {
            this.initPos(rayTraceResult, entity.posX, entity.posY, entity.posZ);
        }
        else
        {
            this.initPos(rayTraceResult, 0.0d, 0.0d, 0.0d);
        }
    }

    public void initPos(RayTraceResult rayTraceResult, double x, double y, double z)
    {
        // Hit a block
        if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.hitVec != null)
        {
            this.posX = rayTraceResult.hitVec.xCoord;
            this.posY = rayTraceResult.hitVec.yCoord;
            this.posZ = rayTraceResult.hitVec.zCoord;
        }
        // Hit an entity
        else if (rayTraceResult.typeOfHit == RayTraceResult.Type.ENTITY)
        {
            if (rayTraceResult.hitVec != null)
            {
                this.posX = rayTraceResult.hitVec.xCoord;
                this.posY = rayTraceResult.hitVec.yCoord;
                this.posZ = rayTraceResult.hitVec.zCoord;
            }
            else if (rayTraceResult.entityHit != null)
            {
                this.posX = rayTraceResult.entityHit.posX;
                this.posY = rayTraceResult.entityHit.posY;
                this.posZ = rayTraceResult.entityHit.posZ;
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
    public void adjustPositionToTouchFace(Entity entity, EnumFacing facing)
    {
        this.posX += (facing.getFrontOffsetX() * entity.width / 2);
        this.posZ += (facing.getFrontOffsetZ() * entity.width / 2);

        // Bottom side
        if (facing.equals(EnumFacing.DOWN) == true)
        {
            this.posY -= entity.height;
        }
    }
}
