package fi.dy.masa.enderutilities.event;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemPickupManager;

public class ItemPickupEventHandler
{
    @SubscribeEvent
    public void onEntityItemPickupEvent(EntityItemPickupEvent event)
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
