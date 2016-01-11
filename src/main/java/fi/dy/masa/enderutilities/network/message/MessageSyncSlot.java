package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerLargeStacks;
import io.netty.buffer.ByteBuf;

public class MessageSyncSlot implements IMessage, IMessageHandler<MessageSyncSlot, IMessage>
{
    private int windowId;
    private int slotNum;
    private ItemStack stack;

    public MessageSyncSlot()
    {
    }

    public MessageSyncSlot(int windowId, int slotNum, ItemStack stack)
    {
        this.windowId = windowId;
        this.slotNum = slotNum;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.windowId = buf.readByte();
        this.slotNum = buf.readShort();
        this.stack = ByteBufUtils.readItemStackFromBuffer(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.windowId);
        buf.writeShort(this.slotNum);
        ByteBufUtils.writeItemStackToBuffer(buf, this.stack);
    }

    @Override
    public IMessage onMessage(MessageSyncSlot message, MessageContext ctx)
    {
        EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
        if (player.openContainer instanceof ContainerLargeStacks && message.windowId == player.openContainer.windowId)
        {
            player.openContainer.putStackInSlot(message.slotNum, message.stack);
        }

        return null;
    }
}
