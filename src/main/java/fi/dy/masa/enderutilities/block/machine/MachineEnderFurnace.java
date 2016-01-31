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
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class MachineEnderFurnace extends Machine
{
    public MachineEnderFurnace(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public boolean breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            // Drop the items from the output buffer
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;
            BlockEnderUtilitiesInventory.dropItemStacks(worldIn, pos, teef.getOutputBufferStack(), teef.getOutputBufferAmount(), true);
        }
    
        // We want the default BlockEnderUtilitiesInventory.breakBlock() to deal with the generic inventory
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;
            if (teef.isBurningLast == true)
            {
                return 15;
            }
            // No-fuel mode
            else if (teef.isCookingLast == true)
            {
                return 7;
            }
        }

        return state.getBlock().getLightValue();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            if (((TileEntityEnderFurnace)te).isBurningLast == true)
            {
                Particles.spawnParticlesAround(worldIn, EnumParticleTypes.PORTAL, pos, 2, rand);
            }
        }
    }
}
