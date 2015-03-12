package fi.dy.masa.enderutilities.client.resources;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.BlockEnderUtilities;

@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public class EnderUtilitiesSmartBlockModel extends EnderUtilitiesFlexibleBakedModel implements ISmartBlockModel
{
    public EnderUtilitiesSmartBlockModel(IBakedModel baseModel)
    {
        super(baseModel);
    }

    public EnderUtilitiesSmartBlockModel(List<BakedQuad> generalQuads, List<List<BakedQuad>> faceQuads, boolean isAmbientOcclusion, boolean isGui3d, TextureAtlasSprite texture, ItemCameraTransforms cameraTransforms)
    {
        super(generalQuads, faceQuads, isAmbientOcclusion, isGui3d, texture, cameraTransforms);
    }

    public EnderUtilitiesSmartBlockModel(List<BakedQuad> generalQuads, List<List<BakedQuad>> faceQuads, boolean isAmbientOcclusion, boolean isGui3d, boolean isBuiltInRenderer, TextureAtlasSprite texture, ItemCameraTransforms cameraTransforms)
    {
        super(generalQuads, faceQuads, isAmbientOcclusion, isGui3d, isBuiltInRenderer, texture, cameraTransforms);
    }

    @Override
    public IFlexibleBakedModel handleBlockState(IBlockState state)
    {
        if (state.getBlock() instanceof BlockEnderUtilities)
        {
            return ((BlockEnderUtilities)state.getBlock()).getModel(state);
        }

        return this;
    }
}
