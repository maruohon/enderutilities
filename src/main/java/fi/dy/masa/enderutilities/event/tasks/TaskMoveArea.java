package fi.dy.masa.enderutilities.event.tasks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosBox;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TaskMoveArea implements IPlayerTask
{
    protected final BlockPos posSrcStart;
    protected final BlockPos posDstStart;
    protected final Rotation rotation;
    protected final Mirror mirror;
    protected final UUID wandUUID;
    protected final int dimension;
    protected final int blocksPerTick;
    protected final BlockPosBox boxRelative;
    protected final BlockPosBox boxSource;
    protected final Set<BlockPos> handledPositions;
    protected int listIndex = 0;
    protected int placedCount = 0;
    protected int failCount = 0;
    protected BlockPos overlappingRelativePos = null;
    protected IBlockState overlappingState;
    protected NBTTagCompound overlappingNBT;
    protected boolean shouldExit = false;

    public TaskMoveArea(int dimension, BlockPos posSrcStart, BlockPos posSrcEnd, BlockPos posDstStart,
            Rotation rotationDst, Mirror mirrorDst, UUID wandUUID, int blocksPerTick)
    {
        this.posSrcStart = posSrcStart;
        this.posDstStart = posDstStart;
        this.rotation = rotationDst;
        this.mirror = mirrorDst;
        this.wandUUID = wandUUID;
        this.dimension = dimension;
        this.blocksPerTick = blocksPerTick;
        this.boxRelative = new BlockPosBox(BlockPos.ORIGIN, posSrcEnd.subtract(posSrcStart));
        this.boxSource = new BlockPosBox(posSrcStart, posSrcEnd);
        this.handledPositions = new HashSet<BlockPos>();
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
            this.moveBlock(world, player, stack);
        }
        else
        {
            this.failCount += 1;
        }

        // Bail out after 10 seconds of failing to execute, or after all blocks have been placed
        if (this.shouldExit || this.failCount > 200)
        {
            world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_BASEDRUM, SoundCategory.BLOCKS, 0.6f, 1.0f);
            return true;
        }

        // Finished looping through the block positions
        if (this.listIndex >= this.boxRelative.count)
        {
            world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.BLOCKS, 0.6f, 1.0f);
            return true;
        }

        return false;
    }

    private void moveBlock(World world, EntityPlayer player, ItemStack stack)
    {
        ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();
        BlockPos posRelative;
        BlockPos posSrc;
        BlockPos posDst;

        for (int i = 0; i < this.blocksPerTick && this.listIndex < this.boxRelative.count; )
        {
            if (ItemBuildersWand.hasEnoughCharge(stack, player) == false)
            {
                this.shouldExit = true;
                break;
            }

            IBlockState stateNew = null;
            NBTTagCompound nbt = null;

            // If there is currently an overlapping position/data stored from the last
            // operation, then we want to use that one instead of the next one in the normal source box
            if (this.overlappingRelativePos != null)
            {
                posRelative = this.overlappingRelativePos;

                stateNew = this.overlappingState;
                nbt = this.overlappingNBT;
                this.overlappingRelativePos = null;
                this.overlappingNBT = null;
            }
            // No overlapping data from last iteration
            else
            {
                posRelative = this.boxRelative.getPosAtIndex(this.listIndex);
                this.listIndex += 1;
            }

            if (this.handledPositions.contains(posRelative))
            {
                continue;
            }

            posSrc = this.posSrcStart.add(posRelative);
            posDst = PositionUtils.getTransformedBlockPos(posRelative, this.mirror, this.rotation).add(this.posDstStart);
            boolean replace = wand.getReplaceExisting(stack, Mode.MOVE_DST);
            boolean destinationOverlapsSource = this.boxSource.containsPosition(posDst);

            // If Replace is disabled and the destination is not air (and is not an overlapping source position),
            // or if one of the source and destination blocks can't be moved/broken if necessary,
            // or the wand doesn't have enough charge left, then skip this position
            if ((replace == false && destinationOverlapsSource == false && world.isAirBlock(posDst) == false) ||
                this.canMoveBlock(world, posSrc, posDst, player, stack) == false)
            {
                this.handledPositions.add(posRelative);
                continue;
            }

            // FIXME We can't really properly check for the movability of blocks in the overlapping case,
            // because that could be a recursive check through the ENTIRE area in the worst case...
            // So there will be block loss if there are non-movable blocks in an overlapping area...

            // No overlapping block data from last iteration, get the current block in the world
            if (stateNew == null)
            {
                stateNew = world.getBlockState(posSrc).getActualState(world, posSrc);

                if (stateNew.getBlock().hasTileEntity(stateNew))
                {
                    TileEntity te = world.getTileEntity(posSrc);
                    if (te != null)
                    {
                        nbt = new NBTTagCompound();
                        nbt = te.writeToNBT(nbt);
                    }
                }

                world.restoringBlockSnapshots = true;
                world.setBlockState(posSrc, Blocks.AIR.getDefaultState(), 2);
                world.restoringBlockSnapshots = false;
            }

            // If the destination position overlaps with the source box, then we will store
            // the current block state and possible TE data before replacing it with the one from
            // the source box. That stored data will then be handled in the next iteration
            // instead of the regular next position.
            if (destinationOverlapsSource)
            {
                // Get the relative source position of the overlapping destination position inside the source area
                this.overlappingRelativePos = posDst.subtract(this.posSrcStart);
                this.overlappingState = world.getBlockState(posDst).getActualState(world, posDst);
                // The actualState is needed to preserve the facing of blocks that store the facing in TE data
                // and that are able to handle it being restored via some trickery on block placement.
                // So basically just the blocks from Ender Utilities and Autoverse probably...

                if (this.overlappingState.getBlock().hasTileEntity(this.overlappingState))
                {
                    TileEntity te = world.getTileEntity(posDst);
                    if (te != null)
                    {
                        this.overlappingNBT = new NBTTagCompound();
                        this.overlappingNBT = te.writeToNBT(this.overlappingNBT);
                    }
                }

                // After the current block data has been stored for the next iteration, clear the position
                // Note that if the server would be to close after this point and before placing it back, that block will be lost forever
                world.restoringBlockSnapshots = true;
                world.setBlockState(posDst, Blocks.AIR.getDefaultState(), 2);
                world.restoringBlockSnapshots = false;
            }

            stateNew = stateNew.withMirror(this.mirror).withRotation(this.rotation);

            if (wand.placeBlockToPosition(stack, world, player, posDst, EnumFacing.UP, stateNew, 2, false, true))
            {
                if (nbt != null)
                {
                    TileEntity te = world.getTileEntity(posDst);
                    if (te != null)
                    {
                        NBTUtils.setPositionInTileEntityNBT(nbt, posDst);
                        te.readFromNBT(nbt);
                        te.markDirty();
                    }
                }

                this.placedCount += 1;
                this.failCount = 0;
                i += 1;
            }

            this.handledPositions.add(posRelative);
        }
    }

    public boolean canMoveBlock(World world, BlockPos posSrc, BlockPos posDst, EntityPlayer player, ItemStack stack)
    {
        return ItemBuildersWand.canManipulateBlock(world, posSrc, player, stack, true) &&
               ItemBuildersWand.canManipulateBlock(world, posDst, player, stack, true);
    }

    @Override
    public void stop()
    {
        this.handledPositions.clear();
        //EnderUtilities.logger.info("TaskMoveArea exiting, moved " + this.placedCount + " blocks in total.");
    }
}
