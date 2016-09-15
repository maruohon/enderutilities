package fi.dy.masa.enderutilities.event.tasks;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TaskBuildersWand implements IPlayerTask
{
    protected final int dimension;
    protected final UUID wandUUID;
    protected final List<BlockPosStateDist> positions;
    protected final int blocksPerTick;
    protected int listIndex;
    protected int placedCount;
    protected int failCount;

    public TaskBuildersWand(World world, UUID wandUUID, List<BlockPosStateDist> positions, int blocksPerTick)
    {
        this.dimension = world.provider.getDimension();
        this.wandUUID = wandUUID;
        this.positions = positions;
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
        return world.provider.getDimension() == this.dimension;
    }

    @Override
    public boolean execute(World world, EntityPlayer player)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.buildersWand);

        if (stack != null && this.wandUUID.equals(NBTUtils.getUUIDFromItemStack(stack, ItemBuildersWand.WRAPPER_TAG_NAME, false)))
        {
            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();

            for (int i = 0; i < this.blocksPerTick && this.listIndex < this.positions.size();)
            {
                if (wand.placeBlockToPosition(stack, world, player, this.positions.get(this.listIndex)) == true)
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
                Mode mode = Mode.getMode(stack);
                BlockPosEU pos = wand.getPosition(stack, ItemBuildersWand.POS_START);

                // Move the target position forward by one block after the area has been built
                if (pos != null && mode != Mode.WALLS && mode != Mode.CUBE && wand.getMovePosition(stack, mode))
                {
                    wand.setPosition(stack, pos.offset(pos.side, 1), ItemBuildersWand.POS_START);
                }

                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.BLOCKS, 0.4f, 1.0f);

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
            return true;
        }

        return false;
    }

    @Override
    public void stop()
    {
        this.positions.clear();
        //EnderUtilities.logger.info("TaskBuildersWand exiting, placed " + this.placedCount + " blocks in total.");
    }
}
