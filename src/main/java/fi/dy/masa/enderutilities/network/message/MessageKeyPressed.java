package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import io.netty.buffer.ByteBuf;

public class MessageKeyPressed implements IMessage, IMessageHandler<MessageKeyPressed, IMessage>
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

    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx)
    {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        ItemStack stack = player.getCurrentEquippedItem();

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

        return null;
    }
}
