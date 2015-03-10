package fi.dy.masa.enderutilities.client.resources;

import java.io.IOException;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Maps;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

@SideOnly(Side.CLIENT)
public class EnderUtilitiesModelRegistry
{
    public static Map<ResourceLocation, ModelBlock> models = Maps.newHashMap();
    public static IFlexibleBakedModel baseItemModel;
    public static ModelBlock modelBlockBase;
    public static ItemMeshDefinition baseItemMeshDefinition;

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
        String name = Reference.MOD_ID + ":" + ReferenceNames.NAME_ITEM_MODEL_BASE;

        ModelResourceLocation mrl = new ModelResourceLocation(name, "inventory");
        baseItemModel = new EnderUtilitiesSmartItemModelBase(itemModelMesher.getModelManager().getModel(mrl));
        modelRegistry.putObject(mrl, baseItemModel);
    }

    public static void registerItemModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher)
    {
        TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();

        EnderUtilitiesItems.enderArrow.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderBag.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderBow.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderBucket.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderLasso.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderPearlReusable.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderPorter.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderCapacitor.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderPart.registerModels(modelRegistry, itemModelMesher, textures, models);
        ((ItemEnderSword)EnderUtilitiesItems.enderSword).registerModels(modelRegistry, itemModelMesher, textures, models);
        ((ItemEnderTool)EnderUtilitiesItems.enderTool).registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.linkCrystal.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.mobHarness.registerModels(modelRegistry, itemModelMesher, textures, models);
    }

    public static boolean setupBaseModels()
    {
        baseItemMeshDefinition = new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                // Base model for the ISmartItemModel
                return new ModelResourceLocation(Reference.MOD_ID + ":" + ReferenceNames.NAME_ITEM_MODEL_BASE, "inventory");
            }
        };

        ModelBlock modelBlock = ModelBlock.deserialize("{\"elements\":[{" + 
                                                            "\"from\": [0, 0, 0], \"to\": [16, 16, 16]," +
                                                            "\"faces\": { \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
        modelBlock.name = "minecraft:builtin/generated";
        models.put(new ResourceLocation(modelBlock.name), modelBlock);

        // FIXME debugging:
        //EnderUtilities.logger.info("Generated base ModelBlock: " + modelBlock.name);
        //EnderUtilitiesModelBlock.printModelBlock(modelBlock);

        String name = Reference.MOD_ID + ":" + "models/item/itemmodelbase";

        try
        {
            modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation(name), models);
        }
        catch (IOException e)
        {
            EnderUtilities.logger.fatal("Caught an IOException while trying to read ModelBlock for " + name);
            return false;
        }

        if (modelBlock == null)
        {
            EnderUtilities.logger.fatal("Failed to load the base ModelBlock for " + name);
            return false;
        }

        modelBlockBase = modelBlock;

        return true;
    }

    /*public static IFlexibleBakedModel createNewBasicItemModel(TextureAtlasSprite newTexture)
    {
        //System.out.println("pre: " + newTexture.toString());

        IFlexibleBakedModel newModel = new EnderUtilitiesSmartItemModelBase(new LinkedList<BakedQuad>(), EnderUtilitiesSmartItemModelBase.newBlankFacingLists(), baseItemModel.isGui3d(), baseItemModel.isAmbientOcclusion(), baseItemModel.isBuiltInRenderer(), newTexture, baseItemModel.getItemCameraTransforms());

        for (Object o : baseItemModel.getGeneralQuads())
        {
            BakedQuad quad = (BakedQuad) o;
            if (quad.getFace().equals(EnumFacing.UP))
            {
                newModel.getGeneralQuads().add(EnderUtilitiesTextureRegistry.changeItemTextureForQuad(quad, newTexture));
            }
        }

        for (EnumFacing facing : EnumFacing.values())
        {
            if (facing.equals(EnumFacing.UP) == false)
            {
                continue;
            }

            for (Object o : baseItemModel.getFaceQuads(facing))
            {
                BakedQuad quad = (BakedQuad) o;
                if (quad.getFace().equals(EnumFacing.UP))
                {
                    newModel.getFaceQuads(facing).add(EnderUtilitiesTextureRegistry.changeItemTextureForQuad(quad, newTexture));
                }
            }
        }

        //newModel.getGeneralQuads().addAll(baseModel.getGeneralQuads());

        //for (EnumFacing facing : EnumFacing.values())
        //{
        //    newModel.getFaceQuads(facing).addAll(baseModel.getFaceQuads(facing));
        //}

        return newModel;
    }*/
}
