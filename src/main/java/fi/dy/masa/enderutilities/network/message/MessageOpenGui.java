package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import io.netty.buffer.ByteBuf;

public class MessageOpenGui implements IMessage, IMessageHandler<MessageOpenGui, IMessage>
{
    private int dimension;
    private double posX;
    private double posY;
    private double posZ;
    private int guiId;

    public MessageOpenGui()
    {
    }

    public MessageOpenGui(int dim, double x, double y, double z, int guiId)
    {
        this.dimension = dim;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.guiId = guiId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.dimension = buf.readInt();
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.guiId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.dimension);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeInt(this.guiId);
    }

    @Override
    public IMessage onMessage(MessageOpenGui message, MessageContext ctx)
    {
        EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
        World world = MinecraftServer.getServer().worldServerForDimension(message.dimension);

        if (player != null)
        {
            switch(message.guiId)
            {
                case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                    ItemStack stack = ItemHandyBag.getOpenableBag(player);
                    if (stack != null)
                    {
                        player.openGui(EnderUtilities.instance, message.guiId, world, (int)player.posX, (int)player.posY, (int)player.posZ);
                    }
                    break;
                default:
            }
        }

        return null;
    }

}
