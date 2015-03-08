package fi.dy.masa.enderutilities.client.resources;

import java.util.LinkedList;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class EnderUtilitiesModelRegistry
{
    public static IBakedModel baseItemModel;

    public static void registerBlockModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher)
    {
        //Item item = Item.getItemFromBlock(EnderUtilitiesBlocks.machine_0);
        //ModelBakery.addVariantName(item, "enderfurnace.off", "toolworkstation", "enderinfuser");

        //registerModel(itemModelMesher, ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE,     0, "inventory"); // Ender Furnace
        //registerModel(itemModelMesher, ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION,  1, "inventory"); // Tool Workstation
        //registerModel(itemModelMesher, ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER,     2, "inventory"); // Ender Infuser
    }

    public static void registerSmartItemModel(IRegistry modelRegistry, ItemModelMesher itemModelMesher)
    {
        // Base model for the ISmartItemModel model, which then gets customized for each item as needed.
        //String name = ReferenceNames.NAME_ITEM_MODEL_BASE;
        String name = ReferenceNames.NAME_ITEM_ENDER_LASSO;

        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
        baseItemModel = new EnderUtilitiesSmartItemModelBase(itemModelMesher.getModelManager().getModel(mrl));
        modelRegistry.putObject(mrl, baseItemModel);
    }

    public static void registerItemModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher)
    {
        ItemMeshDefinition imd = new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                // Base model for the ISmartItemModel
                //return new ModelResourceLocation(Reference.MOD_ID + ":" + ReferenceNames.NAME_ITEM_MODEL_BASE, "inventory");
                return new ModelResourceLocation(Reference.MOD_ID + ":" + ReferenceNames.NAME_ITEM_ENDER_LASSO, "inventory");
            }
        };

        EnderUtilitiesItems.enderArrow.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderBag.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderBow.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderBucket.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderLasso.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderPearlReusable.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderPorter.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderCapacitor.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.enderPart.registerModels(modelRegistry, itemModelMesher, imd);
        ((ItemEnderSword)EnderUtilitiesItems.enderSword).registerModels(modelRegistry, itemModelMesher, imd);
        ((ItemEnderTool)EnderUtilitiesItems.enderTool).registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.linkCrystal.registerModels(modelRegistry, itemModelMesher, imd);
        EnderUtilitiesItems.mobHarness.registerModels(modelRegistry, itemModelMesher, imd);
    }

    /*public static void registerModel(ItemModelMesher itemModelMesher, String name, int meta, String type)
    {
        Item item = GameRegistry.findItem(Reference.MOD_ID, name);
        if (item == null)
        {
            EnderUtilities.logger.fatal(String.format("Failed registering model for %s (meta: %d), type: %s", name, meta, type));
            return;
        }

        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, type);
        itemModelMesher.register(item, meta, mrl);
    }*/

    @SuppressWarnings("rawtypes")
    public static IBakedModel createNewModel(IBakedModel baseModel, TextureAtlasSprite newTexture)
    {
        //System.out.println("pre: " + newTexture.toString());

        IBakedModel newModel = new EnderUtilitiesSmartItemModelBase(new LinkedList(), EnderUtilitiesSmartItemModelBase.newBlankFacingLists(), baseModel.isGui3d(), baseModel.isAmbientOcclusion(), baseModel.isBuiltInRenderer(), newTexture, baseModel.getItemCameraTransforms());

        for (Object o : baseModel.getGeneralQuads())
        {
            newModel.getGeneralQuads().add(EnderUtilitiesTextureRegistry.changeTexture((BakedQuad) o, newTexture));
        }

        for (EnumFacing facing : EnumFacing.values())
        {
            for (Object o : baseModel.getFaceQuads(facing))
            {
                newModel.getFaceQuads(facing).add(EnderUtilitiesTextureRegistry.changeTexture((BakedQuad) o, newTexture));
            }
        }

        /*newModel.getGeneralQuads().addAll(baseModel.getGeneralQuads());

        for (EnumFacing facing : EnumFacing.values())
        {
            newModel.getFaceQuads(facing).addAll(baseModel.getFaceQuads(facing));
        }*/

        return newModel;
    }
}
