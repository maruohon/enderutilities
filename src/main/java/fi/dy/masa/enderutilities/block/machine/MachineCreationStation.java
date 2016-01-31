package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class MachineCreationStation extends Machine
{
    public MachineCreationStation(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public boolean breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te != null && te instanceof TileEntityCreationStation)
        {
            // Drop the items from the furnace inventories
            IInventory inv = ((TileEntityCreationStation)te).getFurnaceInventory();

            for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null)
                {
                    BlockEnderUtilitiesInventory.dropItemStacks(worldIn, pos, stack, stack.stackSize, true);
                }
            }
        }
    
        // We want the default BlockEnderUtilitiesInventory.breakBlock() to deal with the generic inventory
        return false;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        super.onBlockClicked(worldIn, pos, playerIn);

        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityCreationStation)
            {
                ((TileEntityCreationStation)te).onLeftClickBlock(playerIn);
            }
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityCreationStation)
        {
            return 15;
        }

        return state.getBlock().getLightValue();
    }
}
