package fi.dy.masa.enderutilities.client.resources;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;

@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public class EnderUtilitiesSmartItemModel extends EnderUtilitiesFlexibleBakedModel implements ISmartItemModel
{
    public EnderUtilitiesSmartItemModel(IBakedModel baseModel)
    {
        super(baseModel);
    }

    public EnderUtilitiesSmartItemModel(List<BakedQuad> generalQuads, List<List<BakedQuad>> faceQuads, boolean isAmbientOcclusion, boolean isGui3d, TextureAtlasSprite texture, ItemCameraTransforms cameraTransforms)
    {
        super(generalQuads, faceQuads, isAmbientOcclusion, isGui3d, texture, cameraTransforms);
    }

    public EnderUtilitiesSmartItemModel(List<BakedQuad> generalQuads, List<List<BakedQuad>> faceQuads, boolean isAmbientOcclusion, boolean isGui3d, boolean isBuiltInRenderer, TextureAtlasSprite texture, ItemCameraTransforms cameraTransforms)
    {
        super(generalQuads, faceQuads, isAmbientOcclusion, isGui3d, isBuiltInRenderer, texture, cameraTransforms);
    }

    @Override
    public IFlexibleBakedModel handleItemState(ItemStack stack)
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
}
