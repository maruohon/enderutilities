package fi.dy.masa.enderutilities.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
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
    public static final PropertyEnum<BridgeType> TYPE =
            PropertyEnum.<BridgeType>create("type", BridgeType.class);

    public BlockEnergyBridge(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(ACTIVE, false)
                .withProperty(FACING_H, BlockEnderUtilities.DEFAULT_FACING)
                .withProperty(TYPE, BridgeType.RESONATOR));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { ACTIVE, FACING_H, TYPE });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                BridgeType.RESONATOR.getName(),
                BridgeType.RECEIVER.getName(),
                BridgeType.TRANSMITTER.getName()
        };
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        TileEntityEnergyBridge te = new TileEntityEnergyBridge();
        te.setType(state.getValue(TYPE).getMeta());
        return te;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if (world.isRemote == false)
        {
            TileEntityEnergyBridge te = getTileEntitySafely(world, pos, TileEntityEnergyBridge.class);

            if (te != null)
            {
                te.tryAssembleMultiBlock();
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        if (world.isRemote == false)
        {
            TileEntityEnergyBridge te = getTileEntitySafely(world, pos, TileEntityEnergyBridge.class);

            if (te != null)
            {
                te.disassembleMultiblock();
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, BridgeType.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        state = super.getActualState(state, world, pos);

        TileEntityEnergyBridge te = getTileEntitySafely(world, pos, TileEntityEnergyBridge.class);

        if (te != null)
        {
            state = state.withProperty(ACTIVE, te.getIsActive());
        }

        return state;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 15;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        for (int i = 0; i < BridgeType.values().length; i++)
        {
            list.add(new ItemStack(this, 1, BridgeType.values()[i].getMeta()));
        }
    }

    public static enum BridgeType implements IStringSerializable
    {
        RESONATOR   (0, ReferenceNames.NAME_TILE_ENERGY_BRIDGE + "_resonator"),
        RECEIVER    (1, ReferenceNames.NAME_TILE_ENERGY_BRIDGE + "_receiver"),
        TRANSMITTER (2, ReferenceNames.NAME_TILE_ENERGY_BRIDGE + "_transmitter");

        private final String name;
        private final int meta;

        private BridgeType(int meta, String name)
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

        public static BridgeType fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : RESONATOR;
        }
    }
}
