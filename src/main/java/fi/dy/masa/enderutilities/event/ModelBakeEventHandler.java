package fi.dy.masa.enderutilities.event;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.client.renderer.model.SmartItemModelWrapper;

public class ModelBakeEventHandler
{
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event)
    {
        //event.modelRegistry.putObject(SmartItemModelWrapper.RESOURCE, SmartItemModelWrapper.instance());
    }
}
