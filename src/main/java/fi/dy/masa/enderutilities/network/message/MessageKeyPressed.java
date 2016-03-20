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
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import io.netty.buffer.ByteBuf;

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
            // FIXME 1.9
            ItemStack stack = player.getHeldItemMainhand();
            if (stack == null || (stack.getItem() instanceof IKeyBound == false))
            {
                stack = player.getHeldItemOffhand();
            }

            if (stack != null && stack.getItem() instanceof IKeyBound)
            {
                if ((message.keyPressed & ReferenceKeys.KEYBIND_ID_TOGGLE_MODE) != 0)
                {
                    ((IKeyBound) stack.getItem()).doKeyBindingAction(player, stack, message.keyPressed);
                }
            }
            else if (ItemInventorySwapper.getSlotContainingEnabledItem(player) != -1)
            {
                ItemInventorySwapper.handleKeyPressUnselected(player, message.keyPressed);
            }
        }
    }
}
