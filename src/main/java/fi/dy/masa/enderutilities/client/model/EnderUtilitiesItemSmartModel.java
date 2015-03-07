package fi.dy.masa.enderutilities.client.model;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartItemModel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@SuppressWarnings("deprecation")
public class EnderUtilitiesItemSmartModel implements ISmartItemModel
{
    private TextureAtlasSprite texture;
    private List<LinkedList<BakedQuad>> faceQuads;
    private List<BakedQuad> generalQuads;
    private ItemCameraTransforms cameraTransforms;
    private boolean isAmbientOcclusion;
    private boolean isGui3d;
    private boolean isBuiltInRenderer;

    @SuppressWarnings("unchecked")
    public EnderUtilitiesItemSmartModel(IBakedModel baseModel)
    {
        this.generalQuads = baseModel.getGeneralQuads();
        this.faceQuads = newBlankFacingLists();

        for (EnumFacing facing : EnumFacing.values())
        {
            for (Object o : baseModel.getFaceQuads(facing))
            {
                this.faceQuads.get(facing.ordinal()).add((BakedQuad) o);
            }
        }

        this.cameraTransforms = baseModel.getItemCameraTransforms();
        this.isAmbientOcclusion = baseModel.isAmbientOcclusion();
        this.isGui3d = baseModel.isGui3d();
        this.isBuiltInRenderer = baseModel.isBuiltInRenderer();
        this.texture = baseModel.getTexture();
    }

    @Override
    public List getFaceQuads(EnumFacing facing)
    {
        return this.faceQuads.get(facing.ordinal());
    }

    @Override
    public List getGeneralQuads()
    {
        return this.generalQuads;
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
    public IBakedModel handleItemState(ItemStack stack)
    {
        // TODO Auto-generated method stub
        return this;
    }

    /**
     * Taken from DenseOres, by RWTema, in accordance to http://creativecommons.org/licenses/by/4.0/deed.en_GB
     */
    @SuppressWarnings("rawtypes")
    public static List newBlankFacingLists()
    {
        Object[] list = new Object[EnumFacing.values().length];

        for (int i = 0; i < EnumFacing.values().length; ++i)
        {
            list[i] = Lists.newLinkedList();
        }

        return ImmutableList.copyOf(list);
    }
}
