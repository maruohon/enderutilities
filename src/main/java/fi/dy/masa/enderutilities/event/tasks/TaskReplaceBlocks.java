package fi.dy.masa.enderutilities.event.tasks;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
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
        if (world.provider.getDimension() != this.dimension)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute(World world, EntityPlayer player)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.buildersWand);

        if (stack != null && this.wandUUID.equals(NBTUtils.getUUIDFromItemStack(stack, ItemBuildersWand.WRAPPER_TAG_NAME, false)))
        {
            for (int i = 0; i < this.blocksPerTick && this.listIndex < this.positions.size();)
            {
                if (this.replaceBlock(world, player, stack, this.positions.get(this.listIndex)))
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

    private boolean replaceBlock(World world, EntityPlayer player, ItemStack stack, BlockPosStateDist posIn)
    {
        BlockPos pos = posIn.toBlockPos();

        if (this.canReplaceBlock(world, player, stack, posIn))
        {
            this.handleOldBlock(world, player, pos);

            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();
            return wand.placeBlockToPosition(stack, world, player, posIn);
        }

        return false;
    }

    private boolean canReplaceBlock(World world, EntityPlayer player, ItemStack stack, BlockPosStateDist posIn)
    {
        return (ItemBuildersWand.hasEnoughCharge(stack, player) &&
               ItemBuildersWand.canManipulateBlock(world, posIn.toBlockPos(), player, stack, true)) &&
               (player.capabilities.isCreativeMode ||
                   ItemBuildersWand.getAndConsumeBuildItem(stack, player, posIn.blockInfo.blockState, true) != null);
    }

    private void handleOldBlock(World world, EntityPlayer player, BlockPos pos)
    {
        if (player.capabilities.isCreativeMode == false)
        {
            ItemStack stack = BlockUtils.getStackedItemFromBlock(world, pos);

            if (stack != null)
            {
                stack = InventoryUtils.tryInsertItemStackToInventory(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack);

                if (stack != null)
                {
                    EntityUtils.dropItemStacksInWorld(world, pos, stack, -1, true);
                }
            }
        }

        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
    }

    @Override
    public void stop()
    {
        this.positions.clear();
        //EnderUtilities.logger.info("TaskReplaceBlocks exiting, replaced " + this.placedCount + " blocks in total.");
    }
}
