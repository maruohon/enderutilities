package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class MachineEnergyBridge extends Machine
{
    public MachineEnergyBridge(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).tryAssembleMultiBlock(worldIn, pos);
            }
        }
    }

    // FIXME
    /*@Override
    public void onBlockPreDestroy(World world, BlockPos pos, int oldMeta)
    {
        super.onBlockPreDestroy(world, pos, oldMeta);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).disassembleMultiblock(world, pos, oldMeta);
            }
        }
    }*/

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        return 15;
    }
}
