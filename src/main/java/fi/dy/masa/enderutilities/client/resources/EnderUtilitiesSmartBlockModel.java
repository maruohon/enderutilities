package fi.dy.masa.enderutilities.client.resources;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.ItemBlockMachine;
import fi.dy.masa.enderutilities.block.machine.Machine;

@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public class EnderUtilitiesSmartBlockModel extends EnderUtilitiesFlexibleBakedModel implements ISmartBlockModel, ISmartItemModel
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

    @Override
    public IFlexibleBakedModel handleItemState(ItemStack stack)
    {
        if (stack != null)
        {
            Item item = stack.getItem();
            if (item instanceof ItemBlockMachine && ((ItemBlockMachine)stack.getItem()).getBlock() instanceof BlockEnderUtilities)
            {
                ItemBlockMachine ib = (ItemBlockMachine)stack.getItem();
                Machine machine = Machine.getMachine(((BlockEnderUtilities)ib.getBlock()).getBlockIndex(), stack.getItemDamage());
                if (machine != null)
                {
                    return machine.getModel(null);
                }
            }
        }

        return this;
    }
}
