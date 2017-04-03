package fi.dy.masa.enderutilities.event;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.PlacementProperties;

public class WorldEventHandler
{
    @SubscribeEvent
    public void onWorldSaveEvent(WorldEvent.Save event)
    {
        EnergyBridgeTracker.writeToDisk();
        PlacementProperties.getInstance().writeToDisk();
    }
}
