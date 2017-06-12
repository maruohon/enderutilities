package fi.dy.masa.enderutilities.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.item.block.ItemBlockStorage;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityMSU;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.TileUtils;

public class BlockMSU extends BlockEnderUtilitiesInventory
{
    public static final PropertyEnum<BlockMSU.EnumStorageType> TYPE =
            PropertyEnum.<BlockMSU.EnumStorageType>create("type", BlockMSU.EnumStorageType.class);

    public BlockMSU(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(CREATIVE, false)
                .withProperty(TYPE, BlockMSU.EnumStorageType.MASSIVE_STORAGE_UNIT));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { CREATIVE, TYPE });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_MSU + "_0",
                ReferenceNames.NAME_TILE_MSU + "_1"
        };
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return new ItemBlockStorage(this);
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        TileEntityMSU te = new TileEntityMSU();
        te.setStorageTier(state.getValue(TYPE).getMeta());
        return te;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if (willHarvest)
        {
            this.onBlockHarvested(world, pos, state, player);
            return true;
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
    {
        // This will cascade down to getDrops()
        super.harvestBlock(worldIn, player, pos, state, te, stack);

        worldIn.setBlockToAir(pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess worldIn, BlockPos pos, IBlockState state, int fortune)
    {
        List<ItemStack> items = new ArrayList<ItemStack>();

        items.add(this.getDroppedItemWithNBT(worldIn, pos, state, false));

        return items;
    }

    protected ItemStack getDroppedItemWithNBT(IBlockAccess worldIn, BlockPos pos, IBlockState state, boolean addNBTLore)
    {
        Random rand = worldIn instanceof World ? ((World) worldIn).rand : RANDOM;
        ItemStack stack = new ItemStack(this.getItemDropped(state, rand, 0), 1, state.getValue(TYPE).getMeta());
        TileEntityMSU te = getTileEntitySafely(worldIn, pos, TileEntityMSU.class);

        if (te != null && InventoryUtils.getFirstNonEmptySlot(te.getBaseItemHandler()) != -1)
        {
            return TileUtils.storeTileEntityInStackWithCachedInventory(stack, te, addNBTLore, 9);
        }

        return stack;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        worldIn.updateComparatorOutputLevel(pos, this);
        worldIn.removeTileEntity(pos);
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
    @Deprecated
    public float getBlockHardness(IBlockState state, World world, BlockPos pos)
    {
        TileEntityMSU te = getTileEntitySafely(world, pos, TileEntityMSU.class);

        if (te != null && te.isCreative())
        {
            return -1f;
        }

        return super.getBlockHardness(state, world, pos);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntityMSU te = getTileEntitySafely(world, pos, TileEntityMSU.class);

        if (te != null)
        {
            state = state.withProperty(CREATIVE, te.isCreative());
        }

        return state;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation)
    {
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror)
    {
        return state;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        for (int i = 0; i < EnumStorageType.values().length; i++)
        {
            list.add(new ItemStack(this, 1, EnumStorageType.values()[i].getMeta()));
        }
    }

    public static enum EnumStorageType implements IStringSerializable
    {
        MASSIVE_STORAGE_UNIT    (0, ReferenceNames.NAME_TILE_MSU + "_0"),
        MASSIVE_STORAGE_BUNDLE  (1, ReferenceNames.NAME_TILE_MSU + "_1");

        private final String name;
        private final int meta;

        private EnumStorageType(int meta, String nameBase)
        {
            this.meta = meta;
            this.name = nameBase;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public static EnumStorageType fromMeta(int meta)
        {
            return values()[meta % values().length];
        }
    }
}
