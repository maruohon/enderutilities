package fi.dy.masa.enderutilities.client.resources;

import net.minecraft.client.renderer.texture.TextureMap;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;

public class EnderUtilitiesTextureRegistry
{
    public static void registerItemTextures(TextureMap textureMap)
    {
        EnderUtilitiesItems.enderArrow.registerTextures(textureMap);
        EnderUtilitiesItems.enderBag.registerTextures(textureMap);
        EnderUtilitiesItems.enderBow.registerTextures(textureMap);
        EnderUtilitiesItems.enderBucket.registerTextures(textureMap);
        EnderUtilitiesItems.enderLasso.registerTextures(textureMap);
        EnderUtilitiesItems.enderPearlReusable.registerTextures(textureMap);
        EnderUtilitiesItems.enderPorter.registerTextures(textureMap);
        EnderUtilitiesItems.enderCapacitor.registerTextures(textureMap);
        EnderUtilitiesItems.enderPart.registerTextures(textureMap);
        ((ItemEnderSword)EnderUtilitiesItems.enderSword).registerTextures(textureMap);
        ((ItemEnderTool)EnderUtilitiesItems.enderTool).registerTextures(textureMap);
        EnderUtilitiesItems.linkCrystal.registerTextures(textureMap);
        EnderUtilitiesItems.mobHarness.registerTextures(textureMap);
    }
}
