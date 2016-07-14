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
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosBox;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TaskMoveArea implements IPlayerTask
{
    protected final ItemStack wandStack;
    protected final BlockPos posSrcStart;
    protected final BlockPos posSrcEnd;
    protected final BlockPos posDstStart;
    protected final BlockPos posDstEnd;
    protected final Rotation rotation;
    protected final Mirror mirror;
    protected final UUID playerUUID;
    protected final int dimension;
    protected final int blocksPerTick;
    protected final BlockPosBox boxRelative;
    protected final BlockPosBox boxSource;
    protected final BlockPosBox boxDestination;
    protected int listIndex;
    protected int placedCount;
    protected int failCount;
    protected final Set<BlockPos> handledSrcPositions;
    protected BlockPos overlappingPos = null;
    protected IBlockState overlappingState;
    protected NBTTagCompound overlappingNBT;

    public TaskMoveArea(ItemStack wandStack, BlockPosEU posSrcStart, BlockPosEU posSrcEnd, BlockPosEU posDstStart, BlockPosEU posDstEnd,
            Rotation rotationDst, Mirror mirrorDst, UUID playerUUID, int blocksPerTick)
    {
        this.wandStack = ItemStack.copyItemStack(wandStack);
        this.posSrcStart = posSrcStart.toBlockPos();
        this.posSrcEnd = posSrcEnd.toBlockPos();
        this.posDstStart = posDstStart.toBlockPos();
        this.posDstEnd = posDstEnd.toBlockPos();
        this.rotation = rotationDst;
        this.mirror = mirrorDst;
        this.playerUUID = playerUUID;
        this.dimension = posDstStart.dimension;
        this.blocksPerTick = blocksPerTick;
        this.boxRelative = new BlockPosBox(BlockPos.ORIGIN, this.posSrcEnd.subtract(this.posSrcStart));
        this.boxSource = new BlockPosBox(this.posSrcStart, this.posSrcEnd);
        this.boxDestination = new BlockPosBox(this.posDstStart, this.posDstEnd);
        this.listIndex = 0;
        this.placedCount = 0;
        this.failCount = 0;
        this.handledSrcPositions = new HashSet<BlockPos>();
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
        if (stack != null)
        {
            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();
            BlockPos posRelative;
            BlockPos posSrc;
            BlockPos posDstRelative;
            BlockPos posDst;
            IBlockState stateNew;

            for (int i = 0; i < this.blocksPerTick && this.listIndex < this.boxRelative.count; )
            {
                NBTTagCompound nbt = null;

                // If there is currently an overlapping position/data stored from the last
                // operation, then we want to use that one instead of the next one in the normal source box
                if (this.overlappingPos != null)
                {
                    posSrc = this.overlappingPos;
                    posDstRelative = PositionUtils.getTransformedBlockPos(posSrc.subtract(this.posSrcStart), this.mirror, this.rotation);

                    stateNew = this.overlappingState;
                    nbt = this.overlappingNBT;
                    this.overlappingPos = null;
                    this.overlappingNBT = null;
                }
                else
                {
                    posRelative = this.boxRelative.getPosAtIndex(this.listIndex);
                    this.listIndex += 1;

                    posSrc = this.posSrcStart.add(posRelative);
                    posDstRelative = PositionUtils.getTransformedBlockPos(posRelative, this.mirror, this.rotation);

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

                    // Only clear the source position if it's not inside the destination area
                    if (this.boxDestination.containsPosition(posSrc) == false)
                    {
                        world.restoringBlockSnapshots = true;
                        world.setBlockState(posSrc, Blocks.AIR.getDefaultState(), 2);
                        world.restoringBlockSnapshots = false;
                    }
                }

                posDst = posDstRelative.add(this.posDstStart);

                if (this.handledSrcPositions.contains(posDst))
                {
                    continue;
                }

                boolean overlap = false;
                // If the destination position overlaps with the source box, and that position hasn't been handled yet,
                // then we will store the current block state and possible TE data before replacing it with the one from
                // the source box. That stored data will then be handled in the next iteration instead of the regular next position.
                if (this.boxSource.containsPosition(posDst) && this.handledSrcPositions.contains(posDst) == false)
                {
                    this.overlappingPos = posDst;
                    this.overlappingState = world.getBlockState(posDst).getActualState(world, posDst);
                    overlap = true;

                    if (this.overlappingState.getBlock().hasTileEntity(this.overlappingState))
                    {
                        TileEntity te = world.getTileEntity(posDst);
                        if (te != null)
                        {
                            this.overlappingNBT = new NBTTagCompound();
                            this.overlappingNBT = te.writeToNBT(this.overlappingNBT);
                        }
                    }

                    world.restoringBlockSnapshots = true;
                    world.setBlockState(posDst, Blocks.AIR.getDefaultState(), 2);
                    world.restoringBlockSnapshots = false;
                }

                stateNew = stateNew.withMirror(this.mirror).withRotation(this.rotation);

                if (wand.placeBlockToPosition(this.wandStack, world, player, posDst, EnumFacing.UP, stateNew, 2, false, overlap == false) == true)
                {
                    if (nbt != null && stateNew.getBlock().hasTileEntity(stateNew))
                    {
                        TileEntity teDst = world.getTileEntity(posDst);
                        if (teDst != null)
                        {
                            NBTUtils.setPositionInTileEntityNBT(nbt, posDst);
                            teDst.readFromNBT(nbt);
                            teDst.markDirty();
                        }
                    }

                    this.placedCount += 1;
                    this.failCount = 0;
                    i += 1;
                }

                this.handledSrcPositions.add(posDst);
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

        // Finished looping through the block positions
        if (this.listIndex >= this.boxRelative.count)
        {
            //this.template.notifyBlocks(world, this.posStart);
            //this.template.addEntitiesToWorld(world, this.posStart);

            world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.BLOCKS, 0.6f, 1.0f);

            return true;
        }

        return false;
    }

    @Override
    public void stop()
    {
        EnderUtilities.logger.info("TaskStructureBuild exiting, placed " + this.placedCount + " blocks in total.");
        this.handledSrcPositions.clear();
    }
}
