package fi.dy.masa.enderutilities.item.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemBlockStorage extends ItemBlockEnderUtilities
{
    public static final String PRE_BLUE = TextFormatting.BLUE.toString();
    public static final String PRE_GREEN = TextFormatting.GREEN.toString();
    public static final String PRE_WHITE = TextFormatting.WHITE.toString();
    public static final String RST_GRAY = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
    public static final String RST_WHITE = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

    public ItemBlockStorage(Block block)
    {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        List<ItemStack> storedItems = NBTUtils.readStoredItemsFromStack(stack, "BlockEntityTag", "Items");

        if (storedItems.isEmpty() == false)
        {
            int count = storedItems.size();

            for (int i = 0; i < count && i < 16; i++)
            {
                ItemStack stackTmp = storedItems.get(i);
                String countStr = String.format("%s", EUStringUtils.formatNumberWithKSeparators(stackTmp.stackSize));
                list.add(String.format("%s (%s%s%s)", stackTmp.getDisplayName(), PRE_WHITE, countStr, RST_GRAY));
            }

            if (count > 16)
            {
                String str1 = I18n.format("enderutilities.tooltip.item.and");
                String str2 = I18n.format("enderutilities.tooltip.item.morestacksnotlisted");
                list.add(String.format("  ... %s %s%d%s %s", str1, PRE_WHITE, 16 - count, RST_GRAY, str2));
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String name = super.getItemStackDisplayName(stack);
        List<ItemStack> storedItems = NBTUtils.readStoredItemsFromStack(stack, "BlockEntityTag", "Items");

        if (storedItems.size() == 1)
        {
            String countStr = EUStringUtils.getStackSizeString(storedItems.get(0), 4);
            name = String.format("%s - %s%s%s (%s)", name, PRE_GREEN, storedItems.get(0).getDisplayName(), RST_WHITE, countStr);
        }
        else if (storedItems.size() > 0)
        {
            name = String.format("%s (%d %s)", name, storedItems.size(), I18n.format("enderutilities.tooltip.item.stacks"));
        }

        return name;
    }
}
