package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockEnderFurnace extends BlockEnderUtilitiesInventory
{
    //public static final PropertyEnum<EnumMachineType> TYPE = PropertyEnum.<EnumMachineType>create("type", EnumMachineType.class);
    public static final PropertyEnum<EnumMachineMode> MODE = PropertyEnum.<EnumMachineMode>create("mode", EnumMachineMode.class);

    public BlockEnderFurnace(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(MODE, EnumMachineMode.OFF)
                .withProperty(FACING_H, BlockEnderUtilities.DEFAULT_FACING));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING_H, MODE });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] { ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE };
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityEnderFurnace();
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntityEnderFurnace te = getTileEntitySafely(world, pos, TileEntityEnderFurnace.class);

        if (te != null)
        {
            if (te.isBurningLast)
            {
                return 15;
            }
            // No-fuel mode
            else if (te.isCookingLast)
            {
                return 7;
            }
        }

        return super.getLightValue(state, world, pos);
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
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        state = super.getActualState(state, world, pos);

        TileEntityEnderFurnace te = getTileEntitySafely(world, pos, TileEntityEnderFurnace.class);

        if (te != null)
        {
            EnumMachineMode mode = te.isCookingLast == false ? EnumMachineMode.OFF :
                (te.isBurningLast == false ? EnumMachineMode.ON_NOFUEL : 
                    te.fastMode ? EnumMachineMode.ON_FAST : EnumMachineMode.ON_NORMAL);

            state = state.withProperty(MODE, mode);
        }

        return state;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand)
    {
        TileEntityEnderFurnace te = getTileEntitySafely(world, pos, TileEntityEnderFurnace.class);

        if (te != null)
        {
            if (te.isBurningLast)
            {
                Effects.spawnParticlesAround(world, EnumParticleTypes.PORTAL, pos, 2, rand);

                if (rand.nextDouble() < 0.1D)
                {
                    world.playSound((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D,
                            SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                }
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
