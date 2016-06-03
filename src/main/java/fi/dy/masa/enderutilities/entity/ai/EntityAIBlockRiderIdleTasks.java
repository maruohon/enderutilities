package fi.dy.masa.enderutilities.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIBlockRiderIdleTasks extends EntityAIBase
{
    public final EntityLiving entity;

    public EntityAIBlockRiderIdleTasks(EntityLiving living)
    {
        this.entity = living;
    }

    @Override
    public boolean isInterruptible()
    {
        return false;
    }

    @Override
    public void startExecuting()
    {
        //System.out.printf("%s - startExecuting()\n", this.getClass().getSimpleName());
        this.setMutexBits(1); // Block most other AI tasks that try to move
    }

    @Override
    public void resetTask()
    {
        //System.out.printf("%s - resetTask()\n", this.getClass().getSimpleName());
        this.setMutexBits(0); // Don't block other AI tasks when not running
    }

    @Override
    public void updateTask()
    {
    }

    @Override
    public boolean shouldExecute()
    {
        //System.out.printf("%s - shouldExecute(): %s\n", this.getClass().getSimpleName(), (this.entity.isEntityAlive() && this.entity.isRiding()));
        //return this.entity.isEntityAlive() && EntityUtils.doesEntityStackHavePlayers(this.entity);
        return this.entity.isEntityAlive() && this.entity.isRiding();
    }
}
