package fi.dy.masa.enderutilities.event.tasks;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.effects.Effects;

public class TaskPositionDebug implements IPlayerTask
{
    private final int dimension;
    private List<BlockPos> positions;
    private final int blocksPerTick;
    private int listIndex;
    private int count;

    public TaskPositionDebug(World world, List<BlockPos> positions, int blocksPerTick)
    {
        this.dimension = world.provider.getDimension();
        this.positions = positions;
        this.blocksPerTick = blocksPerTick;
        this.listIndex = 0;
        this.count = 0;
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
        for (int i = 0; i < this.blocksPerTick; i++)
        {
            if (this.listIndex >= this.positions.size())
            {
                return true;
            }

            int meta = this.listIndex & 0xF;
            BlockPos pos = this.positions.get(this.listIndex++);

            world.setBlockState(pos, Blocks.STAINED_GLASS.getStateFromMeta(meta), 3);
            world.playSound(null, pos, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            Effects.spawnParticlesFromServer(world.provider.getDimension(), pos, EnumParticleTypes.VILLAGER_ANGRY);

            this.count++;
        }

        return false;
    }

    @Override
    public void stop()
    {
        EnderUtilities.logger.info("TaskPositionDebug exiting, handled {} positions", this.count);
        this.positions.clear();
    }
}
