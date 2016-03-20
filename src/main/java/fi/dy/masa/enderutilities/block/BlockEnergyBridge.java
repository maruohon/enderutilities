package fi.dy.masa.enderutilities.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.block.base.BlockProperties;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class BlockEnergyBridge extends BlockEnderUtilitiesTileEntity
{
    public static final PropertyDirection FACING = BlockProperties.FACING;
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public static final PropertyEnum<BlockEnergyBridge.EnumMachineType> TYPE =
            PropertyEnum.<BlockEnergyBridge.EnumMachineType>create("type", BlockEnergyBridge.EnumMachineType.class);

    public BlockEnergyBridge(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(ACTIVE, false)
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(TYPE, BlockEnergyBridge.EnumMachineType.RESONATOR));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { ACTIVE, FACING, TYPE });
    }

    @Override
    public String[] getUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR,
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER,
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER
        };
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        return new TileEntityEnergyBridge();
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
                ((TileEntityEnergyBridge)te).setType(worldIn.getBlockState(pos).getBlock().getMetaFromState(state));
                ((TileEntityEnergyBridge)te).tryAssembleMultiBlock(worldIn, pos);
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).disassembleMultiblock(worldIn, pos);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return false;
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
        if (te instanceof TileEntityEnergyBridge)
        {
            TileEntityEnergyBridge teeb = (TileEntityEnergyBridge)te;
            state = state.withProperty(ACTIVE, teeb.getIsActive());

            EnumFacing facing = EnumFacing.getFront(((TileEntityEnderUtilities)te).getRotation());
            if (facing.getAxis().isHorizontal() == true)
            {
                state = state.withProperty(FACING, facing);
            }
        }

        return state;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 15;
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
        RESONATOR (ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR),
        RECEIVER (ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER),
        TRANSMITTER (ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER);

        private final String name;

        private EnumMachineType(String name)
        {
            this.name = name.replace(".", "_");
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
            return meta < values().length ? values()[meta] : RESONATOR;
        }
    }
}
