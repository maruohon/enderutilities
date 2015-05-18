package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
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
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ)
    {
        if (player != null && player.isSneaking() == true)
        {
            if (world.isRemote == false)
            {
                boolean adjustPosHit = stack.getItem() == EnderUtilitiesItems.linkCrystal && ((ItemLinkCrystal)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;
                this.setTarget(stack, player, pos, face, hitX, hitY, hitZ, adjustPosHit, false);
            }

            return true;
        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote == false && player != null && player.isSneaking() == true)
        {
            this.setTarget(stack, player, true);
        }

        return stack;
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
                        list.add("(" + target.blockName + "#" + target.blockMeta + " side: " + target.facing.toString() + ")");
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
     * Toggles between public and private mode, if this item has a player tag,
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
        if (stack == null || player == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Alt + Toggle mode: Toggle the private/public mode
        if (ReferenceKeys.keypressContainsAlt(key) == true
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsControl(key) == false)
        {
            this.changePrivacyMode(stack, player);
        }
        // Alt + Shift + Toggle mode: Store the player's current location, including rotation
        else if (ReferenceKeys.keypressContainsAlt(key) == true
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsControl(key) == false)
        {
            this.setTarget(stack, player, true);
        }
    }

    @Override
    public NBTHelperTarget getTarget(ItemStack stack)
    {
        return NBTHelperTarget.getTargetFromItem(stack);
    }

    public void setTarget(ItemStack stack, EntityPlayer player, boolean storeRotation)
    {
        int x = (int)player.posX;
        int y = (int)player.posY;
        int z = (int)player.posZ;
        double hitX = player.posX - x;
        double hitY = player.posY - y;
        double hitZ = player.posZ - z;
        boolean adjustPosHit = stack.getItem() == EnderUtilitiesItems.linkCrystal && ((ItemLinkCrystal)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;

        this.setTarget(stack, player, new BlockPos(x, y, z), EnumFacing.UP, hitX, hitY, hitZ, adjustPosHit, storeRotation);
    }

    @Override
    public void setTarget(ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing face, double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeRotation)
    {
        if (NBTHelperPlayer.canAccessItem(stack, player) == false)
        {
            return;
        }

        NBTHelperTarget.writeTargetTagToItem(stack, pos, player.dimension, face, hitX, hitY, hitZ, doHitOffset, player.rotationYaw, player.rotationPitch, storeRotation);

        if (NBTHelperPlayer.itemHasPlayerTag(stack) == false)
        {
            NBTHelperPlayer.writePlayerTagToItem(stack, player, true);
        }
    }
}
