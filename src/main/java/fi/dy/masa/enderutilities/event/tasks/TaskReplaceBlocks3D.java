package fi.dy.masa.enderutilities.event.tasks;

import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockInfo;
import fi.dy.masa.enderutilities.util.BlockPosBox;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TaskReplaceBlocks3D implements IPlayerTask
{
    protected final int dimension;
    protected final UUID wandUUID;
    protected final BlockPosEU pos1;
    protected final BlockPosEU pos2;
    protected final BlockPosBox box;
    protected final IBlockState stateTarget;
    protected final BlockInfo blockInfoReplacement;
    protected final int blocksPerTick;
    protected int listIndex = 0;
    protected int placedCount = 0;
    protected int failCount = 0;

    public TaskReplaceBlocks3D(World world, UUID wandUUID, BlockPosEU pos1, BlockPosEU pos2,
            IBlockState stateTarget, BlockInfo blockInfoReplacement, int blocksPerTick)
    {
        this.dimension = world.provider.getDimension();
        this.wandUUID = wandUUID;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.box = new BlockPosBox(pos1, pos2);
        this.stateTarget = stateTarget;
        this.blockInfoReplacement = blockInfoReplacement;
        this.blocksPerTick = blocksPerTick;
    }

    @Override
    public void init()
    {
    }

    @Override
    public boolean canExecute(World world, EntityPlayer player)
    {
        return world.provider.getDimension() == this.dimension;
    }

    @Override
    public boolean execute(World world, EntityPlayer player)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.buildersWand);

        if (stack != null && this.wandUUID.equals(NBTUtils.getUUIDFromItemStack(stack, ItemBuildersWand.WRAPPER_TAG_NAME, false)))
        {
            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();

            // 20k blocks check limit per task run
            for (int i = 0, bpt = 0; i < 20000 && bpt < this.blocksPerTick && this.listIndex < this.box.count; i++)
            {
                BlockPos pos = this.box.getPosAtIndex(this.listIndex);
                this.listIndex += 1;

                if (world.getBlockState(pos) == this.stateTarget)
                {
                    BlockPosStateDist posState = new BlockPosStateDist(new BlockPosEU(pos), this.blockInfoReplacement);

                    if (wand.replaceBlock(world, player, stack, posState))
                    {
                        this.placedCount += 1;
                        this.failCount = 0;
                        bpt += 1;
                    }
                }
            }

            // Finished looping through the block positions
            if (this.listIndex >= this.box.count)
            {
                return true;
            }
        }
        else
        {
            this.failCount += 1;
        }

        // Bail out after 10 seconds of failing to execute, or after all blocks have been placed
        if (this.failCount > 200)
        {
            world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_BASEDRUM, SoundCategory.BLOCKS, 0.6f, 1.0f);
            return true;
        }

        return false;
    }

    @Override
    public void stop()
    {
        //EnderUtilities.logger.info("TaskReplaceBlocks exiting, replaced " + this.placedCount + " blocks in total.");
    }
}
