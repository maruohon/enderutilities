package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public abstract class BlockEnderUtilitiesTileEntity extends BlockEnderUtilities
{
    public BlockEnderUtilitiesTileEntity(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return this.createTileEntityInstance(world, state);
    }

    protected abstract TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state);

    public boolean isTileEntityValid(TileEntity te)
    {
        return te != null && te.isInvalid() == false;
    }

    protected EnumFacing getPlacementFacing(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        return placer.getHorizontalFacing().getOpposite();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntityEnderUtilities te = getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

        if (te == null)
        {
            return;
        }

        NBTTagCompound nbt = stack.getTagCompound();

        // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
        if (nbt != null && nbt.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
        {
            te.readFromNBTCustom(nbt.getCompoundTag("BlockEntityTag"));
        }
        else
        {
            if (placer instanceof EntityPlayer)
            {
                te.setOwner((EntityPlayer)placer);
            }

            if (te instanceof TileEntityEnderUtilitiesInventory && stack.hasDisplayName())
            {
                ((TileEntityEnderUtilitiesInventory) te).setInventoryName(stack.getDisplayName());
            }
        }

        te.setFacing(this.getPlacementFacing(world, pos, state, placer, stack));

        // This is to fix the modular inventories not loading properly when placed from a Ctrl + pick-blocked stack
        te.onLoad();
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn)
    {
        if (world.isRemote == false)
        {
            TileEntityEnderUtilities te = getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

            if (te != null)
            {
                te.onLeftClickBlock(playerIn);
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (worldIn.isRemote == false)
        {
            TileEntityEnderUtilities te = getTileEntitySafely(worldIn, pos, TileEntityEnderUtilities.class);

            if (te != null)
            {
                te.onNeighborBlockChange(worldIn, pos, state, blockIn);
            }
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntityEnderUtilities te = getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

        if (te != null)
        {
            EnumFacing facing = te.getFacing();

            // Check that we don't try to set a vertical facing to a horizontal-only property
            if (this.propFacing == FACING || facing.getAxis() != EnumFacing.Axis.Y)
            {
                state = state.withProperty(this.propFacing, te.getFacing());
            }
        }

        return state;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation)
    {
        return state.withProperty(this.propFacing, rotation.rotate(state.getValue(this.propFacing)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror)
    {
        return state.withRotation(mirror.toRotation(state.getValue(this.propFacing)));
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        TileEntityEnderUtilities te = getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

        if (te != null)
        {
            te.rotate(Rotation.CLOCKWISE_90);
            IBlockState state = world.getBlockState(pos).getActualState(world, pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }

        return false;
    }
}
