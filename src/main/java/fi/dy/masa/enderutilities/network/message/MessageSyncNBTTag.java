package fi.dy.masa.enderutilities.network.message;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.PlacementProperties;
import io.netty.buffer.ByteBuf;

public class MessageSyncNBTTag implements IMessage
{
    private UUID uuid;
    private Type type;
    private NBTTagCompound tag;

    public MessageSyncNBTTag()
    {
    }

    public MessageSyncNBTTag(UUID uuid, Type type, NBTTagCompound tag)
    {
        this.uuid = uuid;
        this.type = type;
        this.tag = tag;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte((byte) this.type.ordinal());
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        ByteBufUtilsEU.writeNBTTagCompoundToBuffer(buf, this.tag);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            this.type = Type.fromOrdinal(buf.readByte());
            this.uuid = new UUID(buf.readLong(), buf.readLong());
            this.tag = ByteBufUtilsEU.readNBTTagCompoundFromBuffer(buf);
        }
        catch (IOException e)
        {
            EnderUtilities.logger.warn("MessageSyncNBTTag: Exception while reading data from buffer", e);
        }
    }

    public static class Handler implements IMessageHandler<MessageSyncNBTTag, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageSyncNBTTag message, MessageContext ctx)
        {
            if (ctx.side != Side.CLIENT)
            {
                EnderUtilities.logger.error("Wrong side in MessageSyncNBTTag: " + ctx.side);
                return null;
            }

            final Minecraft mc = FMLClientHandler.instance().getClient();

            if (mc == null)
            {
                EnderUtilities.logger.error("Minecraft was null in MessageSyncNBTTag");
                return null;
            }

            mc.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    processMessage(message);
                }
            });

            return null;
        }

        protected void processMessage(final MessageSyncNBTTag message)
        {
            if (message.type == Type.PLACEMENT_PROPERTIES_FULL)
            {
                PlacementProperties.getInstance().readAllDataForPlayerFromNBT(message.tag);
            }
            else if (message.type == Type.PLACEMENT_PROPERTIES_CURRENT)
            {
                PlacementProperties.getInstance().readSyncedItemData(message.uuid, message.tag);
            }
        }
    }

    public enum Type
    {
        PLACEMENT_PROPERTIES_FULL,
        PLACEMENT_PROPERTIES_CURRENT;

        public static Type fromOrdinal(int type)
        {
            return values()[type % values().length];
        }
    }
}
