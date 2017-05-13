package fi.dy.masa.enderutilities.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.item.block.ItemBlockStorage;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.ItemUtils;

public class BlockBarrel extends BlockEnderUtilitiesInventory
{
    public static final PropertyBool CREATIVE    = PropertyBool.create("creative");
    public static final PropertyBool LABEL_UP    = PropertyBool.create("up");
    public static final PropertyBool LABEL_DOWN  = PropertyBool.create("down");
    public static final PropertyBool LABEL_FRONT = PropertyBool.create("front");
    public static final PropertyBool LABEL_BACK  = PropertyBool.create("back");
    public static final PropertyBool LABEL_LEFT  = PropertyBool.create("left");
    public static final PropertyBool LABEL_RIGHT = PropertyBool.create("right");

    public BlockBarrel(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(FACING_H, BlockEnderUtilities.DEFAULT_FACING).withProperty(CREATIVE, false)
                .withProperty(LABEL_UP, false).withProperty(LABEL_DOWN, false)
                .withProperty(LABEL_FRONT, false).withProperty(LABEL_BACK, false)
                .withProperty(LABEL_LEFT, false).withProperty(LABEL_RIGHT, false));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING_H, CREATIVE, LABEL_UP, LABEL_DOWN, LABEL_FRONT, LABEL_BACK, LABEL_LEFT, LABEL_RIGHT });
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return new ItemBlockStorage(this);
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] { ReferenceNames.NAME_TILE_ENTITY_BARREL };
    }

    @Override
    protected String[] generateTooltipNames()
    {
        return new String[] { ReferenceNames.NAME_TILE_ENTITY_BARREL };
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return 0;
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityBarrel();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking() == false || player.getHeldItem(hand) != null)
        {
            if (world.isRemote == false)
            {
                TileEntityBarrel te = getTileEntitySafely(world, pos, TileEntityBarrel.class);
                te.onRightClick(player, hand, side);
            }

            return true;
        }

        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        if (state.getValue(CREATIVE) || this.retainsContentsWhenBroken(world, pos, state))
        {
            world.updateComparatorOutputLevel(pos, this);
            world.removeTileEntity(pos);
        }
        else
        {
            TileEntityBarrel te = getTileEntitySafely(world, pos, TileEntityBarrel.class);

            if (te != null)
            {
                InventoryUtils.dropInventoryContentsInWorld(world, pos, te.getUpgradeInventory());

                // Fail-safe for not spawning hundreds of thousands of items in the world,
                // if there is no structure upgrade installed and a barrel is forcibly broken (in Creative mode for example).
                ItemStack stack = te.getBaseItemHandler().getStackInSlot(0);

                if (stack != null)
                {
                    EntityUtils.dropItemStacksInWorld(world, pos, stack, Math.min(stack.stackSize, 4096), true);
                }

                world.updateComparatorOutputLevel(pos, this);
                world.removeTileEntity(pos);
            }
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if (willHarvest && this.retainsContentsWhenBroken(world, pos, state))
        {
            this.onBlockHarvested(world, pos, state, player);
            return true;
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
    {
        // This will cascade down to getDrops()
        super.harvestBlock(world, player, pos, state, te, stack);

        world.setBlockToAir(pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        if (this.retainsContentsWhenBroken(world, pos, state))
        {
            List<ItemStack> items = new ArrayList<ItemStack>();
            items.add(this.getDroppedItemWithNBT(world, pos, state, false));
            return items;
        }
        else
        {
            return super.getDrops(world, pos, state, fortune);
        }
    }

    private boolean retainsContentsWhenBroken(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        TileEntityBarrel te = getTileEntitySafely(world, pos, TileEntityBarrel.class);
        return te != null && te.retainsContentsWhenBroken();
    }

    protected ItemStack getDroppedItemWithNBT(IBlockAccess worldIn, BlockPos pos, IBlockState state, boolean addNBTLore)
    {
        Random rand = worldIn instanceof World ? ((World) worldIn).rand : RANDOM;
        ItemStack stack = new ItemStack(this.getItemDropped(state, rand, 0), 1, 0);
        TileEntityEnderUtilities te = getTileEntitySafely(worldIn, pos, TileEntityEnderUtilities.class);

        if (te != null)
        {
            return ItemUtils.storeTileEntityInStackWithCachedInventory(stack, te, addNBTLore, 9);
        }

        return stack;
    }

    @Override
    @Deprecated
    public float getBlockHardness(IBlockState state, World world, BlockPos pos)
    {
        if (state.getValue(CREATIVE))
        {
            return -1f;
        }

        TileEntityBarrel te = getTileEntitySafely(world, pos, TileEntityBarrel.class);

        if (te != null && te.retainsContentsWhenBroken() == false && te.isOverSpillCapacity())
        {
            return -1f;
        }

        return super.getBlockHardness(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(CREATIVE) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(CREATIVE, (meta & 0x1) != 0);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntityBarrel te = getTileEntitySafely(world, pos, TileEntityBarrel.class);

        if (te != null)
        {
            int labelMask = te.getLabelMask(true);
            EnumFacing facing = te.getFacing();
            state = state.withProperty(FACING_H, facing);
            state = state.withProperty(LABEL_UP,    (labelMask & (1 << EnumFacing.UP.getIndex())) != 0);
            state = state.withProperty(LABEL_DOWN,  (labelMask & (1 << EnumFacing.DOWN.getIndex())) != 0);
            state = state.withProperty(LABEL_FRONT, (labelMask & (1 << facing.getIndex())) != 0);
            state = state.withProperty(LABEL_BACK,  (labelMask & (1 << facing.getOpposite().getIndex())) != 0);
            state = state.withProperty(LABEL_LEFT,  (labelMask & (1 << facing.rotateY().getIndex())) != 0);
            state = state.withProperty(LABEL_RIGHT, (labelMask & (1 << facing.rotateYCCW().getIndex())) != 0);
        }

        return state;
    }
}
