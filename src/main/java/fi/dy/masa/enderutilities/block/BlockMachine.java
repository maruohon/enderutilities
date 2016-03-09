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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;

import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.block.base.BlockProperties;
import fi.dy.masa.enderutilities.client.effects.Effects;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockMachine extends BlockEnderUtilitiesInventory
{
    public static final PropertyDirection FACING = BlockProperties.FACING;

    public static final PropertyEnum<BlockMachine.EnumMachineType> TYPE =
            PropertyEnum.<BlockMachine.EnumMachineType>create("type", BlockMachine.EnumMachineType.class);

    public BlockMachine(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(TYPE, BlockMachine.EnumMachineType.ENDER_INFUSER)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { TYPE, FACING });
    }

    @Override
    public String[] getUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER,
                ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION,
                ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION
        };
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        EnumMachineType type = state.getValue(TYPE);
        switch (type)
        {
            case ENDER_INFUSER: return new TileEntityEnderInfuser();
            case TOOL_WORKSTATION: return new TileEntityToolWorkstation();
            case CREATION_STATION: return new TileEntityCreationStation();
        }

        return new TileEntityEnderFurnace();
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
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
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityCreationStation)
        {
            // Drop the items from the furnace inventories
            IItemHandler inv = ((TileEntityCreationStation)te).getFurnaceInventory();

            for (int i = 0; i < inv.getSlots(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null)
                {
                    EntityUtils.dropItemStacksInWorld(worldIn, pos, stack, -1, true);
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public int getLightValue(IBlockAccess worldIn, BlockPos pos)
    {
        if (worldIn.getBlockState(pos).getValue(TYPE) == EnumMachineType.CREATION_STATION)
        {
            return 10;
        }

        return super.getLightValue(worldIn, pos);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, EnumMachineType.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnderUtilities)
        {
            EnumFacing facing = EnumFacing.getFront(((TileEntityEnderUtilities)te).getRotation());

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
        for (int meta = 0; meta < 3; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public static enum EnumMachineType implements IStringSerializable
    {
        ENDER_INFUSER(ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER),
        TOOL_WORKSTATION(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION),
        CREATION_STATION(ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION);

        private final String name;

        private EnumMachineType(String name)
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

        public static EnumMachineType fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : ENDER_INFUSER;
        }
    }
}
