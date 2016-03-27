package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageOpenGui implements IMessage
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

    public static class Handler implements IMessageHandler<MessageOpenGui, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageOpenGui message, MessageContext ctx)
        {
            if (ctx.side != Side.SERVER)
            {
                EnderUtilities.logger.error("Wrong side in MessageOpenGui: " + ctx.side);
                return null;
            }

            final EntityPlayerMP sendingPlayer = ctx.getServerHandler().playerEntity;
            if (sendingPlayer == null)
            {
                EnderUtilities.logger.error("player was null in MessageOpenGui");
                return null;
            }

            final WorldServer playerWorldServer = sendingPlayer.getServerWorld();
            if (playerWorldServer == null)
            {
                EnderUtilities.logger.error("World was null in MessageOpenGui");
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

        protected void processMessage(final MessageOpenGui message, EntityPlayer player)
        {
            switch(message.guiId)
            {
                case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                    ItemStack stack = ItemHandyBag.getOpenableBag(player);
                    if (stack != null)
                    {
                        World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.dimension);
                        player.openGui(EnderUtilities.instance, message.guiId, world, (int)player.posX, (int)player.posY, (int)player.posZ);
                    }
                    break;
                default:
            }
        }
    }
}
