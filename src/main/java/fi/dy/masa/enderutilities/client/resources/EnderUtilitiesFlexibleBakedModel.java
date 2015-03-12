package fi.dy.masa.enderutilities.client.resources;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public class EnderUtilitiesFlexibleBakedModel implements IFlexibleBakedModel
{
    private List<BakedQuad> generalQuads;
    private List<List<BakedQuad>> faceQuads;
    private boolean isAmbientOcclusion;
    private boolean isGui3d;
    private boolean isBuiltInRenderer;
    private TextureAtlasSprite texture;
    private ItemCameraTransforms cameraTransforms;
    private VertexFormat format;

    public EnderUtilitiesFlexibleBakedModel(IBakedModel baseModel)
    {
        this(baseModel.getGeneralQuads(), null, baseModel.isGui3d(), baseModel.isAmbientOcclusion(), baseModel.isBuiltInRenderer(), baseModel.getTexture(), baseModel.getItemCameraTransforms());

        this.faceQuads = newBlankFacingLists();
        for (EnumFacing facing : EnumFacing.values())
        {
            this.faceQuads.get(facing.ordinal()).addAll(baseModel.getFaceQuads(facing));
        }
    }

    public EnderUtilitiesFlexibleBakedModel(List<BakedQuad> generalQuads, List<List<BakedQuad>> faceQuads, boolean isAmbientOcclusion, boolean isGui3d, TextureAtlasSprite texture, ItemCameraTransforms cameraTransforms)
    {
        this(generalQuads, faceQuads, isAmbientOcclusion, isGui3d, false, texture, cameraTransforms);
    }

    public EnderUtilitiesFlexibleBakedModel(List<BakedQuad> generalQuads, List<List<BakedQuad>> faceQuads, boolean isAmbientOcclusion, boolean isGui3d, boolean isBuiltInRenderer, TextureAtlasSprite texture, ItemCameraTransforms cameraTransforms)
    {
        this.generalQuads = generalQuads;
        this.faceQuads = faceQuads;
        this.isAmbientOcclusion = isAmbientOcclusion;
        this.isGui3d = isGui3d;
        this.isBuiltInRenderer = isBuiltInRenderer;
        this.texture = texture;
        this.cameraTransforms = cameraTransforms;
        this.format = Attributes.DEFAULT_BAKED_FORMAT;
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return this.generalQuads;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing facing)
    {
        return this.faceQuads.get(facing.ordinal());
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return this.isAmbientOcclusion;
    }

    @Override
    public boolean isGui3d()
    {
        return this.isGui3d;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return this.isBuiltInRenderer;
    }

    @Override
    public TextureAtlasSprite getTexture()
    {
        return this.texture;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return this.cameraTransforms;
    }


    @Override
    public VertexFormat getFormat()
    {
        return this.format;
    }

    @SideOnly(Side.CLIENT)
    public static class Builder
    {
        private List<BakedQuad> builderGeneralQuads;
        private List<List<BakedQuad>> builderFaceQuads;
        private boolean builderAmbientOcclusion;
        private boolean builderGui3d;
        private TextureAtlasSprite builderTexture;
        private ItemCameraTransforms builderCameraTransforms;

        public Builder(ModelBlock modelBlockIn)
        {
            this(modelBlockIn.isAmbientOcclusion(), modelBlockIn.isGui3d(), new ItemCameraTransforms(modelBlockIn.getThirdPersonTransform(), modelBlockIn.getFirstPersonTransform(), modelBlockIn.getHeadTransform(), modelBlockIn.getInGuiTransform()));
        }

        private Builder(boolean ambientOcclusion, boolean isGui3d, ItemCameraTransforms cameraTransforms)
        {
            this.builderAmbientOcclusion = ambientOcclusion;
            this.builderGui3d = isGui3d;
            this.builderCameraTransforms = cameraTransforms;

            this.builderGeneralQuads = Lists.newArrayList();
            this.builderFaceQuads = Lists.newArrayListWithCapacity(6);

            for (int i = 0; i < 6; ++i)
            {
                this.builderFaceQuads.add(new ArrayList<BakedQuad>());
            }
        }

        public EnderUtilitiesFlexibleBakedModel.Builder addGeneralQuad(BakedQuad quad)
        {
            this.builderGeneralQuads.add(quad);
            return this;
        }

        public EnderUtilitiesFlexibleBakedModel.Builder addFaceQuad(EnumFacing face, BakedQuad quad)
        {
            this.builderFaceQuads.get(face.ordinal()).add(quad);
            return this;
        }

        public EnderUtilitiesFlexibleBakedModel.Builder setTexture(TextureAtlasSprite texture)
        {
            this.builderTexture = texture;
            return this;
        }

        public IFlexibleBakedModel makeBakedModel()
        {
            if (this.builderTexture == null)
            {
                //throw new RuntimeException("Missing particle!");
                this.builderTexture = new EnderUtilitiesTexture("missingno");
            }

            return new EnderUtilitiesFlexibleBakedModel(this.builderGeneralQuads, this.builderFaceQuads, this.builderAmbientOcclusion, this.builderGui3d, this.builderTexture, this.builderCameraTransforms);
        }
    }

    /**
     * Taken from DenseOres, by RWTema, in accordance to http://creativecommons.org/licenses/by/4.0/deed.en_GB
     */
    public static List<List<BakedQuad>> newBlankFacingLists()
    {
        List<List<BakedQuad>> list = new ArrayList<List<BakedQuad>>(EnumFacing.values().length);

        for (int i = 0; i < EnumFacing.values().length; ++i)
        {
            list.add(i, new LinkedList<BakedQuad>());
        }

        return ImmutableList.copyOf(list);
    }
}
