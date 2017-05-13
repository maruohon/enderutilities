package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import io.netty.buffer.ByteBuf;

public class MessageOpenGui implements IMessage
{
    private int dimension;
    private int guiId;

    public MessageOpenGui()
    {
    }

    public MessageOpenGui(int dim, int guiId)
    {
        this.dimension = dim;
        this.guiId = guiId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.dimension = buf.readInt();
        this.guiId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.dimension);
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

            final EntityPlayerMP sendingPlayer = ctx.getServerHandler().player;
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
                        // These two lines are to fix the UUID being missing the first time the GUI opens,
                        // if the item is grabbed from the creative inventory or from JEI or from /give
                        NBTUtils.getUUIDFromItemStack(stack, "UUID", true);
                        player.openContainer.detectAndSendChanges();
                        player.openGui(EnderUtilities.instance, message.guiId, player.getEntityWorld(),
                                (int)player.posX, (int)player.posY, (int)player.posZ);
                    }
                    break;
                default:
            }
        }
    }
}
