package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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
    public static boolean checkCanPlaceBlockAt(World world, BlockPos pos, int side, EntityPlayer player, ItemStack stack)
    {
        if (stack == null || (stack.getItem() instanceof ItemBlock) == false)
        {
            return false;
        }

        ItemBlock itemBlock = (ItemBlock)stack.getItem();
        Block block = world.getBlockState(pos).getBlock();
        EnumFacing facing = EnumFacing.getFront(side);

        if (block == Blocks.snow_layer && block.isReplaceable(world, pos) == true)
        {
            facing = EnumFacing.UP;
        }
        else if (block.isReplaceable(world, pos) == false)
        {
            pos = pos.offset(facing);
        }

        return world.canBlockBePlaced(itemBlock.block, pos, false, facing, (Entity)null, stack);
    }
}
