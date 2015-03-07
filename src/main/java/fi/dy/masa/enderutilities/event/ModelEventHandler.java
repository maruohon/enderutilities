package fi.dy.masa.enderutilities.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesTextureRegistry;

public class ModelEventHandler
{
    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        EnderUtilitiesTextureRegistry.registerItemTextures(event.map);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void bakeModels(ModelBakeEvent event)
    {
        ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        EnderUtilitiesModelRegistry.registerBlockModels(event.modelRegistry, itemModelMesher);

        EnderUtilitiesModelRegistry.registerItemMeshDefinitions(itemModelMesher);
        EnderUtilitiesModelRegistry.registerSmartItemModel(event.modelRegistry, itemModelMesher);
    }
}
