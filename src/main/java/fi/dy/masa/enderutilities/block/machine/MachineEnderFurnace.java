package fi.dy.masa.enderutilities.block.machine;

import java.util.Map;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Maps;

import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
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
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;
            int mode = 0;
            if (teef.isActive == true)
            {
                if (teef.operatingMode == 1)
                {
                    mode = 3;
                }
                else if (teef.usingFuel == true)
                {
                    mode = 2;
                }
                else
                {
                    mode = 1;
                }
            }

            return iBlockState.withProperty(BlockEnderUtilitiesTileEntity.MACHINE_MODE, Integer.valueOf(mode));
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

    @SideOnly(Side.CLIENT)
    @Override
    public void registerTextures(TextureMap textureMap)
    {
        this.texture_names = new String[6];
        this.texture_names[0] = "enderfurnace.front.off";
        this.texture_names[1] = "enderfurnace.front.on.nofuel";
        this.texture_names[2] = "enderfurnace.front.on.slow";
        this.texture_names[3] = "enderfurnace.front.on.fast";
        this.texture_names[4] = "machine.top.0";
        this.texture_names[5] = "machine.side.0";

        super.registerTextures(textureMap);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Map<String, String> getTextureMapping(IBlockState iBlockState)
    {
        Map<String, String> textureMapping = Maps.newHashMap();
        int index = 0;

        // When we retrieve the model for the ItemBlocks, the IBlockState will be null!
        if (iBlockState != null)
        {
            int mode = (Integer)iBlockState.getValue(BlockEnderUtilitiesTileEntity.MACHINE_MODE);
            if (mode >= 0 && mode < 4)
            {
                index = mode;
            }
        }

        textureMapping.put("front",   ReferenceTextures.getTileTextureName(this.texture_names[index]));
        textureMapping.put("top",     ReferenceTextures.getTileTextureName(this.texture_names[4]));
        textureMapping.put("bottom",  ReferenceTextures.getTileTextureName(this.texture_names[4]));
        textureMapping.put("side",    ReferenceTextures.getTileTextureName(this.texture_names[5]));

        return textureMapping;
    }
}
