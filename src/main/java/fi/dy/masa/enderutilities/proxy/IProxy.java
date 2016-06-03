package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface IProxy
{
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx);

    public void registerColorHandlers();

    public void registerEntities();

    public void registerEventHandlers();

    public void registerKeyBindings();

    public void registerModels();

    public void registerRenderers();

    public void registerSounds();

    public void registerTileEntities();

    public void setupReflection();

    public boolean isShiftKeyDown();

    public boolean isControlKeyDown();

    public boolean isAltKeyDown();
}
