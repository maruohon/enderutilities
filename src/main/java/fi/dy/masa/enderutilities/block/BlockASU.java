package fi.dy.masa.enderutilities.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.item.block.ItemBlockASU;
import fi.dy.masa.enderutilities.item.block.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
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
        ItemBlockEnderUtilities item = new ItemBlockASU(this);
        item.setHasPlacementProperties(true);
        item.addPlacementProperty(ReferenceNames.NAME_TILE_ENTITY_ASU + ".stack_limit", Constants.NBT.TAG_INT, 0, TileEntityASU.MAX_STACK_SIZE);
        item.addPlacementProperty(ReferenceNames.NAME_TILE_ENTITY_ASU + ".slots", Constants.NBT.TAG_BYTE, 1, 9);
        return item;
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityASU();
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
        ItemStack stack = new ItemStack(this.getItemDropped(state, rand, 0), 1, 0);
        TileEntityASU te = getTileEntitySafely(worldIn, pos, TileEntityASU.class);

        if (te != null)
        {
            return ItemUtils.storeTileEntityInStackWithCachedInventory(stack, te, addNBTLore, 9);
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
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntityASU te = getTileEntitySafely(world, pos, TileEntityASU.class);

        if (te != null)
        {
            state = state.withProperty(TIER, te.getInvSize());
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
}
