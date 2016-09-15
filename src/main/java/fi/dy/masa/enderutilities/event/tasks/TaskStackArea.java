package fi.dy.masa.enderutilities.event.tasks;

import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Area3D;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.TemplateEnderUtilities;
import fi.dy.masa.enderutilities.util.TemplateEnderUtilities.TemplateBlockInfo;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TaskStackArea implements IPlayerTask
{
    protected final int dimension;
    protected final UUID wandUUID;
    protected final BlockPos posOrig;
    protected final Vec3i sizeAbs;
    //protected final BlockPosBox box;
    protected final TemplateEnderUtilities template;
    protected final Area3D area;
    protected final int blocksPerTick;
    protected final int templateNumBlocks;
    protected Vec3i currentArea;
    protected BlockPos currentStartPos;
    protected int listIndex = 0;
    protected int placedCount = 0;
    protected int failCount = 0;

    public TaskStackArea(World world, UUID wandUUID, BlockPos posOrig, BlockPos endPosRelative,
            TemplateEnderUtilities template, Area3D area, int blocksPerTick)
    {
        this.dimension = world.provider.getDimension();
        this.wandUUID = wandUUID;
        this.posOrig = posOrig;
        this.sizeAbs = new Vec3i(Math.abs(endPosRelative.getX()) + 1, Math.abs(endPosRelative.getY()) + 1, Math.abs(endPosRelative.getZ()) + 1);
        this.template = template;
        this.templateNumBlocks = template.getBlockList().size();
        this.area = area;
        this.blocksPerTick = blocksPerTick;
        this.currentArea = new Vec3i(-area.getXNeg(), -area.getYNeg(), -area.getZNeg());

        if (this.currentArea.getX() == 0 && this.currentArea.getY() == 0 && this.currentArea.getZ() == 0)
        {
            this.incrementArea();
        }

        this.currentStartPos = this.getStartPos();
    }

    protected BlockPos getStartPos()
    {
        return this.posOrig.add(this.currentArea.getX() * this.sizeAbs.getX(),
                                this.currentArea.getY() * this.sizeAbs.getY(),
                                this.currentArea.getZ() * this.sizeAbs.getZ());
    }

    protected boolean incrementArea()
    {
        int x = this.currentArea.getX();
        int y = this.currentArea.getY();
        int z = this.currentArea.getZ();

        if (++x > this.area.getXPos())
        {
            x = this.area.getXNeg();

            if (++z > this.area.getZPos())
            {
                z = this.area.getZNeg();
                y++;
            }
        }

        this.currentArea = new Vec3i(x, y, z);
        this.currentStartPos = this.getStartPos();

        if (y > this.area.getYPos())
        {
            return false;
        }

        // Skip the original area
        if (x == 0 && y == 0 && z == 0)
        {
            this.incrementArea();
        }

        return true;
    }

    @Override
    public void init()
    {
    }

    @Override
    public boolean canExecute(World world, EntityPlayer player)
    {
        return world.provider.getDimension() == this.dimension && this.currentArea.getY() <= this.area.getYPos();
    }

    @Override
    public boolean execute(World world, EntityPlayer player)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.buildersWand);

        if (stack != null && this.wandUUID.equals(NBTUtils.getUUIDFromItemStack(stack, ItemBuildersWand.WRAPPER_TAG_NAME, false)))
        {
            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();
            PlacementSettings placement = this.template.getPlacementSettings();

            for (int placedThisTick = 0 ; placedThisTick < this.blocksPerTick && this.listIndex < this.templateNumBlocks; )
            {
                TemplateBlockInfo blockInfo = this.template.getBlockList().get(this.listIndex);
                IBlockState state = blockInfo.blockState.withMirror(placement.getMirror()).withRotation(placement.getRotation());
                BlockPos pos = TemplateEnderUtilities.transformedBlockPos(placement, blockInfo.pos).add(this.currentStartPos);

                if (wand.placeBlockToPosition(stack, world, player, pos, EnumFacing.UP, state, 2, true, true) == true)
                {
                    this.placedCount += 1;
                    this.failCount = 0;
                    placedThisTick += 1;
                }

                this.listIndex += 1;

                // Finished looping through the block positions
                if (this.listIndex >= this.templateNumBlocks)
                {
                    this.listIndex = 0;

                    // Finished iterating through all the stacked areas
                    if (this.incrementArea() == false)
                    {
                        world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.BLOCKS, 0.6f, 1.0f);
                        return true;
                    }
                }
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
