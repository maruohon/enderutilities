package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;

public class ItemEnderUtilities extends Item
{
    public ItemEnderUtilities()
    {
        super();
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
    }

    /*
     * Custom addInformation() method, which allows selecting a subset of the tooltip strings via a bitmask supplied in the parameter 'selection'.
     * Value '0' will return a compact version of the tooltip.
     * Value '-1' will return the full tooltip.
     */
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, int selection)
    {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips)
    {
        if (EnderUtilities.proxy.isShiftKeyDown() == false)
        {
            this.addInformationSelective(stack, player, list, advancedTooltips, 0);
            return;
        }

        this.addInformationSelective(stack, player, list, advancedTooltips, -1);
    }
}
