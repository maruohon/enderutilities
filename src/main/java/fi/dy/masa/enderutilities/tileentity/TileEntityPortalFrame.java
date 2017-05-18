package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TileEntityPortalFrame extends TileEntityEnderUtilities
{
    private IBlockState camoState;

    public TileEntityPortalFrame()
    {
        super(ReferenceNames.NAME_TILE_FRAME);
    }

    /**
     * @return the current camouflage block state. If none is set, then Blocks.AIR.getDefaultState() is returned.
     */
    public IBlockState getCamoState()
    {
        return this.camoState != null ? this.camoState : Blocks.AIR.getDefaultState();
    }

    @Override
    public void onRightClickBlock(EntityPlayer player, EnumHand hand, EnumFacing side)
    {
        ItemStack stack = player.getHeldItemOffhand();

        // Sneaking with an empty hand, clear the camo block
        if (player.isSneaking())
        {
            this.camoState = null;
            this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1f, 1f);
            this.notifyBlockUpdate();
        }
        else if (stack != null)
        {
            // Right clicking with an ItemBlock
            if (stack.getItem() instanceof ItemBlock)
            {
                ItemBlock item = (ItemBlock) stack.getItem();
                int meta = item.getMetadata(stack.getMetadata());
                this.camoState = item.block.getStateForPlacement(this.getWorld(), this.getPos(), EnumFacing.UP, 0.5f, 1f, 0.5f, meta, player, stack);
                this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                this.notifyBlockUpdate();
            }
        }
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Camo", Constants.NBT.TAG_COMPOUND))
        {
            this.camoState = NBTUtils.readBlockStateFromTag(nbt.getCompoundTag("Camo"));
        }
    }

    @Override
    protected NBTTagCompound writeToNBTCustom(NBTTagCompound nbt)
    {
        if (this.camoState != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTUtils.writeBlockStateToTag(this.camoState, tag);
            nbt.setTag("Camo", tag);
        }

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        if (this.camoState != null)
        {
            nbt.setInteger("camo", Block.getStateId(this.camoState));
        }

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        if (tag.hasKey("camo", Constants.NBT.TAG_INT))
        {
            this.camoState = Block.getStateById(tag.getInteger("camo"));
        }
        else
        {
            this.camoState = null;
        }

        this.notifyBlockUpdate();
    }
}
