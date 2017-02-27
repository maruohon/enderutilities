package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class BlockMachine extends BlockEnderUtilitiesInventory
{
    public static final PropertyEnum<BlockMachine.EnumMachineType> TYPE =
            PropertyEnum.<BlockMachine.EnumMachineType>create("type", BlockMachine.EnumMachineType.class);

    public BlockMachine(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(TYPE, BlockMachine.EnumMachineType.ENDER_INFUSER)
                .withProperty(FACING_H, BlockEnderUtilities.DEFAULT_FACING));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { TYPE, FACING_H });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER,
                ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION,
                ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION,
                ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED
        };
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        switch (state.getValue(TYPE))
        {
            case CREATION_STATION:  return new TileEntityCreationStation();
            case ENDER_INFUSER:     return new TileEntityEnderInfuser();
            case QUICK_STACKER:     return new TileEntityQuickStackerAdvanced();
            case TOOL_WORKSTATION:  return new TileEntityToolWorkstation();
        }

        return new TileEntityEnderInfuser();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        EnumMachineType machine = state.getValue(TYPE);

        if (machine == EnumMachineType.CREATION_STATION)
        {
            TileEntityCreationStation te = getTileEntitySafely(world, pos, TileEntityCreationStation.class);

            if (te != null)
            {
                // Drop the items from the furnace inventories
                InventoryUtils.dropInventoryContentsInWorld(world, pos, te.getFurnaceInventory());
            }
        }
        else if (machine == EnumMachineType.TOOL_WORKSTATION)
        {
            TileEntityToolWorkstation te = getTileEntitySafely(world, pos, TileEntityToolWorkstation.class);

            if (te != null)
            {
                // Drop the items from the tool slot and the rename slot
                InventoryUtils.dropInventoryContentsInWorld(world, pos, te.getToolSlotInventory());
                InventoryUtils.dropInventoryContentsInWorld(world, pos, te.getRenameSlotInventory());
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        if (state.getValue(TYPE) == EnumMachineType.CREATION_STATION)
        {
            return 10;
        }

        return super.getLightValue(state, worldIn, pos);
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
    public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand)
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
        for (int meta = 0; meta < EnumMachineType.values().length; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public static enum EnumMachineType implements IStringSerializable
    {
        ENDER_INFUSER       (0, ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER),
        TOOL_WORKSTATION    (1, ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION),
        CREATION_STATION    (2, ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION),
        QUICK_STACKER       (3, ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED);

        private final String name;
        private final int meta;

        private EnumMachineType(int meta, String name)
        {
            this.name = name;
            this.meta = meta;
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
            return meta < values().length ? values()[meta] : ENDER_INFUSER;
        }
    }
}
