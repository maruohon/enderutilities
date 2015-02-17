package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public class ItemLocationBound extends ItemEnderUtilities implements ILocationBound, IKeyBound
{
    public ItemLocationBound()
    {
        super();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (stack == null || stack.getItem() == null || player == null || player.isSneaking() == false || NBTHelperPlayer.canAccessItem(stack, player) == false)
        {
            return false;
        }

        Item item = stack.getItem();
        boolean adjustPosHit = item == EnderUtilitiesItems.linkCrystal && ((ItemLinkCrystal)item).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;
        this.setTarget(stack, x, y, z, player.dimension, side, hitX, hitY, hitZ, adjustPosHit);

        if (NBTHelperPlayer.itemHasPlayerTag(stack) == false)
        {
            NBTHelperPlayer.writePlayerTagToItem(stack, player, true);
        }

        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        NBTHelperTarget target = this.getTarget(stack);
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
        NBTHelperTarget target = this.getTarget(stack);
        if (target == null)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.notargetset"));
            return;
        }

        String preBlue = EnumChatFormatting.BLUE.toString();
        String preDGreen = EnumChatFormatting.DARK_GREEN.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        if (NBTHelperPlayer.canAccessItem(stack, player) == true)
        {
            String dimName = TooltipHelper.getDimensionName(target.dimension, target.dimensionName, false);

            boolean showBlock = false;
            String blockName = "";
            Item item = stack.getItem();
            // Show the target block info for block type link crystals
            if (item instanceof IModule && ((IModule)item).getModuleType(stack).equals(ModuleType.TYPE_LINKCRYSTAL) && ((IModule)item).getModuleTier(stack) == ItemLinkCrystal.TYPE_BLOCK)
            {
                // FIXME this may not be accurate if the damage is different than the meta! See util.BlockInfo.getBasicBlockInfo() in the TellMe mod.
                ItemStack targetStack = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF);
                if (targetStack != null && targetStack.getItem() != null)
                {
                    blockName = targetStack.getDisplayName();
                    showBlock = true;
                }
            }

            // Full tooltip
            if (verbose == true)
            {
                String s = StatCollector.translateToLocal("enderutilities.tooltip.dimension") + ": " + preBlue + target.dimension + rst;
                if (dimName.length() > 0)
                {
                    s = s + " - " + preDGreen + dimName + rst;
                }
                list.add(s);
                list.add(String.format("x: %s%.2f%s y: %s%.2f%s z: %s%.2f%s", preBlue, target.dPosX, rst, preBlue, target.dPosY, rst, preBlue, target.dPosZ, rst));

                if (showBlock == true)
                {
                    list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.target") + ": " + preDGreen + blockName + rst);
                    if (advancedTooltips == true)
                    {
                        list.add("(" + target.blockName + "#" + target.blockMeta + " side: " + ForgeDirection.getOrientation(target.blockFace) + ")");
                    }
                }
            }
            // Compact/short tooltip
            else
            {
                String s = preDGreen + dimName + rst;
                if (dimName.length() == 0)
                {
                    s = StatCollector.translateToLocal("enderutilities.tooltip.dimension.compact") + ": " + preBlue + target.dimension + rst;
                }

                if (showBlock == true)
                {
                    list.add(String.format("%s%s%s - %s @ %s%.2f%s %s%.2f%s %s%.2f%s", preDGreen, blockName, rst, s, preBlue, target.dPosX, rst, preBlue, target.dPosY, rst, preBlue, target.dPosZ, rst));
                }
                else
                {
                    list.add(String.format("%s @ %s%.2f%s %s%.2f%s %s%.2f%s", s, preBlue, target.dPosX, rst, preBlue, target.dPosY, rst, preBlue, target.dPosZ, rst));
                }
            }
        }

        // Player tag data
        NBTHelperPlayer playerData = NBTHelperPlayer.getPlayerDataFromItem(stack);
        if (playerData == null)
        {
            return;
        }

        String strPublic = "";
        if (playerData.isPublic == true)
        {
            strPublic = EnumChatFormatting.GREEN.toString() + StatCollector.translateToLocal("enderutilities.tooltip.item.public") + rst;
        }
        else
        {
            strPublic = EnumChatFormatting.RED.toString() + StatCollector.translateToLocal("enderutilities.tooltip.item.private") + rst;
        }

        // Full tooltip
        if (verbose == true)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + strPublic);
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.owner") + ": " + preDGreen + playerData.playerName + rst);
        }
        // Compact/short tooltip
        else
        {
            list.add(strPublic + " - " + preDGreen + playerData.playerName + rst);
        }
    }

    /**
     * Toggles between public and private mode, if this item has a plyer tag,
     * and if the player is the owner of this item.
     * @param stack
     * @param player
     */
    public void changePrivacyMode(ItemStack stack, EntityPlayer player)
    {
        NBTHelperPlayer data = NBTHelperPlayer.getPlayerDataFromItem(stack);
        if (data != null && data.isOwner(player) == true)
        {
            data.isPublic = ! data.isPublic;
            data.writeToItem(stack);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Alt + Toggle mode: Toggle the private/public mode
        if (ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.changePrivacyMode(stack, player);
        }
    }

    @Override
    public NBTHelperTarget getTarget(ItemStack stack)
    {
        return NBTHelperTarget.getTargetFromItem(stack);
    }

    @Override
    public void setTarget(ItemStack stack, int x, int y, int z, int dim, int blockFace, double hitX, double hitY, double hitZ, boolean doHitOffset)
    {
        if (stack == null)
        {
            return;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        nbt = NBTHelperTarget.writeTargetTagToNBT(nbt, x, y, z, dim, blockFace, hitX, hitY, hitZ, doHitOffset);
        stack.setTagCompound(nbt);
    }
}
