package fi.dy.masa.enderutilities.client.resources;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartItemModel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class EnderUtilitiesSmartItemModelBase implements ISmartItemModel
{
    private TextureAtlasSprite texture;
    private List<LinkedList<BakedQuad>> faceQuads;
    private List<BakedQuad> generalQuads;
    private ItemCameraTransforms cameraTransforms;
    private boolean isAmbientOcclusion;
    private boolean isGui3d;
    private boolean isBuiltInRenderer;

    public EnderUtilitiesSmartItemModelBase(IBakedModel baseModel)
    {
        this(baseModel.getGeneralQuads(), null, baseModel.isGui3d(), baseModel.isAmbientOcclusion(), baseModel.isBuiltInRenderer(), baseModel.getTexture(), baseModel.getItemCameraTransforms());

        this.faceQuads = newBlankFacingLists();
        for (EnumFacing facing : EnumFacing.values())
        {
            this.faceQuads.get(facing.ordinal()).addAll(baseModel.getFaceQuads(facing));
        }
    }

    public EnderUtilitiesSmartItemModelBase(List generalQuads, List faceQuads, boolean isAmbientOcclusion, boolean isGui3d, boolean isBuiltInRenderer, TextureAtlasSprite texture, ItemCameraTransforms cameraTransforms)
    {
        this.generalQuads = generalQuads;
        this.faceQuads = faceQuads;
        this.isAmbientOcclusion = isAmbientOcclusion;
        this.isGui3d = isGui3d;
        this.isBuiltInRenderer = isBuiltInRenderer;
        this.texture = texture;
        this.cameraTransforms = cameraTransforms;
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
        if (stack != null)
        {
            Item item = stack.getItem();
            if (item instanceof ItemEnderUtilities)
            {
                return ((ItemEnderUtilities)stack.getItem()).getItemModel(stack);
            }
            else if (item instanceof ItemEnderTool)
            {
                return ((ItemEnderTool)stack.getItem()).getItemModel(stack);
            }
            else if (item instanceof ItemEnderSword)
            {
                return ((ItemEnderSword)stack.getItem()).getItemModel(stack);
            }
        }

        return this;
    }

    /**
     * Taken from DenseOres, by RWTema, in accordance to http://creativecommons.org/licenses/by/4.0/deed.en_GB
     */
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
