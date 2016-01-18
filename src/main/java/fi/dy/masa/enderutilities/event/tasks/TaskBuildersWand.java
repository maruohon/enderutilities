package fi.dy.masa.enderutilities.event.tasks;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;

public class TaskBuildersWand implements IPlayerTask
{
    protected int dimension;
    protected UUID playerUUID;
    protected List<BlockPosStateDist> positions;
    protected int blocksPerTick;
    protected int listIndex;
    protected int placedCount;
    protected int failCount;

    public TaskBuildersWand(World world, UUID playerUUID, List<BlockPosStateDist> positions, int blocksPerTick)
    {
        this.dimension = world.provider.dimensionId;
        this.playerUUID = playerUUID;
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
        if (world.provider.dimensionId != this.dimension)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute(World world, EntityPlayer player)
    {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack != null && stack.getItem() == EnderUtilitiesItems.buildersWand)
        {
            for (int i = 0; i < this.blocksPerTick && this.listIndex < this.positions.size();)
            {
                if (ItemBuildersWand.placeBlockToPosition(stack, world, player, this.positions.get(this.listIndex)) == true)
                {
                    this.placedCount += 1;
                    this.failCount = 0;
                    i += 1;
                }

                this.listIndex += 1;
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

        // Finished looping through the block positions
        if (this.listIndex >= this.positions.size())
        {
            if (stack != null && stack.getItem() == EnderUtilitiesItems.buildersWand)
            {
                Mode mode = Mode.getMode(stack);
                BlockPosEU pos = ((ItemBuildersWand)stack.getItem()).getPosition(player, true);

                // Move the target position forward by one block after the area has been built
                if (pos != null && mode != Mode.WALLS && mode != Mode.CUBE && mode != Mode.COLUMN)
                {
                    ((ItemBuildersWand)stack.getItem()).setPosition(player, pos.offset(ForgeDirection.getOrientation(pos.face), 1), true);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void stop()
    {
        EnderUtilities.logger.info("TaskBuildersWand exiting, placed " + this.placedCount + " blocks in total.");
        this.positions.clear();
    }
}
