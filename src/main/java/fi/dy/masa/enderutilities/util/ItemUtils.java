package fi.dy.masa.enderutilities.util;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;

public class ItemUtils
{
    public static ItemStack storeTileEntityInStack(ItemStack stack, TileEntity te, boolean addNBTLore)
    {
        NBTTagCompound nbt = te.writeToNBT(new NBTTagCompound());
        nbt.removeTag("x");
        nbt.removeTag("y");
        nbt.removeTag("z");

        if (stack.getItem() == Items.SKULL && nbt.hasKey("Owner"))
        {
            NBTTagCompound tagOwner = nbt.getCompoundTag("Owner");
            NBTTagCompound nbtOwner2 = new NBTTagCompound();
            nbtOwner2.setTag("SkullOwner", tagOwner);
            stack.setTagCompound(nbtOwner2);
        }
        else
        {
            stack.setTagInfo("BlockEntityTag", nbt);

            if (addNBTLore)
            {
                NBTTagCompound tagDisplay = new NBTTagCompound();
                NBTTagList tagLore = new NBTTagList();
                tagLore.appendTag(new NBTTagString("(+NBT)"));
                tagDisplay.setTag("Lore", tagLore);
                stack.setTagInfo("display", tagDisplay);
            }
        }

        return stack;
    }
}
