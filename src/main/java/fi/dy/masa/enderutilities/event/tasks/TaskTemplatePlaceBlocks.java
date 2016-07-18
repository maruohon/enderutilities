package fi.dy.masa.enderutilities.event.tasks;

import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.TemplateEnderUtilities;
import fi.dy.masa.enderutilities.util.TemplateEnderUtilities.TemplateBlockInfo;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TaskTemplatePlaceBlocks implements IPlayerTask
{
    protected final TemplateEnderUtilities template;
    protected final BlockPos posStart;
    protected final UUID playerUUID;
    protected final UUID wandUUID;
    protected final int dimension;
    protected final int blocksPerTick;
    protected final boolean tileEntities;
    protected final boolean entities;
    protected int listIndex;
    protected int placedCount;
    protected int failCount;

    public TaskTemplatePlaceBlocks(TemplateEnderUtilities template, BlockPos posStart, int dimension, UUID playerUUID, UUID wandUUID,
            int blocksPerTick, boolean copyTileEntities, boolean placeEntities)
    {
        this.template = template;
        this.posStart = posStart;
        this.playerUUID = playerUUID;
        this.wandUUID = wandUUID;
        this.dimension = dimension;
        this.blocksPerTick = blocksPerTick;
        this.tileEntities = copyTileEntities;
        this.entities = placeEntities;
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
            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();
            PlacementSettings placement = this.template.getPlacementSettings();

            for (int i = 0; i < this.blocksPerTick && this.listIndex < this.template.getBlockList().size();)
            {
                TemplateBlockInfo blockInfo = this.template.getBlockList().get(this.listIndex);
                IBlockState state = blockInfo.blockState.withMirror(placement.getMirror()).withRotation(placement.getRotation());
                BlockPos pos = TemplateEnderUtilities.transformedBlockPos(placement, blockInfo.pos).add(this.posStart);

                if (wand.placeBlockToPosition(stack, world, player, pos, EnumFacing.UP, state, 2, true, true) == true)
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
            world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.BLOCKS, 0.6f, 1.0f);

            return true;
        }

        // Finished looping through the block positions
        if (this.listIndex >= this.template.getBlockList().size())
        {
            this.template.notifyBlocks(world, this.posStart);
            this.template.addEntitiesToWorld(world, this.posStart);

            world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.BLOCKS, 0.6f, 1.0f);

            return true;
        }

        return false;
    }

    @Override
    public void stop()
    {
        EnderUtilities.logger.info("TaskStructureBuild exiting, placed " + this.placedCount + " blocks in total.");
    }
}
