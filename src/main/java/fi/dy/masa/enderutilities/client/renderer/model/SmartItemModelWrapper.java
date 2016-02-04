package fi.dy.masa.enderutilities.client.renderer.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.ISmartItemModel;

import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

@SuppressWarnings("deprecation")
public class SmartItemModelWrapper implements ISmartItemModel
{
    public static final ModelResourceLocation RESOURCE = new ModelResourceLocation(Reference.MOD_ID + ":smartitemmodelwrapper", "inventory");
    private static SmartItemModelWrapper instance;
    private ModelManager modelManager;
    private List<BakedQuad> generalQuads;
    private List<BakedQuad> faceQuads;

    private SmartItemModelWrapper()
    {
        this.modelManager = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
        this.generalQuads = new ArrayList<BakedQuad>();
        this.faceQuads = new ArrayList<BakedQuad>();
    }

    public static SmartItemModelWrapper instance()
    {
        if (instance == null)
        {
            instance = new SmartItemModelWrapper();
        }

        return instance;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_)
    {
        return this.faceQuads;
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return this.generalQuads;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return this.modelManager.getMissingModel().getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack)
    {
        if (stack.getItem() == EnderUtilitiesItems.enderSword)
        {
            return this.getSwordModel(stack);
        }

        return this.getToolModel(stack);
    }

    private IBakedModel getToolModel(ItemStack stack)
    {
        return this.modelManager.getModel(new ModelResourceLocation("minecraft:blaze_rod", "inventory"));
    }

    private IBakedModel getSwordModel(ItemStack stack)
    {
        return this.modelManager.getModel(new ModelResourceLocation("minecraft:stick", "inventory"));
    }
}
