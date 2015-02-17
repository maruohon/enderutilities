package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IKeyBound
{
    /**
     * Performs some action on the item, based on the key(s) that were pressed.
     * @param player The player performing this action
     * @param stack The target ItemStack
     * @param key The mod-specific key id, including modifier bits.
     */
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key);
}
