package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IKeyBoundUnselected
{
    public void doUnselectedKeyAction(EntityPlayer player, ItemStack stack, int key);
}
