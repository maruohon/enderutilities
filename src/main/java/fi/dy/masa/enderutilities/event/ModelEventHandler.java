package fi.dy.masa.enderutilities.event;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesModelRegistry;

public class ModelEventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void bakeModels(ModelBakeEvent event)
    {
        EnderUtilitiesModelRegistry.registerBlockModels(event.modelRegistry);

        EnderUtilitiesModelRegistry.registerItemMeshDefinitions();
        EnderUtilitiesModelRegistry.registerItemModels(event.modelRegistry);
    }
}
