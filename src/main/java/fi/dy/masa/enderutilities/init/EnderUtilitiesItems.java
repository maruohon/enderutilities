package fi.dy.masa.enderutilities.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.item.ItemEnderArrow;
import fi.dy.masa.enderutilities.item.ItemEnderBag;
import fi.dy.masa.enderutilities.item.ItemEnderBow;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemEnderPearlReusable;
import fi.dy.masa.enderutilities.item.ItemEnderPorter;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;

public class EnderUtilitiesItems
{
    public static final ItemEnderUtilities enderCapacitor = new ItemEnderCapacitor();
    public static final ItemEnderUtilities enderPart = new ItemEnderPart();
    public static final ItemEnderUtilities linkCrystal = new ItemLinkCrystal();

    public static final ItemEnderUtilities enderArrow = new ItemEnderArrow();
    public static final ItemEnderUtilities enderBag = new ItemEnderBag();
    public static final ItemEnderUtilities enderBow = new ItemEnderBow();
    public static final ItemEnderUtilities enderBucket = new ItemEnderBucket();
    public static final ItemEnderUtilities enderLasso = new ItemEnderLasso();
    public static final ItemEnderUtilities enderPearlReusable = new ItemEnderPearlReusable();
    public static final ItemEnderUtilities enderPorter = new ItemEnderPorter();
    public static final Item enderSword = new ItemEnderSword();
    public static final Item enderTool = new ItemEnderTool();
    public static final ItemEnderUtilities mobHarness = new ItemMobHarness();

    public static void init()
    {
        if (Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderPart, ReferenceNames.NAME_ITEM_ENDERPART);
        }
        if (Configs.disableItemEnderCapacitor.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderCapacitor, ReferenceNames.NAME_ITEM_ENDERPART_ENDERCAPACITOR);
        }
        if (Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            GameRegistry.registerItem(linkCrystal, ReferenceNames.NAME_ITEM_ENDERPART_LINKCRYSTAL);
        }
        if (Configs.disableItemEnderArrow.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderArrow, ReferenceNames.NAME_ITEM_ENDER_ARROW);
        }
        if (Configs.disableItemEnderBag.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderBag, ReferenceNames.NAME_ITEM_ENDER_BAG);
        }
        if (Configs.disableItemEnderBow.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderBow, ReferenceNames.NAME_ITEM_ENDER_BOW);
        }
        if (Configs.disableItemEnderBucket.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderBucket, ReferenceNames.NAME_ITEM_ENDER_BUCKET);
        }
        if (Configs.disableItemEnderLasso.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderLasso, ReferenceNames.NAME_ITEM_ENDER_LASSO);
        }
        if (Configs.disableItemEnderPearl.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderPearlReusable, ReferenceNames.NAME_ITEM_ENDER_PEARL_REUSABLE);
        }
        if (Configs.disableItemEnderPorterBasic.getBoolean(false) == false ||
            Configs.disableItemEnderPorterAdvanced.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderPorter, ReferenceNames.NAME_ITEM_ENDER_PORTER);
        }
        if (Configs.disableItemEnderSword.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderSword, ReferenceNames.NAME_ITEM_ENDER_SWORD);
        }
        if (Configs.disableItemEnderTool.getBoolean(false) == false)
        {
            GameRegistry.registerItem(enderTool, ReferenceNames.NAME_ITEM_ENDERTOOL);
        }
        if (Configs.disableItemMobHarness.getBoolean(false) == false)
        {
            GameRegistry.registerItem(mobHarness, ReferenceNames.NAME_ITEM_MOB_HARNESS);
        }

        ItemStack arrow = new ItemStack(Items.arrow);
        ItemStack bow = new ItemStack(Items.bow);
        ItemStack bucket = new ItemStack(Items.bucket);
        ItemStack diamond = new ItemStack(Items.diamond);
        ItemStack eye = new ItemStack(Items.ender_eye);
        ItemStack gold = new ItemStack(Items.gold_ingot);
        ItemStack goldnugget = new ItemStack(Items.gold_nugget);
        ItemStack leather = new ItemStack(Items.leather);
        ItemStack pearl = new ItemStack(Items.ender_pearl);
        //ItemStack powder = new ItemStack(Items.blaze_powder);
        ItemStack rsblock = new ItemStack(Blocks.redstone_block);
        ItemStack string = new ItemStack(Items.string);

        if (Configs.disableRecipeEnderArrow.getBoolean(false) == false && Configs.disableItemEnderArrow.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderArrow), " NP", " AN", "E  ", 'N', goldnugget, 'P', pearl, 'A', arrow, 'E', eye);
        }
        if (Configs.disableRecipeEnderBag.getBoolean(false) == false && Configs.disableItemEnderBag.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBag), "LDL", "DED", "LDL", 'L', leather, 'D', diamond, 'E', eye);
        }
        if (Configs.disableRecipeEnderBow.getBoolean(false) == false && Configs.disableItemEnderBow.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBow), "PDP", "DBD", "PDP", 'P', pearl, 'D', diamond, 'B', bow);
        }
        if (Configs.disableRecipeEnderBucket.getBoolean(false) == false && Configs.disableItemEnderBucket.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBucket), "EGE", "DBD", "EGE", 'E', eye, 'G', gold, 'D', diamond, 'B', bucket);
        }
        if (Configs.disableRecipeEnderLasso.getBoolean(false) == false && Configs.disableItemEnderLasso.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderLasso), "DGD", "GPG", "DSD", 'D', diamond, 'G', gold, 'E', eye, 'P', pearl, 'S', string);
        }
        if (Configs.disableRecipeEnderPearl.getBoolean(false) == false && Configs.disableItemEnderPearl.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable), "PEP", "ERE", "PEP", 'P', pearl, 'E', eye, 'R', rsblock); // regular pearl
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable, 1, 1), " D ", "DPD", " D ", 'D', diamond, 'P', new ItemStack(enderPearlReusable, 1, 0)); // Elite pearl
        }
        if (Configs.disableRecipeEnderPorterBasic.getBoolean(false) == false &&
            (Configs.disableItemEnderPorterBasic.getBoolean(false) == false ||
            Configs.disableItemEnderPorterAdvanced.getBoolean(false) == false))
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter), "PNP", "NRN", "PNP", 'P', pearl, 'N', goldnugget, 'R', rsblock);
        }
        if (Configs.disableRecipeEnderPorterAdvanced.getBoolean(false) == false &&
                (Configs.disableItemEnderPorterBasic.getBoolean(false) == false ||
                Configs.disableItemEnderPorterAdvanced.getBoolean(false) == false))
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter, 1, 1), "GDG", "DPD", "GDG", 'G', gold, 'D', diamond, 'P', new ItemStack(enderPorter, 1, 0)); // Ender Porter (Advanced)
        }
        if (Configs.disableRecipeMobHarness.getBoolean(false) == false && Configs.disableItemMobHarness.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(mobHarness), "LEL", "LDL", "LEL", 'L', leather, 'E', eye, 'D', diamond);
        }
    }
}
