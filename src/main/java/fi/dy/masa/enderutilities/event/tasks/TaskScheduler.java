package fi.dy.masa.enderutilities.event.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskScheduler
{
    protected static TaskScheduler instance;
    protected List<ITask> tasks;
    protected List<Timer> timers;

    protected TaskScheduler()
    {
        this.tasks = new ArrayList<ITask>();
        this.timers = new ArrayList<Timer>();
    }

    public static TaskScheduler getInstance()
    {
        if (instance == null)
        {
            instance = new TaskScheduler();
        }

        return instance;
    }

    public void runTasks()
    {
        Iterator<ITask> taskIter = this.tasks.iterator();
        Iterator<Timer> timerIter = this.timers.iterator();

        while (taskIter.hasNext() == true)
        {
            boolean finished = false;
            ITask task = taskIter.next();
            Timer timer = timerIter.next();

            if (timer.tick() == true)
            {
                if (task.canExecute() == true)
                {
                    finished = task.execute();
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

    public void addTask(ITask task, int interval)
    {
        task.init();

        this.tasks.add(task);
        this.timers.add(new Timer(interval));
    }

    public boolean hasTask(Class <? extends ITask> clazz)
    {
        for (ITask task : this.tasks)
        {
            if (clazz.equals(task.getClass()) == true)
            {
                return true;
            }
        }

        return false;
    }

    public void removeTask(Class <? extends ITask> clazz)
    {
        Iterator<ITask> taskIter = this.tasks.iterator();
        Iterator<Timer> timerIter = this.timers.iterator();

        while (taskIter.hasNext() == true)
        {
            ITask task = taskIter.next();
            timerIter.next();

            if (clazz.equals(task.getClass()) == true)
            {
                task.stop();
                taskIter.remove();
                timerIter.remove();
            }
        }
    }

    public void clearTasks()
    {
    }

    public static class Timer
    {
        public int interval;
        public int counter;

        public Timer(int interval)
        {
            this.interval = interval;
            this.counter = interval;
        }

        public boolean tick()
        {
            if (--this.counter <= 0)
            {
                this.reset();
                return true;
            }

            return false;
        }

        public void reset()
        {
            this.counter = this.interval;
        }
    }
}
