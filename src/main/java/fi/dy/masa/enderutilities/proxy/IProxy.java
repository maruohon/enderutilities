package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public interface IProxy
{
    public abstract EntityPlayer getPlayerFromMessageContext(MessageContext ctx);

    public abstract void registerEntities();

    public abstract void registerEventHandlers();

    public abstract void registerFuelHandlers();

    public abstract void registerKeyBindings();

    public abstract void registerRenderers();

    public abstract void registerTileEntities();

    public abstract boolean isShiftKeyDown();

    public abstract boolean isControlKeyDown();
}
