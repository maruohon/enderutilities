package fi.dy.masa.enderutilities.event.tasks;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;

public class TaskStructureBuild implements IPlayerTask
{
    protected int dimension;
    protected UUID playerUUID;
    protected int blocksPerTick;
    protected int listIndex;
    protected int placedCount;
    protected int failCount;

    public TaskStructureBuild(World world, UUID playerUUID, List<BlockPosStateDist> positions, int blocksPerTick)
    {
        this.dimension = world.provider.getDimension();
        this.playerUUID = playerUUID;
        this.blocksPerTick = blocksPerTick;
        this.listIndex = 0;
        this.placedCount = 0;
        this.failCount = 0;
    }

    @Override
    public void init()
    {
    }

    @Override
    public boolean canExecute(World world, EntityPlayer player)
    {
        if (world.provider.getDimension() != this.dimension)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute(World world, EntityPlayer player)
    {
        return true;
    }

    @Override
    public void stop()
    {
        EnderUtilities.logger.info("TaskStructureBuild exiting, placed " + this.placedCount + " blocks in total.");
    }
}
