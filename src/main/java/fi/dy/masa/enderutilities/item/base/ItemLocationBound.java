package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public class ItemLocationBound extends ItemEnderUtilities
{
    public ItemLocationBound()
    {
        super();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (stack == null)
        {
            return false;
        }

        if (player.isSneaking() == true)
        {
            boolean adjustPosHit = true;

            // Don't adjust the target position for uses that are targeting the block, not the actual exact location.
            if (stack.getItem() == EnderUtilitiesItems.linkCrystal && stack.getItemDamage() != 0)
            {
                adjustPosHit = false;
            }

            NBTTagCompound nbt = stack.getTagCompound();
            nbt = NBTHelperTarget.writeTargetTagToNBT(nbt, x, y, z, player.dimension, side, hitX, hitY, hitZ, adjustPosHit);
            nbt = NBTHelperPlayer.writePlayerTagToNBT(nbt, player);
            stack.setTagCompound(nbt);

            return true;
        }

        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        NBTHelperTarget target = NBTHelperTarget.getTarget(stack);
        if (target != null)
        {
            String pre = EnumChatFormatting.GREEN.toString();
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();
            String dimName = TooltipHelper.getDimensionName(target.dimension, target.dimensionName, true);
            return super.getItemStackDisplayName(stack) + " " + pre + dimName + rst;
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        NBTHelperTarget target = NBTHelperTarget.getTarget(stack);
        if (target == null)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.notargetset"));
            return;
        }

        String dimPre = EnumChatFormatting.DARK_GREEN.toString();
        String numPre = EnumChatFormatting.BLUE.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        String dimName = TooltipHelper.getDimensionName(target.dimension, target.dimensionName, false);

        // Full tooltip
        if (verbose == true)
        {
            String s = StatCollector.translateToLocal("enderutilities.tooltip.dimension") + ": " + numPre + target.dimension + rst;
            if (dimName.length() > 0)
            {
                s = s + " - " + dimPre + dimName + rst;
            }

            list.add(s);
            list.add(String.format("x: %s%.2f%s y: %s%.2f%s z: %s%.2f%s", numPre, target.dPosX, rst, numPre, target.dPosY, rst, numPre, target.dPosZ, rst));

            // For debug:
            //list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s", coordPre, target.posX, rst, coordPre, target.posY, rst, coordPre, target.posZ, rst));
        }
        // Compact/short tooltip
        else
        {
            String s = dimPre + dimName + rst;
            if (dimName.length() == 0)
            {
                s = StatCollector.translateToLocal("enderutilities.tooltip.dimension.compact") + ": " + numPre + target.dimension + rst;
            }

            list.add(String.format("%s - %s%.2f%s %s%.2f%s %s%.2f%s", s, numPre, target.dPosX, rst, numPre, target.dPosY, rst, numPre, target.dPosZ, rst));
        }


    }
}
