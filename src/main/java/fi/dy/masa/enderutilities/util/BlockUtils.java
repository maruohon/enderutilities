package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
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
    public static boolean blockMatches(World world, BlockPosEU pos, Block requiredBlock, int requiredMeta, Class <? extends TileEntity> TEClass, ForgeDirection requiredOrientation)
    {
        Block block = world.getBlock(pos.posX, pos.posY, pos.posZ);
        int meta = world.getBlockMetadata(pos.posX, pos.posY, pos.posZ);
        TileEntity te = world.getTileEntity(pos.posX, pos.posY, pos.posZ);

        if (block != requiredBlock || (meta != requiredMeta && requiredMeta != OreDictionary.WILDCARD_VALUE))
        {
            return false;
        }

        if (te == null && TEClass == null)
        {
            return true;
        }

        if (te != null && TEClass != null && TEClass.isAssignableFrom(te.getClass()) == true && (requiredOrientation == ForgeDirection.UNKNOWN
            || (te instanceof TileEntityEnderUtilities && ForgeDirection.getOrientation(((TileEntityEnderUtilities)te).getRotation()).equals(requiredOrientation))))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if the block in the given ItemStack stack can be placed in the given position.
     * Note: This method is a functional copy of ItemBlock.func_150936_a() which is client side only.
     */
    public static boolean checkCanPlaceBlockAt(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack)
    {
        if (stack == null || (stack.getItem() instanceof ItemBlock) == false)
        {
            return false;
        }

        ItemBlock itemBlock = (ItemBlock)stack.getItem();
        Block block = world.getBlock(x, y, z);
        if (block == Blocks.snow_layer)
        {
            side = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z))
        {
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            x += dir.offsetX;
            y += dir.offsetY;
            z += dir.offsetZ;
        }

        return world.canPlaceEntityOnSide(itemBlock.field_150939_a, x, y, z, false, side, (Entity)null, stack);
    }
}
