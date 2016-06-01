package fi.dy.masa.enderutilities.event.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        while (taskIter.hasNext() == true)
        {
            boolean finished = false;
            IPlayerTask task = taskIter.next();
            Timer timer = timerIter.next();

            if (timer.tick() == true)
            {
                if (task.canExecute(world, player) == true)
                {
                    finished = task.execute(world, player);
                }
            }

            if (finished == true)
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

        while (taskIter.hasNext() == true)
        {
            IPlayerTask taskTmp = taskIter.next();

            if (clazz.equals(taskTmp.getClass()) == true)
            {
                return true;
            }
        }

        return false;
    }

    public void removeTask(EntityPlayer player, Class <? extends IPlayerTask> clazz)
    {
        List<IPlayerTask> playerTasks = this.tasks.get(player.getUniqueID());
        if (playerTasks == null)
        {
            return;
        }

        Iterator<IPlayerTask> taskIter = playerTasks.iterator();
        Iterator<Timer> timerIter = this.timers.get(player.getUniqueID()).iterator();

        while (taskIter.hasNext() == true)
        {
            IPlayerTask taskTmp = taskIter.next();
            timerIter.next();

            if (clazz.equals(taskTmp.getClass()) == true)
            {
                taskTmp.stop();
                taskIter.remove();
                timerIter.remove();
            }
        }

        if (playerTasks.isEmpty() == true)
        {
            this.tasks.remove(player.getUniqueID());
            this.timers.remove(player.getUniqueID());
        }
    }

    public void clearTasks()
    {
    }
}
