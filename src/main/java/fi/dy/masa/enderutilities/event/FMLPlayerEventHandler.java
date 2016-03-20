package fi.dy.masa.enderutilities.event;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class FMLPlayerEventHandler
{
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event)
    {
        // FIXME 1.9: remove?
        if (event.player.isBeingRidden() == true)
        {
            event.player.getPassengers().get(0).dismountRidingEntity();
        }
    }
}
