package fi.dy.masa.enderutilities.event.tasks;

public interface ITask
{
    /**
     * Initialize the task. Called when the task is added to the task list.
     */
    public void init();

    /**
     * Return whether this task can be executed.
     * @return true if the task can be executed
     */
    public boolean canExecute();

    /**
     * Execute the task. Return true to indicate that this task has finished.
     * @return true to indicate the task has finished and can be removed
     */
    public boolean execute();

    /**
     * Stop the task. This is also called when the tasks are removed.
     */
    public void stop();
}
