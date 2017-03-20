package fi.dy.masa.enderutilities.event;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemPickupManager;

public class ItemPickupEventHandler
{
    @SubscribeEvent
    public void onEntityItemPickupEvent(EntityItemPickupEvent event)
    {
        if (ItemPickupManager.onEntityItemPickupEvent(event) == false)
        {
            return;
        }

        if (ItemHandyBag.onEntityItemPickupEvent(event) == false)
        {
            return;
        }
    }

    @SubscribeEvent
    public void onPlayerItemPickupEvent(PlayerItemPickupEvent event)
    {
        if (ItemPickupManager.onItemPickupEvent(event) == false)
        {
            return;
        }

        if (ItemHandyBag.onItemPickupEvent(event) == false)
        {
            return;
        }
    }
}
