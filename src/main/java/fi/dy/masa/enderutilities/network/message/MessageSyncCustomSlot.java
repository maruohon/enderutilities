package fi.dy.masa.enderutilities.network.message;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

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
        try
        {
            this.windowId = buf.readByte();
            this.typeId = buf.readByte();
            this.slotNum = buf.readShort();
            this.stack = ByteBufUtils.readItemStackFromBuffer(buf);
        }
        catch (IOException e)
        {
            EnderUtilities.logger.warn("MessageSyncCustomSlot: Exception while reading data from buffer");
            e.printStackTrace();
        }
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
    public IMessage onMessage(final MessageSyncCustomSlot message, MessageContext ctx)
    {
        if (ctx.side != Side.CLIENT)
        {
            EnderUtilities.logger.error("Wrong side in MessageSyncCustomSlot: " + ctx.side);
            return null;
        }

        Minecraft mc = FMLClientHandler.instance().getClient();
        final EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
        if (mc == null || player == null)
        {
            EnderUtilities.logger.error("Minecraft or player was null in MessageSyncCustomSlot");
            return null;
        }

        mc.addScheduledTask(new Runnable()
        {
            public void run()
            {
                processMessage(message, player);
            }
        });

        return null;
    }

    protected void processMessage(final MessageSyncCustomSlot message, EntityPlayer player)
    {
        if (player.openContainer instanceof ICustomSlotSync && message.windowId == player.openContainer.windowId)
        {
            ICustomSlotSync target = (ICustomSlotSync)player.openContainer;
            target.putCustomStack(message.typeId, message.slotNum, message.stack);
        }
    }
}
