package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class ItemLocationBound extends ItemEnderUtilities implements ILocationBound, IKeyBound
{
    public ItemLocationBound()
    {
        super();
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (player != null && player.isSneaking() == true)
        {
            if (world.isRemote == false)
            {
                boolean adjustPosHit = stack.getItem() == EnderUtilitiesItems.linkCrystal && ((ItemLinkCrystal)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;
                this.setTarget(stack, player, pos, side, hitX, hitY, hitZ, adjustPosHit, false);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        if (player.isSneaking() == true)
        {
            if (world.isRemote == false)
            {
                this.setTarget(stack, player, true);
            }

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    @Override
    public String getBaseItemDisplayName(ItemStack stack)
    {
        String itemName = super.getBaseItemDisplayName(stack);
        if (itemName.length() >= 14 && this.shouldDisplayTargetName(stack) == true)
        {
            itemName = EUStringUtils.getInitialsWithDots(itemName);
        }

        return itemName;
    }

    @Override
    public boolean shouldDisplayTargetName(ItemStack stack)
    {
        if (stack.hasDisplayName() == true)
        {
            return false;
        }

        String targetName = this.getTargetDisplayName(stack);
        return targetName != null && targetName.length() > 0;
    }

    @Override
    public String getTargetDisplayName(ItemStack stack)
    {
        TargetData target = TargetData.getTargetFromItem(stack);
        return target != null ? target.getTargetBlockDisplayName() : null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (this.shouldDisplayTargetName(stack) == true)
        {
            String targetName = this.getTargetDisplayName(stack);
            String pre = TextFormatting.GREEN.toString();
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

            return this.getBaseItemDisplayName(stack) + " " + pre + targetName + rst;
        }

        return super.getBaseItemDisplayName(stack);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        TargetData target = this.getTarget(stack);
        if (target == null)
        {
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.notargetset"));
            return;
        }

        String preBlue = TextFormatting.BLUE.toString();
        String preDGreen = TextFormatting.DARK_GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        if (OwnerData.canAccessItem(stack, player) == true)
        {
            String dimName = target.getDimensionName(false);

            boolean showBlock = false;
            String blockName = "";
            Item item = stack.getItem();
            // Show the target block info for block type link crystals
            if (item instanceof IModule && ((IModule)item).getModuleType(stack).equals(ModuleType.TYPE_LINKCRYSTAL) && ((IModule)item).getModuleTier(stack) == ItemLinkCrystal.TYPE_BLOCK)
            {
                Block block = Block.getBlockFromName(target.blockName);
                ItemStack targetStack = new ItemStack(block, 1, target.itemMeta);
                if (targetStack != null && targetStack.getItem() != null)
                {
                    blockName = targetStack.getDisplayName();
                    showBlock = true;
                }
            }

            // Full tooltip
            if (verbose == true)
            {
                String s = I18n.translateToLocal("enderutilities.tooltip.dimension") + ": " + preBlue + target.dimension + rst;
                if (dimName.length() > 0)
                {
                    s = s + " - " + preDGreen + dimName + rst;
                }
                list.add(s);
                list.add(String.format("x: %s%.2f%s y: %s%.2f%s z: %s%.2f%s", preBlue, target.dPosX, rst, preBlue, target.dPosY, rst, preBlue, target.dPosZ, rst));

                if (showBlock == true)
                {
                    list.add(I18n.translateToLocal("enderutilities.tooltip.item.target") + ": " + preDGreen + blockName + rst);
                    if (advancedTooltips == true)
                    {
                        list.add(String.format("%s meta: %d Side: %s (%d)", target.blockName, target.blockMeta, target.facing, target.blockFace));
                    }
                }
            }
            // Compact/short tooltip
            else
            {
                String s = preDGreen + dimName + rst;
                if (dimName.length() == 0)
                {
                    s = I18n.translateToLocal("enderutilities.tooltip.dimension.compact") + ": " + preBlue + target.dimension + rst;
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
        OwnerData playerData = OwnerData.getPlayerDataFromItem(stack);
        if (playerData == null)
        {
            return;
        }

        String strPublic = "";
        if (playerData.getIsPublic() == true)
        {
            strPublic = TextFormatting.GREEN.toString() + I18n.translateToLocal("enderutilities.tooltip.item.public") + rst;
        }
        else
        {
            strPublic = TextFormatting.RED.toString() + I18n.translateToLocal("enderutilities.tooltip.item.private") + rst;
        }

        // Full tooltip
        if (verbose == true)
        {
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.mode") + ": " + strPublic);
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.owner") + ": " + preDGreen + playerData.getOwnerName() + rst);
        }
        // Compact/short tooltip
        else
        {
            list.add(strPublic + " - " + preDGreen + playerData.getOwnerName() + rst);
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
        if (OwnerData.itemHasPlayerTag(stack) == false)
        {
            OwnerData.writePlayerTagToItem(stack, player, false);
        }
        else
        {
            OwnerData data = OwnerData.getPlayerDataFromItem(stack);
            if (data != null && data.isOwner(player) == true)
            {
                data.setIsPublic(! data.getIsPublic());
                data.writeToItem(stack);
            }
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
    public TargetData getTarget(ItemStack stack)
    {
        return TargetData.getTargetFromItem(stack);
    }

    public void setTarget(ItemStack stack, EntityPlayer player, boolean storeRotation)
    {
        BlockPos pos = player.getPosition();
        double hitX = player.posX - pos.getX();
        double hitY = player.posY - pos.getY();
        double hitZ = player.posZ - pos.getZ();
        //System.out.printf("x: %d y: %d z: %d hit: %.3f %.3f %.3f\n", x, y, z, hitX, hitY, hitZ);
        boolean adjustPosHit = stack.getItem() == EnderUtilitiesItems.linkCrystal && ((ItemLinkCrystal)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;

        this.setTarget(stack, player, pos, EnumFacing.UP, hitX, hitY, hitZ, adjustPosHit, storeRotation);
    }

    @Override
    public void setTarget(ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing side, double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeRotation)
    {
        if (OwnerData.canAccessItem(stack, player) == false)
        {
            return;
        }

        TargetData.writeTargetTagToItem(stack, pos, player.dimension, side, player, hitX, hitY, hitZ, doHitOffset, player.rotationYaw, player.rotationPitch, storeRotation);

        if (OwnerData.itemHasPlayerTag(stack) == false)
        {
            OwnerData.writePlayerTagToItem(stack, player, true);
        }
    }
}
