package fi.dy.masa.minecraft.mods.enderutilities.init;

import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.minecraft.mods.enderutilities.items.EnderBag;
import fi.dy.masa.minecraft.mods.enderutilities.items.EnderBucket;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class ModItems
{
	public static final Item enderBag = new EnderBag();
	public static final Item enderBucket = new EnderBucket();

	public static void init()
	{
		GameRegistry.registerItem(enderBag, Reference.NAME_ITEM_ENDER_BAG);
		GameRegistry.registerItem(enderBucket, Reference.NAME_ITEM_ENDER_BUCKET);
	}
}
