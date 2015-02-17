package fi.dy.masa.enderutilities.item.base;

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
    public void setTarget(ItemStack stack, int x, int y, int z, int dim, int blockFace, double hitX, double hitY, double hitZ, boolean doHitOffset);
}
