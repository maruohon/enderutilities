package fi.dy.masa.enderutilities.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.network.message.MessageOpenGui;
import fi.dy.masa.enderutilities.network.message.MessageSendString;
import fi.dy.masa.enderutilities.network.message.MessageSyncCustomSlot;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;
import fi.dy.masa.enderutilities.reference.Reference;

public class PacketHandler
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID.toLowerCase());

    public static void init()
    {
        INSTANCE.registerMessage(MessageKeyPressed.Handler.class, MessageKeyPressed.class, 0, Side.SERVER);
        INSTANCE.registerMessage(MessageAddEffects.Handler.class, MessageAddEffects.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(MessageGuiAction.Handler.class, MessageGuiAction.class, 2, Side.SERVER);
        INSTANCE.registerMessage(MessageOpenGui.Handler.class, MessageOpenGui.class, 3, Side.SERVER);
        INSTANCE.registerMessage(MessageSyncSlot.Handler.class, MessageSyncSlot.class, 4, Side.CLIENT);
        INSTANCE.registerMessage(MessageSyncCustomSlot.Handler.class, MessageSyncCustomSlot.class, 5, Side.CLIENT);
        INSTANCE.registerMessage(MessageSendString.Handler.class, MessageSendString.class, 6, Side.SERVER);
    }
}
