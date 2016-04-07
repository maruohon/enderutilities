package fi.dy.masa.enderutilities.item.base;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class ItemEnderUtilities extends Item
{
    public String name;

    public ItemEnderUtilities()
    {
        super();
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.addItemOverrides();
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

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips)
    {
        ArrayList<String> tmpList = new ArrayList<String>();
        boolean verbose = EnderUtilities.proxy.isShiftKeyDown();

        // "Fresh" items without NBT data: display the tips before the usual tooltip data
        if (stack.getTagCompound() == null)
        {
            this.addTooltips(stack, tmpList, verbose);

            if (verbose == false && tmpList.size() > 1)
            {
                list.add(I18n.translateToLocal("enderutilities.tooltip.item.holdshiftfordescription"));
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
            if (tmpList.size() > 0)
            {
                list.add(tmpList.get(0));
            }
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.holdshift"));
        }
        else
        {
            list.addAll(tmpList);
        }
    }

    public static void addTooltips(String key, List<String> list, boolean verbose)
    {
        String translated = I18n.translateToLocal(key);
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

    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(this.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation[] getItemVariants()
    {
        return new ResourceLocation[] { Item.itemRegistry.getNameForObject(this) };
    }

    @SideOnly(Side.CLIENT)
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        return null;
    }

    protected void addItemOverrides()
    {
    }
}
