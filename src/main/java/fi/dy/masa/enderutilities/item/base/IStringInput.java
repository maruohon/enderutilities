package fi.dy.masa.enderutilities.item.base;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IStringInput
{
    public void handleString(EntityPlayer player, @Nullable ItemStack stack, String text);
}
