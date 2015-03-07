package fi.dy.masa.enderutilities.client.resources;

import java.util.LinkedList;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
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
        // We use the Ender Lasso as a base model for all the items based on the ISmartItemModel.
        String name = ReferenceNames.NAME_ITEM_ENDER_LASSO;
        //registerModel(itemModelMesher, name, 0, "inventory");

        //ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
        //baseItemModel = new EnderUtilitiesItemSmartModelBase((IBakedModel)modelRegistry.getObject(mrl));
        baseItemModel = new EnderUtilitiesItemSmartModelBase(itemModelMesher.getItemModel(new ItemStack(Items.book)));
        modelRegistry.putObject(new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory"), baseItemModel);
    }

    public static void registerItemModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher)
    {
        ItemMeshDefinition imd = new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                // We use the Lasso as a base model for the ISmartItemModel, see below
                return new ModelResourceLocation(Reference.MOD_ID + ":" + ReferenceNames.NAME_ITEM_ENDER_LASSO, "inventory");
            }
        };

        itemModelMesher.register(EnderUtilitiesItems.enderArrow,            imd);
        itemModelMesher.register(EnderUtilitiesItems.enderBag,              imd);
        itemModelMesher.register(EnderUtilitiesItems.enderBow,              imd);
        itemModelMesher.register(EnderUtilitiesItems.enderBucket,           imd);
        itemModelMesher.register(EnderUtilitiesItems.enderLasso,            imd);
        itemModelMesher.register(EnderUtilitiesItems.enderPearlReusable,    imd);
        itemModelMesher.register(EnderUtilitiesItems.enderPorter,           imd);
        itemModelMesher.register(EnderUtilitiesItems.enderCapacitor,        imd);
        itemModelMesher.register(EnderUtilitiesItems.enderPart,             imd);
        itemModelMesher.register(EnderUtilitiesItems.enderSword,            imd);
        itemModelMesher.register(EnderUtilitiesItems.enderTool,             imd);
        itemModelMesher.register(EnderUtilitiesItems.linkCrystal,           imd);
        itemModelMesher.register(EnderUtilitiesItems.mobHarness,            imd);

        EnderUtilitiesItems.enderArrow.registerModels(modelRegistry);
        EnderUtilitiesItems.enderBag.registerModels(modelRegistry);
        EnderUtilitiesItems.enderBow.registerModels(modelRegistry);
        EnderUtilitiesItems.enderBucket.registerModels(modelRegistry);
        EnderUtilitiesItems.enderLasso.registerModels(modelRegistry);
        EnderUtilitiesItems.enderPearlReusable.registerModels(modelRegistry);
        EnderUtilitiesItems.enderPorter.registerModels(modelRegistry);
        EnderUtilitiesItems.enderCapacitor.registerModels(modelRegistry);
        EnderUtilitiesItems.enderPart.registerModels(modelRegistry);
        ((ItemEnderSword)EnderUtilitiesItems.enderSword).registerModels(modelRegistry);
        ((ItemEnderTool)EnderUtilitiesItems.enderTool).registerModels(modelRegistry);
        EnderUtilitiesItems.linkCrystal.registerModels(modelRegistry);
        EnderUtilitiesItems.mobHarness.registerModels(modelRegistry);
    }

    public static void registerModel(ItemModelMesher itemModelMesher, String name, int meta, String type)
    {
        Item item = GameRegistry.findItem(Reference.MOD_ID, name);
        if (item == null)
        {
            EnderUtilities.logger.fatal(String.format("Failed registering model for %s (meta: %d), type: %s", name, meta, type));
            return;
        }

        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, type);
        itemModelMesher.register(item, meta, mrl);
    }

    @SuppressWarnings("rawtypes")
    public static IBakedModel createModel(IBakedModel baseModel, TextureAtlasSprite newTexture)
    {
        System.out.println("pre: " + newTexture.toString());

        IBakedModel newModel = new SimpleBakedModel(new LinkedList(), EnderUtilitiesItemSmartModelBase.newBlankFacingLists(), baseModel.isGui3d(), baseModel.isAmbientOcclusion(), newTexture, baseModel.getItemCameraTransforms());

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
