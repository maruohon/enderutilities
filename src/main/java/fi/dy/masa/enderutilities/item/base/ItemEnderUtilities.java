package fi.dy.masa.enderutilities.item.base;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.client.resources.TextureItems;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SuppressWarnings("deprecation")
public class ItemEnderUtilities extends Item
{
    public String name;

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite textures[];
    @SideOnly(Side.CLIENT)
    public String texture_names[];
    @SideOnly(Side.CLIENT)
    public IBakedModel models[];

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
    public static void addVariantNames(Item item, String[] names)
    {
        // FIXME we should register all _models_ not individual texture names
        // That would also mean fixing the models so that a single model has all the necessary layers for each item
        // and the face quads should be baked based on the item NBT.
        String[] nameSpaced = new String[names.length];
        for (int i = 0; i < names.length; ++i)
        {
            nameSpaced[i] = Reference.MOD_ID + ":" + names[i];
        }

        ModelBakery.addVariantName(item, nameSpaced);
    }

    @SideOnly(Side.CLIENT)
    public void registerModel(int index, IRegistry modelRegistry)
    {
        if (this.textures.length <= index || this.textures[index] == null)
        {
            EnderUtilities.logger.fatal("Good afternoon, this is Major Derp. I live in ItemEnderUtilities.registerModel()");
            return;
        }

        this.models[index] = EnderUtilitiesModelRegistry.createModel(EnderUtilitiesModelRegistry.baseItemModel, this.textures[index]);
        modelRegistry.putObject(Reference.MOD_ID + ":" + this.texture_names[index], this.models[index]);
        //modelRegistry.putObject(ReferenceNames.getPrefixedName(this.texture_names[index]), this.models[index]);
        //modelRegistry.putObject(new ModelResourceLocation(Reference.MOD_ID + ":" + this.texture_names[index], "inventory"), this.models[index]);
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher, ItemMeshDefinition imd)
    {
        itemModelMesher.register(this, imd);

        int len = this.textures.length;
        this.models = new IBakedModel[len];

        for (int i = 0; i < len; ++i)
        {
            this.registerModel(i, modelRegistry);
        }

        //addVariantNames(this, this.texture_names);
    }

    @SideOnly(Side.CLIENT)
    public void registerTexture(int index, String spriteName, TextureMap textureMap)
    {
        if (index >= this.textures.length)
        {
            EnderUtilities.logger.fatal("Index out of bounds in ItemEnderUtilities.registerTexture(): " + index);
            return;
        }

        textureMap.setTextureEntry(ReferenceTextures.getItemTextureName(spriteName), new TextureItems(ReferenceTextures.getItemTextureName(spriteName)));

        this.textures[index] = textureMap.getTextureExtry(ReferenceTextures.getItemTextureName(spriteName));
        this.texture_names[index] = spriteName;
    }

    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        this.textures = new TextureAtlasSprite[1];
        this.texture_names = new String[this.textures.length];

        this.registerTexture(0, this.name, textureMap);
    }

    @SideOnly(Side.CLIENT)
    public IBakedModel getItemModel(ItemStack stack)
    {
        int index = stack.getItemDamage();

        return this.models[index < this.textures.length ? index : 0];
    }
}
