package fi.dy.masa.enderutilities.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;

public class CreativeTab
{
    public static final CreativeTabs ENDER_UTILITIES_TAB = new CreativeTabs(Reference.MOD_ID)
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(EnderUtilitiesItems.ENDER_PEARL_REUSABLE, 1, 1);
        }

        @Override
        public String getTranslationKey()
        {
            return Reference.MOD_NAME;
        }
    };
}
