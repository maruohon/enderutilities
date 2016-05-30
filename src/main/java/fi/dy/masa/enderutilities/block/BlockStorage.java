package fi.dy.masa.enderutilities.block;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.ITieredStorage;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;

public class BlockStorage extends BlockEnderUtilitiesInventory
{
    protected static final AxisAlignedBB SINGLE_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);

    public static final PropertyEnum<BlockStorage.EnumStorageType> TYPE =
            PropertyEnum.<BlockStorage.EnumStorageType>create("type", BlockStorage.EnumStorageType.class);

    public BlockStorage(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(TYPE, BlockStorage.EnumStorageType.MEMORY_CHEST_0)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { TYPE, FACING });
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return SINGLE_CHEST_AABB;
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_0",
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_1",
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_2",
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_0",
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_1",
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_2"
        };
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        TileEntityEnderUtilities te = new TileEntityMemoryChest();

        switch(state.getValue(TYPE))
        {
            case MEMORY_CHEST_0:    te = new TileEntityMemoryChest(); break;
            case MEMORY_CHEST_1:    te = new TileEntityMemoryChest(); break;
            case MEMORY_CHEST_2:    te = new TileEntityMemoryChest(); break;
            case HANDY_CHEST_0:     te = new TileEntityHandyChest(); break;
            case HANDY_CHEST_1:     te = new TileEntityHandyChest(); break;
            case HANDY_CHEST_2:     te = new TileEntityHandyChest(); break;
        }

        te.setFacing(state.getValue(FACING));

        return te;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (worldIn.isRemote == true)
        {
            return;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof ITieredStorage)
        {
            // FIXME add properties for the type/tier
            int meta = state.getBlock().getMetaFromState(state);

            // FIXME This will only work as long as there are three tiers of every type of storage...
            ((ITieredStorage)te).setStorageTier(meta % 3);
        }
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        if (worldIn.isRemote == true)
        {
            return;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityHandyChest)
        {
            ((TileEntityHandyChest)te).onLeftClickBlock(playerIn);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, EnumStorageType.fromMeta(meta));
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
            EnumFacing facing = ((TileEntityEnderUtilities)te).getFacing();
            if (facing.getAxis().isHorizontal() == true)
            {
                state = state.withProperty(FACING, facing);
            }
        }

        return state;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < 6; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public static enum EnumStorageType implements IStringSerializable
    {
        MEMORY_CHEST_0 (ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_0"),
        MEMORY_CHEST_1 (ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_1"),
        MEMORY_CHEST_2 (ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_2"),
        HANDY_CHEST_0 (ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_0"),
        HANDY_CHEST_1 (ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_1"),
        HANDY_CHEST_2 (ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_2");

        private final String name;

        private EnumStorageType(String name)
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

        public static EnumStorageType fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : MEMORY_CHEST_0;
        }
    }
}
