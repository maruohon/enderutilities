package fi.dy.masa.minecraft.mods.enderutilities.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.minecraft.mods.enderutilities.init.ModItems;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class CreativeTab
{
	public static final CreativeTabs ENDER_UTILITIES_TAB = new CreativeTabs(Reference.MOD_ID)
	{
		@Override
		public Item getTabIconItem()
		{
			return ModItems.enderBag;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public String getTranslatedTabLabel()
		{
			return Reference.MOD_NAME;
		}
	};
}
