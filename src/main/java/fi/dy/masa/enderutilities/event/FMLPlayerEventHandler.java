package fi.dy.masa.enderutilities.event;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class FMLPlayerEventHandler
{
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event)
    {
        if (event != null && event.player != null && event.player.riddenByEntity != null)
        {
            event.player.riddenByEntity.mountEntity(null);
        }
    }
}
