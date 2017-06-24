package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface IProxy
{
    public EntityPlayer getClientPlayer();

    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx);

    public void playSound(int soundId, float pitch, float volume, boolean repeat, boolean stop, float x, float y, float z);

    public void registerColorHandlers();

    public ModFixs getDataFixer();

    public void registerEntities();

    public void registerEventHandlers();

    public void registerKeyBindings();

    public void registerRenderers();

    public boolean isShiftKeyDown();

    public boolean isControlKeyDown();

    public boolean isAltKeyDown();
}
