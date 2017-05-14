package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.IStringInput;
import fi.dy.masa.enderutilities.util.EntityUtils;
import io.netty.buffer.ByteBuf;

public class MessageSendString implements IMessage
{
    private Type type;
    private String text;

    public MessageSendString()
    {
    }

    public MessageSendString(Type type, String text)
    {
        this.type = type;
        this.text = text;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.type = Type.values()[buf.readByte() % Type.values().length];
        this.text = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.type.ordinal());
        ByteBufUtils.writeUTF8String(buf, this.text);
    }

    public static class Handler implements IMessageHandler<MessageSendString, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageSendString message, MessageContext ctx)
        {
            if (ctx.side != Side.SERVER)
            {
                EnderUtilities.logger.error("Wrong side in MessageSendString: " + ctx.side);
                return null;
            }

            final EntityPlayerMP sendingPlayer = ctx.getServerHandler().player;
            if (sendingPlayer == null)
            {
                EnderUtilities.logger.error("Sending player was null in MessageSendString");
                return null;
            }

            final WorldServer playerWorldServer = sendingPlayer.getServerWorld();
            if (playerWorldServer == null)
            {
                EnderUtilities.logger.error("World was null in MessageSendString");
                return null;
            }

            playerWorldServer.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    processMessage(message, sendingPlayer);
                }
            });

            return null;
        }

        protected void processMessage(final MessageSendString message, EntityPlayer player)
        {
            if (message.type == Type.ITEM)
            {
                ItemStack stack = EntityUtils.getHeldItemOfType(player, IStringInput.class);

                if (stack.isEmpty() == false)
                {
                    ((IStringInput) stack.getItem()).handleString(player, stack, message.text);
                }
            }
            else if (player.openContainer instanceof IStringInput)
            {
                ((IStringInput) player.openContainer).handleString(player, null, message.text);
            }
        }
    }

    public static enum Type
    {
        ITEM,
        BLOCK;
    }
}
