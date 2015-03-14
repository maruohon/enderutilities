package fi.dy.masa.enderutilities.item.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelBlock;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelFactory;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class ItemEnderUtilities extends Item
{
    public String name;

    /** Non-namespaced/non-mod-domain-prepended variant names for this item. */
    @SideOnly(Side.CLIENT)
    public String variants[];
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite textures[];
    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel models[];

    public ItemEnderUtilities()
    {
        super();
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
    }

    @Override
    public Item setUnlocalizedName(String name)
    {
        this.name = name;
        return super.setUnlocalizedName(ReferenceNames.getPrefixedName(name));
    }

    /**
     * Custom addInformation() method, which allows selecting a subset of the tooltip strings.
     */
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips)
    {
        ArrayList<String> tmpList = new ArrayList<String>();
        boolean verbose = EnderUtilities.proxy.isShiftKeyDown();

        // "Fresh" items without NBT data: display the tips before the usual tooltip data
        if (stack != null && stack.getTagCompound() == null)
        {
            this.addTooltips(stack, tmpList, verbose);

            if (verbose == false && tmpList.size() > 1)
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.holdshiftfordescription"));
            }
            else
            {
                list.addAll(tmpList);
            }
        }

        tmpList.clear();
        this.addInformationSelective(stack, player, tmpList, advancedTooltips, true);

        // If we want the compact version of the tooltip, and the compact list has more than 2 lines, only show the first line
        // plus the "Hold Shift for more" tooltip.
        if (verbose == false && tmpList.size() > 2)
        {
            tmpList.clear();
            this.addInformationSelective(stack, player, tmpList, advancedTooltips, false);
            list.add(tmpList.get(0));
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.holdshift"));
        }
        else
        {
            list.addAll(tmpList);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void addTooltips(String key, List<String> list, boolean verbose)
    {
        String translated = StatCollector.translateToLocal(key);
        // Translation found
        if (translated.equals(key) == false)
        {
            // We currently use '|lf' as a delimiter to split the string into multiple lines
            if (translated.contains("|lf"))
            {
                String[] lines = translated.split(Pattern.quote("|lf"));
                for (String line : lines)
                {
                    list.add(line);
                }
            }
            else
            {
                list.add(translated);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(this.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    @SideOnly(Side.CLIENT)
    public void addVariants(String... variantsIn)
    {
        // FIXME we should register all _models_ not individual texture names
        // That would also mean fixing the models so that a single model has all the necessary layers for each item
        // and the face quads should be baked based on the item NBT.
        int len = variantsIn.length;
        this.variants = new String[len];

        String[] namespaced = new String[len];
        for (int i = 0; i < len; ++i)
        {
            this.variants[i] = variantsIn[i];
            namespaced[i] = Reference.MOD_ID + ":" + variantsIn[i];
        }

        ModelBakery.addVariantName(this, namespaced);
    }

    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        int len = this.variants.length;
        //this.textures = new TextureAtlasSprite[len];

        for (int i = 0; i < len; ++i)
        {
            String name = ReferenceTextures.getItemTextureName(this.variants[i]);
            //textureMap.setTextureEntry(name, new EnderUtilitiesTexture(name));
            //this.textures[i] = textureMap.getTextureExtry(name);
            textureMap.registerSprite(new ResourceLocation(name));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher, TextureMap textureMap, Map<ResourceLocation, ModelBlock> modelMap)
    {
        itemModelMesher.register(this, EnderUtilitiesModelRegistry.baseItemMeshDefinition);
        ItemModelGenerator itemModelGenerator = new ItemModelGenerator();

        int len = this.variants.length;
        this.models = new IFlexibleBakedModel[len];

        for (int i = 0; i < len; ++i)
        {
            ModelBlock base;

            // Get the name of the model with the correct translation/rotation/scale etc.
            String name = this.getBaseModelName(this.variants[i]);
            if (name != null)
            {
                name = Reference.MOD_ID + ":item/" + name;
                base = EnderUtilitiesModelBlock.readModel(new ResourceLocation(name), modelMap);
                if (base == null)
                {
                    EnderUtilities.logger.fatal("Failed to read ModelBlock for " + name);
                    base = EnderUtilitiesModelRegistry.modelBlockBaseItems;
                }
            }
            // If the name is null, then the item in question doesn't have a custom model and we want to use the base model
            else
            {
                base = EnderUtilitiesModelRegistry.modelBlockBaseItems;
            }

            //EnderUtilitiesModelBlock.printModelBlock(base); // FIXME debug

            String modelName = Reference.MOD_ID + ":item/" + this.variants[i];
            String textureName = ReferenceTextures.getItemTextureName(this.variants[i]);
            ModelBlock modelBlock = EnderUtilitiesModelBlock.createNewItemModelBlockForTexture(base, modelName, textureName, modelMap);
            modelBlock = itemModelGenerator.makeItemModel(textureMap, modelBlock);

            if (modelBlock != null)
            {
                this.models[i] = EnderUtilitiesModelFactory.instance.bakeModel(modelBlock, ModelRotation.X0_Y0, false); // FIXME: rotation and uv-lock ??
                modelRegistry.putObject(new ModelResourceLocation(Reference.MOD_ID + ":" + this.variants[i], "inventory"), this.models[i]);
            }
            else
            {
                EnderUtilities.logger.fatal("ModelBlock from makeItemModel() was null when trying to bake item model for " + this.variants[i]);
            }
        }
    }

    /**
     * Get the name of the item model to use as the base model.
     * @return the name of the model to use (without any paths or modid), or null to use the default model
     */
    @SideOnly(Side.CLIENT)
    public String getBaseModelName(String variant)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void registerVariants()
    {
        this.addVariants(this.name);
    }

    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel getItemModel(ItemStack stack)
    {
        int index = stack.getItemDamage();

        return this.models[index < this.models.length ? index : 0];
    }
}
