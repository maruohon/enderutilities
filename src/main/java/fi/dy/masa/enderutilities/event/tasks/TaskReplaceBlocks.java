package fi.dy.masa.enderutilities.event.tasks;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TaskReplaceBlocks implements IPlayerTask
{
    protected final int dimension;
    protected final UUID wandUUID;
    protected final List<BlockPosStateDist> positions;
    protected final int blocksPerTick;
    protected int listIndex = 0;
    protected int placedCount = 0;
    protected int failCount = 0;

    public TaskReplaceBlocks(World world, UUID wandUUID, List<BlockPosStateDist> positions, int blocksPerTick)
    {
        this.dimension = world.provider.getDimension();
        this.wandUUID = wandUUID;
        this.positions = positions;
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
        ItemStack stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.BUILDERS_WAND);

        if (stack.isEmpty() == false && this.wandUUID.equals(NBTUtils.getUUIDFromItemStack(stack, ItemBuildersWand.WRAPPER_TAG_NAME, false)))
        {
            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();

            for (int i = 0; i < this.blocksPerTick && this.listIndex < this.positions.size();)
            {
                if (wand.replaceBlock(world, player, stack, this.positions.get(this.listIndex)))
                {
                    this.placedCount += 1;
                    this.failCount = 0;
                    i += 1;
                }

                this.listIndex += 1;
            }

            // Finished looping through the block positions
            if (this.listIndex >= this.positions.size())
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
        this.positions.clear();
        //EnderUtilities.logger.info("TaskReplaceBlocks exiting, replaced " + this.placedCount + " blocks in total.");
    }
}
