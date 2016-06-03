package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IStringInput
{
    public void handleString(EntityPlayer player, ItemStack stack, String text);
}
