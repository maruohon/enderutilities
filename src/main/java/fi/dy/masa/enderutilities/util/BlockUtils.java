package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockUtils
{
    public static boolean blockMatches(World world, BlockPosEU pos, Block requiredBlock, int requiredMeta, Class <? extends TileEntity> TEClass, EnumFacing requiredFacing)
    {
        IBlockState iBlockState = world.getBlockState(pos.getBlockPos());
        Block block = iBlockState.getBlock();
        int meta = block.getMetaFromState(iBlockState);
        TileEntity te = world.getTileEntity(pos.getBlockPos());

        if (block == requiredBlock && meta == requiredMeta && te != null && TEClass.isAssignableFrom(te.getClass()) == true
            && (requiredFacing == null || (te instanceof TileEntityEnderUtilities && EnumFacing.getFront(((TileEntityEnderUtilities)te).getRotation()).equals(requiredFacing))))
        {
            return true;
        }

        return false;
    }
}
