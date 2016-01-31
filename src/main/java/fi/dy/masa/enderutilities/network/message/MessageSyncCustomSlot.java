package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.ICustomSlotSync;
import io.netty.buffer.ByteBuf;

public class MessageSyncCustomSlot implements IMessage, IMessageHandler<MessageSyncCustomSlot, IMessage>
{
    private int windowId;
    private int typeId;
    private int slotNum;
    private ItemStack stack;

    public MessageSyncCustomSlot()
    {
    }

    public MessageSyncCustomSlot(int windowId, int typeId, int slotNum, ItemStack stack)
    {
        this.windowId = windowId;
        this.typeId = typeId;
        this.slotNum = slotNum;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.windowId = buf.readByte();
        this.typeId = buf.readByte();
        this.slotNum = buf.readShort();
        this.stack = ByteBufUtils.readItemStackFromBuffer(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.windowId);
        buf.writeByte(this.typeId);
        buf.writeShort(this.slotNum);
        ByteBufUtils.writeItemStackToBuffer(buf, this.stack);
    }

    @Override
    public IMessage onMessage(MessageSyncCustomSlot message, MessageContext ctx)
    {
        EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
        if (player.openContainer instanceof ICustomSlotSync && message.windowId == player.openContainer.windowId)
        {
            ICustomSlotSync target = (ICustomSlotSync)player.openContainer;
            target.putCustomStack(message.typeId, message.slotNum, message.stack);
        }

        return null;
    }
}
