package fi.dy.masa.enderutilities.client.renderer.model;

import java.io.IOException;
import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelCustomData;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.TRSRTransformation;

import fi.dy.masa.enderutilities.reference.Reference;

public class ModelModularItem implements IModel, IModelCustomData
{
    public static IModel model = new ModelEnderTools();

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getTextures()
    {
        return ImmutableSet.of();
    }

    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        return null;
    }

    @Override
    public IModelState getDefaultState()
    {
        return TRSRTransformation.identity();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
        return this;
    }

    public enum LoaderModularItem implements ICustomModelLoader
    {
        instance;

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.getResourceDomain().equals(Reference.MOD_ID) && modelLocation.getResourcePath().contains("modular_model");
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws IOException
        {
            return model;
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            // no need to clear cache since we create a new model instance
        }
    }
}
