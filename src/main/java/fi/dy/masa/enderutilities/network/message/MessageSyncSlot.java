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
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import io.netty.buffer.ByteBuf;

public class MessageSyncSlot implements IMessage
{
    private int windowId;
    private int slotNum;
    private ItemStack stack = ItemStack.EMPTY;

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
        try
        {
            this.windowId = buf.readByte();
            this.slotNum = buf.readShort();
            this.stack = ByteBufUtilsEU.readItemStackFromBuffer(buf);
        }
        catch (IOException e)
        {
            EnderUtilities.logger.warn("MessageSyncSlot: Exception while reading data from buffer", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.windowId);
        buf.writeShort(this.slotNum);
        ByteBufUtilsEU.writeItemStackToBuffer(buf, this.stack);
    }

    public static class Handler implements IMessageHandler<MessageSyncSlot, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageSyncSlot message, MessageContext ctx)
        {
            if (ctx.side != Side.CLIENT)
            {
                EnderUtilities.logger.error("Wrong side in MessageSyncSlot: " + ctx.side);
                return null;
            }

            Minecraft mc = FMLClientHandler.instance().getClient();
            final EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);

            if (mc == null || player == null)
            {
                EnderUtilities.logger.error("Minecraft or player was null in MessageSyncSlot");
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

        protected void processMessage(final MessageSyncSlot message, EntityPlayer player)
        {
            if (player.openContainer instanceof ContainerEnderUtilities && message.windowId == player.openContainer.windowId)
            {
                //System.out.printf("MessageSyncSlot - slot: %3d stack: %s\n", message.slotNum, message.stack);
                ((ContainerEnderUtilities) player.openContainer).syncStackInSlot(message.slotNum, message.stack);
            }
        }
    }
}
