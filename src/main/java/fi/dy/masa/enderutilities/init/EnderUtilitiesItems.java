package fi.dy.masa.enderutilities.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.item.ItemEU;
import fi.dy.masa.enderutilities.item.ItemEnderArrow;
import fi.dy.masa.enderutilities.item.ItemEnderBag;
import fi.dy.masa.enderutilities.item.ItemEnderBow;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemEnderPearlReusable;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class EnderUtilitiesItems
{
	public static final ItemEU enderArrow = new ItemEnderArrow();
	public static final ItemEU enderBag = new ItemEnderBag();
	public static final ItemEU enderBow = new ItemEnderBow();
	public static final Item enderBucket = new ItemEnderBucket();
	public static final ItemEU enderLasso = new ItemEnderLasso();
	public static final ItemEU enderPearlReusable = new ItemEnderPearlReusable();
	public static final ItemEU mobHarness = new ItemMobHarness();

	public static void init()
	{
		GameRegistry.registerItem(enderPearlReusable, ReferenceItem.NAME_ITEM_ENDER_PEARL_REUSABLE);
		GameRegistry.registerItem(enderBow, ReferenceItem.NAME_ITEM_ENDER_BOW);
		GameRegistry.registerItem(enderArrow, ReferenceItem.NAME_ITEM_ENDER_ARROW);
		GameRegistry.registerItem(enderLasso, ReferenceItem.NAME_ITEM_ENDER_LASSO);
		GameRegistry.registerItem(enderBucket, ReferenceItem.NAME_ITEM_ENDER_BUCKET);
		GameRegistry.registerItem(enderBag, ReferenceItem.NAME_ITEM_ENDER_BAG);
		GameRegistry.registerItem(mobHarness, ReferenceItem.NAME_ITEM_MOB_HARNESS);

		ItemStack arrow = new ItemStack(Items.arrow);
		ItemStack bucket = new ItemStack(Items.bucket);
		ItemStack diamond = new ItemStack(Items.diamond);
		ItemStack eye = new ItemStack(Items.ender_eye);
		ItemStack gold = new ItemStack(Items.gold_ingot);
		ItemStack leather = new ItemStack(Items.leather);
		ItemStack pearl = new ItemStack(Items.ender_pearl);
		ItemStack powder = new ItemStack(Items.blaze_powder);
		ItemStack rsblock = new ItemStack(Blocks.redstone_block);
		ItemStack string = new ItemStack(Items.string);
		ItemStack stick = new ItemStack(Items.stick);

		GameRegistry.addShapelessRecipe(new ItemStack(enderArrow), eye, arrow, gold);
		GameRegistry.addRecipe(new ItemStack(enderBag), "BLB", "LEL", "BLB", 'B', powder, 'L', leather, 'E', eye);
		GameRegistry.addRecipe(new ItemStack(enderBow), "SE ", "S T", "SE ", 'S', string, 'T', stick, 'E', eye);
		GameRegistry.addRecipe(new ItemStack(enderBucket), "EGE", "DBD", "EGE", 'E', eye, 'G', gold, 'D', diamond, 'B', bucket);
		GameRegistry.addRecipe(new ItemStack(enderLasso), "DED", "EGE", "DSD", 'D', diamond, 'E', eye, 'G', gold, 'S', string);
		GameRegistry.addRecipe(new ItemStack(enderPearlReusable), "PEP", "ERE", "PEP", 'P', pearl, 'E', eye, 'R', rsblock);
		GameRegistry.addRecipe(new ItemStack(mobHarness), "LEL", "LDL", "LEL", 'L', leather, 'E', eye, 'D', diamond);
	}
}
