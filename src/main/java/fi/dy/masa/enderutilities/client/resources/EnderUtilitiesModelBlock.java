package fi.dy.masa.enderutilities.client.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import fi.dy.masa.enderutilities.EnderUtilities;

@SideOnly(Side.CLIENT)
@SuppressWarnings({ "deprecation", "rawtypes" })
public class EnderUtilitiesModelBlock extends ModelBlock
{
    public EnderUtilitiesModelBlock(List elements, Map textures, boolean ambientOcclusion, boolean isGui3d, ItemCameraTransforms cameraTransforms)
    {
        super(elements, textures, ambientOcclusion, isGui3d, cameraTransforms);
    }

    public EnderUtilitiesModelBlock(ResourceLocation parentLocation, Map textures, boolean ambientOcclusion, boolean isGui3d, ItemCameraTransforms cameraTransforms)
    {
        super(parentLocation, textures, ambientOcclusion, isGui3d, cameraTransforms);
    }

    public void setParentLocation(ResourceLocation parentLocation)
    {
        this.parentLocation = parentLocation;
    }

    public static ModelBlock createNewModelBlockForTexture(ModelBlock base, TextureAtlasSprite sprite, String name, Map<ResourceLocation, ModelBlock> models)
    {
        if (sprite == null)
        {
            EnderUtilities.logger.fatal("createNewModelBlockForTexture(): sprite == null");
            return base;
        }

        // FIXME debug
        //EnderUtilities.logger.info("createNewModelBlockForTexture(): from base modelBlock:");
        //printModelBlock(base);

        Map<String, String> map = Maps.newHashMap();
        map.put("layer0", sprite.getIconName()); // FIXME

        ItemCameraTransforms ct = new ItemCameraTransforms(base.getThirdPersonTransform(), base.getFirstPersonTransform(), base.getHeadTransform(), base.getInGuiTransform());
        EnderUtilitiesModelBlock newModelBlock = new EnderUtilitiesModelBlock(base.getElements(), map, base.isAmbientOcclusion(), base.isGui3d(), ct);
        newModelBlock.name = name;
        newModelBlock.setParentLocation(base.getParentLocation());
        newModelBlock.getParentFromMap(models);
        models.put(new ResourceLocation(name), newModelBlock);

        // FIXME debug
        //EnderUtilities.logger.info("createNewModelBlockForTexture(): newModelBlock:");
        //printModelBlock(newModelBlock);

        return newModelBlock;
    }

    public static ModelBlock readModel(ResourceLocation location, Map<ResourceLocation, ModelBlock> models) throws IOException
    {
        String resourcePath = location.getResourcePath();

        if (resourcePath.startsWith("builtin/"))
        {
            throw new IllegalArgumentException("Can't load a builtin/ model");
        }

        Object object = null;
        ModelBlock modelBlock = null;

        IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getModelLocation(location));
        object = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

        try
        {
            ModelBlock mb = ModelBlock.deserialize((Reader)object);
            mb.name = location.toString();
            modelBlock = mb;
        }
        finally
        {
            ((Reader)object).close();
        }

        modelBlock.getParentFromMap(models);
        models.put(location, modelBlock);

        return modelBlock;
    }

    public static void setModelParents()
    {
        Iterator iterator = EnderUtilitiesModelRegistry.models.values().iterator();

        while (iterator.hasNext())
        {
            ModelBlock modelblock = (ModelBlock)iterator.next();
            modelblock.getParentFromMap(EnderUtilitiesModelRegistry.models);
        }

        ModelBlock.checkModelHierarchy(EnderUtilitiesModelRegistry.models);
    }

    public static ResourceLocation getModelLocation(ResourceLocation location)
    {
        return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".json");
    }

    /**
     * Print some info about the ModelBlock modelIn, for debugging purposes
     * @param modelIn
     */
    public static void printModelBlock(ModelBlock modelIn)
    {
        EnderUtilities.logger.info("printModelBlock():");
        EnderUtilities.logger.info("    name: " + modelIn.name);
        EnderUtilities.logger.info("    parentLocation: " + modelIn.getParentLocation());
        EnderUtilities.logger.info("    hasParent(): " + (modelIn.getRootModel() != modelIn));
        EnderUtilities.logger.info(String.format("    elements: (%d)", modelIn.getElements() != null ? modelIn.getElements().size() : 0));
        Iterator iter1 = modelIn.getElements().iterator();
        while (iter1.hasNext())
        {
            EnderUtilities.logger.info("        element: " + iter1.next());
        }

        EnderUtilities.logger.info("    textures:");

        Iterator<Map.Entry> iter2 = modelIn.textures.entrySet().iterator();
        while (iter2.hasNext())
        {
            Map.Entry pair = iter2.next();
            String key = (String)pair.getKey();
            String val = (String)pair.getValue();
            EnderUtilities.logger.info("         key: " + key + " val: " + val);
        }
    }
}
