package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockUtils
{
    /**
     * Check if the block in the world matches the one asked for.
     * Use OreDictionary.WILDCARD_VALUE for the requiredMeta to ignore block meta.
     * Use null for the TEClass if the block doesn't have a TileEntity.
     * If the block has a TE of type TileEntityEnderUtilities but the orientation doesn't matter,
     * then give ForgeDirection.UNKNOWN as the requiredOrientation.
     */
    public static boolean blockMatches(World world, BlockPos pos, Block requiredBlock, int requiredMeta, Class <? extends TileEntity> TEClass, EnumFacing requiredOrientation)
    {
        IBlockState iBlockState = world.getBlockState(pos);
        Block block = iBlockState.getBlock();
        int meta = block.getMetaFromState(iBlockState);
        TileEntity te = world.getTileEntity(pos);

        if (block != requiredBlock || (meta != requiredMeta && requiredMeta != OreDictionary.WILDCARD_VALUE))
        {
            return false;
        }

        if (te == null && TEClass == null)
        {
            return true;
        }

        if (te != null && TEClass != null && TEClass.isAssignableFrom(te.getClass()) == true && (requiredOrientation == null
            || (te instanceof TileEntityEnderUtilities && EnumFacing.getFront(((TileEntityEnderUtilities)te).getRotation()).equals(requiredOrientation))))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if the block in the given ItemStack stack can be placed in the given position.
     * Note: This method is a functional copy of ItemBlock.func_150936_a() which is client side only.
     */
    public static boolean checkCanPlaceBlockAt(World world, BlockPos pos, EnumFacing side, Block blockNew, ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        Block blockExisting = world.getBlockState(pos).getBlock();

        if (blockExisting == Blocks.SNOW_LAYER && blockExisting.isReplaceable(world, pos) == true)
        {
            side = EnumFacing.UP;
        }
        else if (blockExisting.isReplaceable(world, pos) == false)
        {
            pos = pos.offset(side);
        }

        return world.canBlockBePlaced(blockNew, pos, false, side, (Entity)null, stack);
    }
}
