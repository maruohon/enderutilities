package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public interface ILocationBound
{
    /**
     * Gets the target information from the ItemStack
     */
    public NBTHelperTarget getTarget(ItemStack stack);

    /**
     *  Saves the given location to the ItemStack
     */
    public void setTarget(ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing face, double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeAngle);
}
