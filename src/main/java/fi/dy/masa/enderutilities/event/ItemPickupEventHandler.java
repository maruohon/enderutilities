package fi.dy.masa.enderutilities.event;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemNullifier;
import fi.dy.masa.enderutilities.item.ItemPickupManager;

public class ItemPickupEventHandler
{
    @SubscribeEvent
    public void onEntityItemPickupEvent(EntityItemPickupEvent event)
    {
        if (ItemPickupManager.onEntityItemPickupEvent(event))
        {
            return;
        }

        if (ItemHandyBag.onEntityItemPickupEvent(event))
        {
            return;
        }

        if (ItemNullifier.onEntityItemPickupEvent(event))
        {
            return;
        }
    }

    @SubscribeEvent
    public void onPlayerItemPickupEvent(PlayerItemPickupEvent event)
    {
        if (ItemPickupManager.onItemPickupEvent(event))
        {
            return;
        }

        if (ItemHandyBag.onItemPickupEvent(event))
        {
            return;
        }

        if (ItemNullifier.onItemPickupEvent(event))
        {
            return;
        }
    }
}
