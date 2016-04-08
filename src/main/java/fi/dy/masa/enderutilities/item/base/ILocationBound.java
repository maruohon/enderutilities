package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public interface ILocationBound
{
    /**
     * Gets the target information from the ItemStack
     */
    NBTHelperTarget getTarget(ItemStack stack);

    /**
     *  Saves the given location to the ItemStack
     */
    void setTarget(ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing side, double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeAngle);

    /**
     * Returns whether to include the targetDisplayName in the item's name
     */
    boolean shouldDisplayTargetName(ItemStack stack);

    /**
     * Returns the display name for the bound target.
     */
    String getTargetDisplayName(ItemStack stack);
}
