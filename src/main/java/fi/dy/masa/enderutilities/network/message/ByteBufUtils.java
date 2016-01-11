package fi.dy.masa.enderutilities.network.message;

import java.io.IOException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.EnderUtilities;
import io.netty.buffer.ByteBuf;

public class ByteBufUtils
{
    public static void writeItemStackToBuffer(ByteBuf buf, ItemStack stack)
    {
        if (stack == null)
        {
            buf.writeShort(-1);
            return;
        }

        buf.writeShort(Item.getIdFromItem(stack.getItem()));
        buf.writeShort(stack.getItemDamage());
        buf.writeInt(stack.stackSize);

        NBTTagCompound tag = null;
        if (stack.getItem().isDamageable() == true || stack.getItem().getShareTag() == true)
        {
            tag = stack.getTagCompound();
        }

        writeNBTTagCompoundToBuffer(buf, tag);
    }

    public static ItemStack readItemStackFromBuffer(ByteBuf buf)
    {
        ItemStack stack = null;
        short id = buf.readShort();

        if (id >= 0)
        {
            short meta = buf.readShort();
            int stackSize = buf.readInt();
            stack = new ItemStack(Item.getItemById(id), stackSize, meta);
            stack.setTagCompound(readNBTTagCompoundFromBuffer(buf));
        }

        return stack;
    }

    public static void writeNBTTagCompoundToBuffer(ByteBuf buf, NBTTagCompound tag)
    {
        if (tag == null)
        {
            buf.writeShort(-1);
            return;
        }

        try
        {
            byte[] byteArr = CompressedStreamTools.compress(tag);
            buf.writeShort((short)byteArr.length);
            buf.writeBytes(byteArr);
        }
        catch (IOException e)
        {
            EnderUtilities.logger.error("IOException while trying to write a NBTTagCompound to ByteBuf");
        }
    }

    public static NBTTagCompound readNBTTagCompoundFromBuffer(ByteBuf buf)
    {
        short length = buf.readShort();

        if (length < 0)
        {
            return null;
        }

        byte[] byteArr = new byte[length];
        buf.readBytes(byteArr);

        NBTTagCompound tag = null;
        try
        {
            tag = CompressedStreamTools.func_152457_a(byteArr, new NBTSizeTracker(2097152L));
        }
        catch (IOException e)
        {
            EnderUtilities.logger.error("IOException while trying to read a NBTTagCompound from ByteBuf");
        }

        return tag;
    }
}
