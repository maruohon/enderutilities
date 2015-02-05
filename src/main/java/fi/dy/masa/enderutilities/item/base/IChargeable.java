package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IChargeable
{
    public int getCapacity(ItemStack stack);

    public void setCapacity(ItemStack stack, int capacity);

    public int getCharge(ItemStack stack);

    public int addCharge(ItemStack stack, int amount, boolean simulate);

    public int useCharge(ItemStack stack, int amount, boolean simulate);

    public int useCharge(ItemStack stack, EntityPlayer player, int amount, boolean simulate);
}
