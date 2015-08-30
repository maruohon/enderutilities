package fi.dy.masa.enderutilities.entity;

import net.minecraft.entity.EntityLivingBase;

public interface IEntityDoubleTargeting
{
    public EntityLivingBase getPrimaryTarget();

    public EntityLivingBase getSecondaryTarget();

    public void setPrimaryTarget(EntityLivingBase livingBase);

    public void setSecondaryTarget(EntityLivingBase livingBase);

    /**
     * Set which target is the active one. true = primary, false = secondary.
     * @param primaryIsActive
     */
    public void setActiveTarget(boolean primaryIsActive);

    /**
     * Get which target type is currently active, the primary or the secondary.
     * @return true = primary target is currently active
     */
    public boolean getPrimaryTargetIsActive();

    /**
     * Returns the currently active target Entity, or null if no targets exist.
     * @return currently active target, or null if none
     */
    public EntityLivingBase getActiveTargetEntity();
}
