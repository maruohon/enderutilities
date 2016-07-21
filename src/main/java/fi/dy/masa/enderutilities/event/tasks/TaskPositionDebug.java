package fi.dy.masa.enderutilities.event.tasks;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.effects.Effects;

public class TaskPositionDebug implements ITask
{
    private final IBlockState blockState;
    private final int dimension;
    private final List<BlockPos> positions;
    private final int blocksPerTick;
    private final boolean placeBlocks;
    private final boolean useParticles;
    private final EnumParticleTypes particle;
    private int listIndex;
    private int count;

    public TaskPositionDebug(World world, List<BlockPos> positions, int blocksPerTick)
    {
        this(world, positions, null, blocksPerTick, true, true, EnumParticleTypes.VILLAGER_ANGRY);
    }

    public TaskPositionDebug(World world, List<BlockPos> positions, IBlockState blockState, int blocksPerTick,
            boolean placeBlocks, boolean useParticles, EnumParticleTypes particle)
    {
        this.dimension = world.provider.getDimension();
        this.positions = positions;
        this.blockState = blockState;
        this.blocksPerTick = blocksPerTick;
        this.placeBlocks = placeBlocks;
        this.useParticles = useParticles;
        this.particle = particle;
        this.listIndex = 0;
        this.count = 0;
    }

    @Override
    public void init()
    {
    }

    @Override
    public boolean canExecute()
    {
        return true;
    }

    @Override
    public boolean execute()
    {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(this.dimension);
        if (world == null)
        {
            return true;
        }

        for (int i = 0; i < this.blocksPerTick; i++)
        {
            if (this.listIndex >= this.positions.size())
            {
                return true;
            }

            int meta = this.listIndex & 0xF;
            BlockPos pos = this.positions.get(this.listIndex++);

            if (this.placeBlocks)
            {
                IBlockState state = this.blockState != null ? this.blockState : Blocks.STAINED_GLASS.getStateFromMeta(meta);

                world.setBlockState(pos, state, 3);
                world.playSound(null, pos, state.getBlock().getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
            }

            if (this.useParticles)
            {
                Effects.spawnParticlesFromServer(this.dimension, pos, this.particle, 1);
            }

            this.count++;
        }

        return false;
    }

    @Override
    public void stop()
    {
        this.positions.clear();
        EnderUtilities.logger.info("TaskPositionDebug exiting, handled {} positions", this.count);
    }
}
