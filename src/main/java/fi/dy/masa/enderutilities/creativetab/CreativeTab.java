package fi.dy.masa.enderutilities.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.reference.Reference;

public class CreativeTab
{
	public static final CreativeTabs ENDER_UTILITIES_TAB = new CreativeTabs(Reference.MOD_ID)
	{
		@SideOnly(Side.CLIENT)
		@Override
		public Item getTabIconItem()
		{
			return EnderUtilitiesItems.enderPearlReusable;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public String getTranslatedTabLabel()
		{
			return Reference.MOD_NAME;
		}
	};
}
