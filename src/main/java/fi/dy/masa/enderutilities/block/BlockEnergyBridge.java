package fi.dy.masa.enderutilities.block;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class BlockEnergyBridge extends BlockEnderUtilitiesTileEntity
{
    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    public static final PropertyEnum<BlockEnergyBridge.EnumMachineType> TYPE =
            PropertyEnum.<BlockEnergyBridge.EnumMachineType>create("type", BlockEnergyBridge.EnumMachineType.class);

    public BlockEnergyBridge(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(ACTIVE, false)
                .withProperty(FACING, BlockEnderUtilities.DEFAULT_FACING)
                .withProperty(TYPE, BlockEnergyBridge.EnumMachineType.RESONATOR));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { ACTIVE, FACING, TYPE });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR,
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER,
                ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER
        };
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
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
                ((TileEntityEnergyBridge) te).setType(state.getValue(TYPE).getMeta());
                ((TileEntityEnergyBridge) te).tryAssembleMultiBlock();
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
                ((TileEntityEnergyBridge) te).disassembleMultiblock();
            }
        }

        super.breakBlock(worldIn, pos, state);
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
        state = super.getActualState(state, worldIn, pos);

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnergyBridge)
        {
            state = state.withProperty(ACTIVE, ((TileEntityEnergyBridge) te).getIsActive());
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
        RESONATOR   (0, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR),
        RECEIVER    (1, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER),
        TRANSMITTER (2, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER);

        private final String name;
        private final int meta;

        private EnumMachineType(int meta, String name)
        {
            this.meta = meta;
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
            return this.meta;
        }

        public static EnumMachineType fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : RESONATOR;
        }
    }
}
