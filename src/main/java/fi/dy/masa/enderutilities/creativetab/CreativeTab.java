package fi.dy.masa.enderutilities.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;

public class CreativeTab
{
    public static final CreativeTabs ENDER_UTILITIES_TAB = new CreativeTabs(Reference.MOD_ID)
    {
        @SideOnly(Side.CLIENT)
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, 1);
        }

        @SideOnly(Side.CLIENT)
        @Override
        public String getTranslatedTabLabel()
        {
            return Reference.MOD_NAME;
        }
    };
}
