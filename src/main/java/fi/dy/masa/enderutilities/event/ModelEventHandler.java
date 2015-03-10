package fi.dy.masa.enderutilities.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelFactory;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesTextureRegistry;

@SideOnly(Side.CLIENT)
public class ModelEventHandler
{
    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        EnderUtilitiesTextureRegistry.registerItemTextures(event.map);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        new EnderUtilitiesModelFactory(Minecraft.getMinecraft().getTextureMapBlocks());
        ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        if (EnderUtilitiesModelRegistry.setupBaseModels() == false)
        {
            EnderUtilities.logger.fatal("Setting up base models failed");
            return;
        }

        EnderUtilitiesModelRegistry.registerSmartItemModel(event.modelRegistry, itemModelMesher);
        EnderUtilitiesModelRegistry.registerItemModels(event.modelRegistry, itemModelMesher);

        //EnderUtilitiesModelRegistry.registerBlockModels(event.modelRegistry, itemModelMesher);
    }
}
