package fi.dy.masa.enderutilities.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.item.block.ItemBlockASU;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.tileentity.ITieredStorage;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.util.ItemUtils;

public class BlockASU extends BlockEnderUtilitiesInventory
{
    public static final PropertyInteger TIER = PropertyInteger.create("tier", 1, 9);

    public BlockASU(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState().withProperty(TIER, 1));
        this.setUnlocalizedName(Reference.MOD_ID + "." + name);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { TIER });
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return new ItemBlockASU(this);
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityASU();
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        if (world.isRemote == false)
        {
            TileEntity te = getTileEntitySafely(world, pos, TileEntity.class);

            if (te instanceof ITieredStorage)
            {
                ((ITieredStorage) te).setStorageTier(state.getValue(TIER));
            }
        }
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
        ItemStack stack = new ItemStack(this.getItemDropped(state, rand, 0), 1, state.getValue(TIER) - 1);
        TileEntityASU te = getTileEntitySafely(worldIn, pos, TileEntityASU.class);

        if (te != null)
        {
            return ItemUtils.storeTileEntityInStack(stack, te, addNBTLore);
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
        return this.getDefaultState().withProperty(TIER, (meta % 9) + 1);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TIER) - 1;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
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
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < 9; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }
}
