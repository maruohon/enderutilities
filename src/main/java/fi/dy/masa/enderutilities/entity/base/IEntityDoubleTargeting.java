package fi.dy.masa.enderutilities.entity.base;

import net.minecraft.entity.EntityLivingBase;

public interface IEntityDoubleTargeting
{
    EntityLivingBase getPrimaryTarget();

    EntityLivingBase getSecondaryTarget();

    void setPrimaryTarget(EntityLivingBase livingBase);

    void setSecondaryTarget(EntityLivingBase livingBase);

    /**
     * Set which target is the active one. true = primary, false = secondary.
     * @param primaryIsActive
     */
    void setActiveTarget(boolean primaryIsActive);

    /**
     * Get which target type is currently active, the primary or the secondary.
     * @return true = primary target is currently active
     */
    boolean getPrimaryTargetIsActive();

    /**
     * Returns the currently active target Entity, or null if no targets exist.
     * @return currently active target, or null if none
     */
    EntityLivingBase getActiveTargetEntity();
}
