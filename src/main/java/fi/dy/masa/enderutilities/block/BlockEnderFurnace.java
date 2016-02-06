package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.block.base.BlockProperties;
import fi.dy.masa.enderutilities.client.effects.Effects;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockEnderFurnace extends BlockEnderUtilitiesInventory
{
    public static final PropertyDirection FACING = BlockProperties.FACING;
    //public static final PropertyEnum<EnumMachineType> TYPE = PropertyEnum.<EnumMachineType>create("type", EnumMachineType.class);
    public static final PropertyEnum<EnumMachineMode> MODE = PropertyEnum.<EnumMachineMode>create("mode", EnumMachineMode.class);

    public BlockEnderFurnace(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState()
                //.withProperty(TYPE, BlockEnderFurnace.EnumMachineType.ENDER_FURNACE)
                .withProperty(MODE, EnumMachineMode.OFF)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { FACING, MODE });
    }

    @Override
    public String[] getUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE
        };
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        return new TileEntityEnderFurnace();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof TileEntityEnderFurnace)
        {
            // Drop the items from the output buffer
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;
            EntityUtils.dropItemStacksInWorld(worldIn, pos, teef.getOutputBufferStack(), teef.getOutputBufferAmount(), true);
        }
    }

    @Override
    public int getLightValue(IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnderFurnace)
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

        return super.getLightValue(worldIn, pos);
    }

    /*@Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState();
    }*/

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnderFurnace)
        {
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace) te;

            EnumMachineMode mode = teef.isCookingLast == false ? EnumMachineMode.OFF :
                (teef.isBurningLast == false ? EnumMachineMode.ON_NOFUEL : 
                    teef.fastMode == true ? EnumMachineMode.ON_FAST : EnumMachineMode.ON_NORMAL);

            state = state.withProperty(MODE, mode);

            EnumFacing facing = EnumFacing.getFront(teef.getRotation());
            if (facing.getAxis().isHorizontal() == true)
            {
                state = state.withProperty(FACING, facing);
            }
        }

        return state;
    }

    @Override
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            if (((TileEntityEnderFurnace)te).isBurningLast == true)
            {
                Effects.spawnParticlesAround(worldIn, EnumParticleTypes.PORTAL, pos, 2, rand);
            }
        }
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < 1; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public static enum EnumMachineMode implements IStringSerializable
    {
        OFF ("off"),
        ON_NOFUEL ("on_nofuel"),
        ON_NORMAL ("on_normal"),
        ON_FAST ("on_fast");

        private final String name;

        private EnumMachineMode(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return this.name;
        }

        public String getName()
        {
            return this.name;
        }

        public int getMeta()
        {
            return this.ordinal();
        }
    }
}
