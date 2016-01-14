package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

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
    public void setTarget(ItemStack stack, EntityPlayer player, int x, int y, int z, int side, double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeAngle);

    /**
     * Returns the display name for the bound target.
     */
    public String getTargetDisplayName(ItemStack stack);
}
