package fi.dy.masa.enderutilities.client.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
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

    public static ModelBlock createNewModelBlock(ModelBlock base, String name, List elements, Map mapTextures, ResourceLocation parentLocation, Map<ResourceLocation, ModelBlock> models, boolean addToMap)
    {
        ItemCameraTransforms ct = new ItemCameraTransforms(base.getThirdPersonTransform(), base.getFirstPersonTransform(), base.getHeadTransform(), base.getInGuiTransform());
        EnderUtilitiesModelBlock newModelBlock = new EnderUtilitiesModelBlock(elements, mapTextures, base.isAmbientOcclusion(), base.isGui3d(), ct);
        newModelBlock.name = name;
        newModelBlock.setParentLocation(parentLocation);

        if (models != null)
        {
            newModelBlock.getParentFromMap(models);

            if (addToMap == true)
            {
                models.put(new ResourceLocation(name), newModelBlock);
            }
        }

        return newModelBlock;
    }

    public static ModelBlock createNewItemModelBlockForTexture(ModelBlock base, String name, String textureName, Map<ResourceLocation, ModelBlock> models, boolean addToMap)
    {
        Map<String, String> map = Maps.newHashMap();
        map.put("layer0", textureName);

        return createNewModelBlockForTextures(base, name, map, models, addToMap);
    }

    public static ModelBlock cloneModelBlock(ModelBlock base, String name, Map<ResourceLocation, ModelBlock> models, boolean addToMap)
    {
        return createNewModelBlock(base, name, base.getElements(), base.textures, new ResourceLocation(base.name), models, addToMap);
    }

    /**
     * Creates a new ModelBlock from the provided base ModelBlock. Note: this will replace the textureMap!
     */
    public static ModelBlock createNewModelBlockForTextures(ModelBlock base, String name, Map mapTextures, Map<ResourceLocation, ModelBlock> models, boolean addToMap)
    {
        return createNewModelBlock(base, name, base.getElements(), mapTextures, new ResourceLocation(base.name), models, addToMap);
    }

    public static ModelBlock createNewModelBlockWithElements(ModelBlock base, String name, List elements, Map<ResourceLocation, ModelBlock> models, boolean addToMap)
    {
        return createNewModelBlock(base, name, elements, base.textures, new ResourceLocation(base.name), models, addToMap);
    }

    public static ModelBlock readModel(ResourceLocation location, Map<ResourceLocation, ModelBlock> models)
    {
        String resourcePath = location.getResourcePath();

        if (resourcePath.startsWith("builtin/"))
        {
            throw new IllegalArgumentException("Can't load a builtin/ model");
        }

        Object object = null;
        ModelBlock modelBlock = null;

        try
        {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getModelLocation(location));
            object = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);
            modelBlock = ModelBlock.deserialize((Reader)object);
            modelBlock.name = location.toString();
            ((Reader)object).close();
        }
        catch (IOException e)
        {
            return null;
        }

        modelBlock.getParentFromMap(models);
        models.put(location, modelBlock);

        return modelBlock;
    }

    public static ResourceLocation getModelLocation(ResourceLocation location)
    {
        return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
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

    public static ModelBlock scaleModelHeight(ModelBlock model, float scale, Map<ResourceLocation, ModelBlock> models, boolean addToMap)
    {
        List<BlockPart> newElements = new ArrayList<BlockPart>();
        Iterator<BlockPart> iterBlockPart = model.getElements().iterator();
        boolean adjustTo = false;

        while (iterBlockPart.hasNext())
        {
            BlockPart blockPart = iterBlockPart.next();
            Vector3f vecFrom = new Vector3f(blockPart.positionFrom.x, blockPart.positionFrom.y, blockPart.positionFrom.z);
            Vector3f vecTo = new Vector3f(blockPart.positionTo.x, blockPart.positionTo.y, blockPart.positionTo.z);

            if (vecFrom.y < vecTo.y)
            {
                vecTo.y = vecFrom.y + (vecTo.y - vecFrom.y) * scale;
                adjustTo = true;
            }
            else
            {
                vecFrom.y = vecTo.y + (vecFrom.y - vecTo.y) * scale;
            }

            Iterator<EnumFacing> iterFacing = blockPart.mapFaces.keySet().iterator();
            Map<EnumFacing, BlockPartFace> mapFacesNew = Maps.newHashMap();

            while (iterFacing.hasNext())
            {
                EnumFacing enumFacing = iterFacing.next();
                BlockPartFace blockPartFace = (BlockPartFace)blockPart.mapFaces.get(enumFacing);

                if (blockPartFace != null && blockPartFace.blockFaceUV != null)
                {
                    BlockFaceUV blockFaceUV = new BlockFaceUV(blockPartFace.blockFaceUV.uvs.clone(), blockPartFace.blockFaceUV.rotation);

                    if (blockFaceUV != null && blockFaceUV.uvs != null)
                    {
                        if (adjustTo == false)
                        {
                            blockFaceUV.uvs[3] = blockFaceUV.uvs[1] + (blockFaceUV.uvs[3] - blockFaceUV.uvs[1]) * scale;
                        }
                        else
                        {
                            blockFaceUV.uvs[1] = blockFaceUV.uvs[3] + (blockFaceUV.uvs[1] - blockFaceUV.uvs[3]) * scale;
                        }
                    }

                    mapFacesNew.put(enumFacing, new BlockPartFace(enumFacing, blockPartFace.tintIndex, blockPartFace.texture, blockFaceUV));
                }
            }

            newElements.add(new BlockPart(vecFrom, vecTo, mapFacesNew, blockPart.partRotation, blockPart.shade));
        }

        return createNewModelBlockWithElements(model, model.name, newElements, models, addToMap);
    }

    /**
     * Print some info about the ModelBlock modelIn, for debugging purposes
     * @param modelIn
     */
    public static void printModelBlock(ModelBlock modelIn)
    {
        if (modelIn == null)
        {
            EnderUtilities.logger.info("printModelBlock(): null");
            return;
        }

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
            EnderUtilities.logger.info("         " + key + ": " + val);
        }
    }
}
