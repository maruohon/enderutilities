package fi.dy.masa.enderutilities.entity.base;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public interface IItemData
{
    public int getItemDamage(Entity entity);

    public NBTTagCompound getTagCompound(Entity entity);
}
