package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import fi.dy.masa.enderutilities.block.BlockDrawbridge;

public class ModelDrawbridgeBaked extends ModelCamouflageBlockBaked
{
    public ModelDrawbridgeBaked(ITextureMapped textureMapping, IModel baseModel, @Nullable IModel overlayModel,
            IBlockState defaultState, IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        super(textureMapping, baseModel, overlayModel, defaultState, state, format, bakedTextureGetter);
    }

    @Override
    protected IBakedModel getBakedBaseModel(IBlockState state)
    {
        IModel model = ((IRetexturableModel) this.baseModel).retexture(this.getTextures(state, this.textures));
        return model.bake(new TRSRTransformation(state.getValue(BlockDrawbridge.FACING)), this.format, this.bakedTextureGetter);
    }

    protected ImmutableMap<String, String> getTextures(IBlockState state, Map<String, String> texturesIn)
    {
        Map<String, String> texturesOut = new HashMap<String, String>();

        texturesOut.put("particle", texturesIn.get("front"));
        texturesOut.put("top",      texturesIn.get("top"));
        texturesOut.put("bottom",   texturesIn.get("bottom"));
        texturesOut.put("front",    texturesIn.get("front"));
        texturesOut.put("back",     texturesIn.get("back"));
        texturesOut.put("left",     texturesIn.get("left"));
        texturesOut.put("right",    texturesIn.get("right"));

        return ImmutableMap.copyOf(texturesOut);
    }
}
