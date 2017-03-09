package fi.dy.masa.enderutilities.event.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.event.tasks.TaskScheduler.Timer;

public class PlayerTaskScheduler
{
    protected static PlayerTaskScheduler instance;
    protected Map<UUID, List<IPlayerTask>> tasks;
    protected Map<UUID, List<Timer>> timers;

    protected PlayerTaskScheduler()
    {
        this.tasks = new HashMap<UUID, List<IPlayerTask>>();
        this.timers = new HashMap<UUID, List<Timer>>();
    }

    public static PlayerTaskScheduler getInstance()
    {
        if (instance == null)
        {
            instance = new PlayerTaskScheduler();
        }

        return instance;
    }

    public void runTasks(World world, EntityPlayer player)
    {
        List<IPlayerTask> playerTasks = this.tasks.get(player.getUniqueID());

        if (playerTasks == null)
        {
            return;
        }

        Iterator<IPlayerTask> taskIter = playerTasks.iterator();
        Iterator<Timer> timerIter = this.timers.get(player.getUniqueID()).iterator();

        while (taskIter.hasNext())
        {
            boolean finished = false;
            IPlayerTask task = taskIter.next();
            Timer timer = timerIter.next();

            if (timer.tick())
            {
                if (task.canExecute(world, player))
                {
                    finished = task.execute(world, player);
                }
                else
                {
                    finished = true;
                }
            }

            if (finished)
            {
                task.stop();
                taskIter.remove();
                timerIter.remove();
            }
        }
    }

    public void addTask(EntityPlayer player, IPlayerTask task, int interval)
    {
        task.init();

        UUID uuid = player.getUniqueID();
        List<IPlayerTask> playerTasks = this.tasks.get(uuid);
        List<Timer> timers = this.timers.get(uuid);

        if (playerTasks == null)
        {
            playerTasks = new ArrayList<IPlayerTask>();
            timers = new ArrayList<Timer>();
            this.tasks.put(uuid, playerTasks);
            this.timers.put(uuid, timers);
        }

        playerTasks.add(task);
        timers.add(new Timer(interval));
    }

    public boolean hasTask(EntityPlayer player, Class <? extends IPlayerTask> clazz)
    {
        List<IPlayerTask> playerTasks = this.tasks.get(player.getUniqueID());

        if (playerTasks == null)
        {
            return false;
        }

        Iterator<IPlayerTask> taskIter = playerTasks.iterator();

        while (taskIter.hasNext())
        {
            IPlayerTask taskTmp = taskIter.next();

            if (clazz.equals(taskTmp.getClass()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove all tasks matchin <b>clazz</b> from player <b>player</b>.
     * If <b>clazz</b> is null, then all tasks from <b>player</b> are removed.
     * @param player
     * @param clazz
     */
    public void removeTask(EntityPlayer player, @Nullable Class <? extends IPlayerTask> clazz)
    {
        List<IPlayerTask> playerTasks = this.tasks.get(player.getUniqueID());

        if (playerTasks == null)
        {
            return;
        }

        Iterator<IPlayerTask> taskIter = playerTasks.iterator();
        Iterator<Timer> timerIter = this.timers.get(player.getUniqueID()).iterator();

        while (taskIter.hasNext())
        {
            IPlayerTask taskTmp = taskIter.next();
            timerIter.next();

            if (clazz == null || clazz.equals(taskTmp.getClass()))
            {
                taskTmp.stop();
                taskIter.remove();
                timerIter.remove();
            }
        }

        if (playerTasks.isEmpty())
        {
            this.tasks.remove(player.getUniqueID());
            this.timers.remove(player.getUniqueID());
        }
    }

    public void clearTasks()
    {
    }
}
