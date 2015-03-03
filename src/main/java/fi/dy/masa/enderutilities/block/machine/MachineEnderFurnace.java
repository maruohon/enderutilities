package fi.dy.masa.enderutilities.block.machine;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class MachineEnderFurnace extends Machine
{
    public MachineEnderFurnace(EnumMachine machineType, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(machineType, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public IBlockState getActualState(IBlockState iBlockState, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            //iBlockState = super.getActualState(iBlockState, worldIn, pos);
            // TODO fast mode
            return iBlockState.withProperty(BlockEnderUtilitiesTileEntity.IS_ACTIVE, Boolean.valueOf(((TileEntityEnderFurnace)te).isActive));
        }

        return iBlockState;
    }

    @Override
    public boolean breakBlock(World world, BlockPos pos, IBlockState iBlockState)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            // Drop the items from the output buffer
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;
            BlockEnderUtilitiesInventory.dropItemStacks(world, pos, teef.getOutputBufferStack(), teef.getOutputBufferAmount(), true);
        }
    
        // We want the default BlockEnderUtilitiesInventory.breakBlock() to deal with the generic inventory
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos, IBlockState iBlockState)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;
            if (teef.isBurning() == true)
            {
                return 15;
            }
            // No-fuel mode
            else if (teef.burnTimeFresh != 0)
            {
                return 7;
            }
        }

        return world.getBlockState(pos).getBlock().getLightValue();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, BlockPos pos, IBlockState iBlockState, Random rand)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            if (((TileEntityEnderFurnace)te).isActive == true)
            {
                Particles.spawnParticlesAround(world, EnumParticleTypes.PORTAL, pos, 2, rand);
            }
        }
    }
}
