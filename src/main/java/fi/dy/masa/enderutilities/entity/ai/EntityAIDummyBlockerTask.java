package fi.dy.masa.enderutilities.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIDummyBlockerTask extends EntityAIBase
{
    private final EntityLiving entity;
    private final int mutexBitsWhenActive;

    public EntityAIDummyBlockerTask(EntityLiving living, int mutexBits)
    {
        this.entity = living;
        this.mutexBitsWhenActive = mutexBits;
    }

    @Override
    public boolean isInterruptible()
    {
        return false;
    }

    @Override
    public void startExecuting()
    {
        //System.out.printf("%s - startExecuting() for %s\n", this.getClass().getSimpleName(), this.entity);
        this.setMutexBits(this.mutexBitsWhenActive);
    }

    @Override
    public void resetTask()
    {
        //System.out.printf("%s - resetTask() for %s\n", this.getClass().getSimpleName(), this.entity);
        this.setMutexBits(0); // Don't block other AI tasks when not running
    }

    @Override
    public void updateTask()
    {
    }

    @Override
    public boolean shouldExecute()
    {
        return this.entity.isEntityAlive();
    }
}
