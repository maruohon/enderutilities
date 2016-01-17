package fi.dy.masa.enderutilities.event.tasks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IPlayerTask
{
    /**
     * Initialize the task. Called when the task is added to the task list.
     */
    public void init();

    /**
     * Return whether this task can be executed.
     * @return true if the task can be executed
     */
    public boolean canExecute(World world, EntityPlayer player);

    /**
     * Execute the task. Return true to indicate that this task has finished.
     * @return true to indicate the task has finished and can be removed
     */
    public boolean execute(World world, EntityPlayer player);

    /**
     * Stop the task. This is also called when the tasks are removed.
     */
    public void stop();
}
