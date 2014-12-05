package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.TooltipHelper;
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

            // Don't adjust the target position for uses that are targeting the block, not the in-world location
            if (stack.getItem() == EnderUtilitiesItems.linkCrystal && stack.getItemDamage() != 0)
            {
                adjustPosHit = false;
            }

            stack.setTagCompound(NBTHelperTarget.writeTargetTagToNBT(stack.getTagCompound(), x, y, z, player.dimension, side, hitX, hitY, hitZ, adjustPosHit));

            return true;
        }

        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        NBTHelperTarget target = new NBTHelperTarget();

        if (target.readTargetTagFromNBT(stack.getTagCompound()) != null)
        {
            String dimName = TooltipHelper.getDimensionName(target.dimension, target.dimensionName, true);
            return super.getItemStackDisplayName(stack) + " (" + dimName + ")";
        }

        return super.getItemStackDisplayName(stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        /*if (EnderUtilities.proxy.isShiftKeyDown() == false)
        {
            list.add("<" + StatCollector.translateToLocal("gui.tooltip.holdshift") + ">");
            return;
        }*/

        NBTTagCompound nbt = stack.getTagCompound();
        NBTHelperTarget target = new NBTHelperTarget();
        if (target.readTargetTagFromNBT(nbt) == null)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
            return;
        }

        String dimPre = "" + EnumChatFormatting.DARK_GREEN;
        String coordPre = "" + EnumChatFormatting.BLUE;
        String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

        list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + coordPre + target.dimension + rst
                + " " + dimPre + TooltipHelper.getDimensionName(target.dimension, target.dimensionName, false) + rst);
        list.add(String.format("x: %s%.2f%s y: %s%.2f%s z: %s%.2f%s", coordPre, target.dPosX, rst, coordPre, target.dPosY, rst, coordPre, target.dPosZ, rst));
        // For debug:
        //list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s", coordPre, target.posX, rst, coordPre, target.posY, rst, coordPre, target.posZ, rst));
    }
}
