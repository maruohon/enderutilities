package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageKeyPressed implements IMessage
{
    private int keyPressed;

    public MessageKeyPressed()
    {
    }

    public MessageKeyPressed(int key)
    {
        this.keyPressed = key;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.keyPressed = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.keyPressed);
    }

    public static class Handler implements IMessageHandler<MessageKeyPressed, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageKeyPressed message, MessageContext ctx)
        {
            if (ctx.side != Side.SERVER)
            {
                EnderUtilities.logger.error("Wrong side in MessageKeyPressed: " + ctx.side);
                return null;
            }

            final EntityPlayerMP sendingPlayer = ctx.getServerHandler().playerEntity;
            if (sendingPlayer == null)
            {
                EnderUtilities.logger.error("Sending player was null in MessageKeyPressed");
                return null;
            }

            final WorldServer playerWorldServer = sendingPlayer.getServerForPlayer();
            if (playerWorldServer == null)
            {
                EnderUtilities.logger.error("World was null in MessageKeyPressed");
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

        protected void processMessage(final MessageKeyPressed message, EntityPlayer player)
        {
            ItemStack stack = player.getCurrentEquippedItem();

            if (stack != null && stack.getItem() instanceof IKeyBound)
            {
                ((IKeyBound) stack.getItem()).doKeyBindingAction(player, stack, message.keyPressed);
            }
            else
            {
                stack = InventoryUtils.getFirstKeyBoundUnselectedItem(player);
                if (stack != null)
                {
                    ((IKeyBoundUnselected) stack.getItem()).doUnselectedKeyAction(player, stack, message.keyPressed);
                }
            }
        }
    }
}
