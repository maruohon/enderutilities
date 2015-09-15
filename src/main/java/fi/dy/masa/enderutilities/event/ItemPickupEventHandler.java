package fi.dy.masa.enderutilities.event;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.ItemHandyBag;

public class ItemPickupEventHandler
{
    @SubscribeEvent
    public void onItemPickupEvent(EntityItemPickupEvent event)
    {
        if (ItemHandyBag.onItemPickupEvent(event) == true)
        {
            return;
        }
    }

}
