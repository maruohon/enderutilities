package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public abstract class BlockEnderUtilitiesInventory extends BlockEnderUtilitiesTileEntity
{
    public BlockEnderUtilitiesInventory(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState iBlockState)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof TileEntityEnderUtilitiesInventory && ((TileEntityEnderUtilitiesInventory) te).getBaseItemHandler() != null)
        {
            IItemHandler itemHandler = ((TileEntityEnderUtilitiesInventory) te).getBaseItemHandler();
            int numSlots = itemHandler.getSlots();

            for (int i = 0; i < numSlots; i++)
            {
                ItemStack stack = itemHandler.getStackInSlot(i);

                if (stack != null)
                {
                    EntityUtils.dropItemStacksInWorld(worldIn, pos, stack, -1, false);
                }
            }

            worldIn.updateComparatorOutputLevel(pos, this);
        }

        worldIn.removeTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnderUtilities == false)
        {
            return false;
        }

        if (this.isTileEntityValid(te))
        {
            playerIn.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te == null || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN) == false)
        {
            return 0;
        }

        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);

        return inv != null ? InventoryUtils.calcRedstoneFromInventory(inv) : 0;
    }
}
