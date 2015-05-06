package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockUtils
{
    public static boolean blockMatches(World world, BlockPos pos, Block requiredBlock, int requiredMeta, Class <? extends TileEntity> TEClass, ForgeDirection requiredOrientation)
    {
        Block block = world.getBlock(pos.posX, pos.posY, pos.posZ);
        int meta = world.getBlockMetadata(pos.posX, pos.posY, pos.posZ);
        TileEntity te = world.getTileEntity(pos.posX, pos.posY, pos.posZ);

        if (block == requiredBlock && meta == requiredMeta && te != null && TEClass.isAssignableFrom(te.getClass()) == true
            && (requiredOrientation == ForgeDirection.UNKNOWN
                || (te instanceof TileEntityEnderUtilities && ForgeDirection.getOrientation(((TileEntityEnderUtilities)te).getRotation()).equals(requiredOrientation))))
        {
            return true;
        }

        return false;
    }
}
