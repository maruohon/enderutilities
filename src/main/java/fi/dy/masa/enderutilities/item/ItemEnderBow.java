package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEnderBow extends ItemLocationBoundModular implements IKeyBound
{
    public static final int ENDER_CHARGE_COST_MOB_TP = 1000;
    public static final byte BOW_MODE_TP_TARGET = 0;
    public static final byte BOW_MODE_TP_SELF = 1;

    public ItemEnderBow()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(384);
        this.setNoRepair();
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BOW);
    }

    /**
     * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase livingBase, int itemInUseCount)
    {
        EntityPlayer player = null;

        if (livingBase instanceof EntityPlayer)
        {
            player = (EntityPlayer) livingBase;
        }

        if (this.isBroken(stack) == true)
        {
            return;
        }

        byte mode = this.getBowMode(stack);

        // Do nothing on the client side
        if (world.isRemote == true || (mode == BOW_MODE_TP_TARGET && player != null &&
                NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false))
        {
            return;
        }

        // If self teleporting is disabled in the configs, do nothing
        if (mode == BOW_MODE_TP_SELF && Configs.enderBowAllowSelfTP == false)
        {
            return;
        }

        if (player != null && player.capabilities.isCreativeMode == false && player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.enderArrow)) == false)
        {
            return;
        }

        float f = (float)(this.getMaxItemUseDuration(stack) - itemInUseCount) / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        if (f < 0.1f) { return; }
        if (f > 1.0f) { f = 1.0f; }

        EntityEnderArrow entityenderarrow = new EntityEnderArrow(world, livingBase, f * 2.0f);
        entityenderarrow.setTpMode(mode);

        if (mode == BOW_MODE_TP_TARGET)
        {
            NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
            // If we want to TP the target, we must have a valid target set
            if (target == null)
            {
                return;
            }

            entityenderarrow.setTpTarget(target);

            // If there is a mob persistence module installed, mark that flag on the arrow entity
            if (UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                entityenderarrow.setPersistence(true);
            }
        }

        if (player != null && player.capabilities.isCreativeMode == false)
        {
            if (mode == BOW_MODE_TP_TARGET && UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST_MOB_TP, false) == false)
            {
                return;
            }

            // FIXME 1.9
            //player.inventory.consumeInventoryItem(EnderUtilitiesItems.enderArrow);
            stack.damageItem(1, player);

            // Tool just broke FIXME this doesn't work when called for the player, for some reason...
            if (this.isBroken(stack) == true)
            {
                player.renderBrokenItemStack(stack);
            }
        }

        if (f == 1.0F)
        {
            entityenderarrow.setIsCritical(true);
        }

        world.playSound(null, livingBase.posX, livingBase.posY, livingBase.posZ, SoundEvents.entity_arrow_shoot, SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
        world.spawnEntityInWorld(entityenderarrow);
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BOW;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        // This method needs to also be executed on the client, otherwise the bow won't be set to in use

        if (this.isBroken(stack) == true)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        // In survival teleporting targets requires Ender Charge
        if (player.capabilities.isCreativeMode == false && this.getBowMode(stack) == BOW_MODE_TP_TARGET
            && UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST_MOB_TP, true) == false)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        if (this.getBowMode(stack) == BOW_MODE_TP_TARGET && NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        // Don't shoot when sneaking and looking at a block, aka. binding the bow to a new location
        if (player.isSneaking() == true)
        {
            RayTraceResult rayTraceResult = this.getMovingObjectPositionFromPlayer(world, player, true);
            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }
        }

        if (player.capabilities.isCreativeMode == true || player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.enderArrow)) == true)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null)
            {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }

            // If the bow is in 'TP target' mode, it has to have a valid target set
            if (nbt.getByte("Mode") == BOW_MODE_TP_TARGET)
            {
                if (NBTHelperTarget.selectedModuleHasTargetTag(stack, ModuleType.TYPE_LINKCRYSTAL) == false)
                {
                    return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
                }
            }

            player.setActiveHand(hand);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    public boolean isBroken(ItemStack stack)
    {
        return stack.getItemDamage() >= this.getMaxDamage(stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (this.getBowMode(stack) == BOW_MODE_TP_SELF)
        {
            return I18n.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        String rst = "" + TextFormatting.RESET + TextFormatting.GRAY;

        // TP self to impact point
        if (nbt != null && nbt.hasKey("Mode", Constants.NBT.TAG_BYTE) && nbt.getByte("Mode") == BOW_MODE_TP_SELF)
        {
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.mode") + ": " + TextFormatting.DARK_GREEN + I18n.translateToLocal("enderutilities.tooltip.item.tpself") + rst);
        }
        // TP the target entity
        else
        {
            super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.mode") + ": " + TextFormatting.DARK_GREEN + I18n.translateToLocal("enderutilities.tooltip.item.tptarget") + rst);
        }
    }

    public byte getBowMode(ItemStack stack)
    {
        if (stack != null && stack.getTagCompound() != null)
        {
            return stack.getTagCompound().getByte("Mode");
        }

        return BOW_MODE_TP_TARGET;
    }

    public void toggleBowMode(EntityPlayer player, ItemStack stack)
    {
        // If self teleporting is disabled in the configs, always set the mode to TP target
        if (Configs.enderBowAllowSelfTP == false)
        {
            NBTUtils.setByte(stack, null, "Mode", BOW_MODE_TP_TARGET);
        }
        else
        {
            NBTUtils.cycleByteValue(stack, null, "Mode", 1);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode key: Change Bow mode
        if (key == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.toggleBowMode(player, stack);
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 5;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 3;
        }

        if (moduleType.equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            return 1;
        }

        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "tex=mode.0.standby"),
                new ModelResourceLocation(rl, "tex=mode.0.broken"),
                new ModelResourceLocation(rl, "tex=mode.0.pulling.0"),
                new ModelResourceLocation(rl, "tex=mode.0.pulling.1"),
                new ModelResourceLocation(rl, "tex=mode.0.pulling.2"),
                new ModelResourceLocation(rl, "tex=mode.1.standby"),
                new ModelResourceLocation(rl, "tex=mode.1.broken"),
                new ModelResourceLocation(rl, "tex=mode.1.pulling.0"),
                new ModelResourceLocation(rl, "tex=mode.1.pulling.1"),
                new ModelResourceLocation(rl, "tex=mode.1.pulling.2")
        };
    }

    // FIXME 1.9
    @SideOnly(Side.CLIENT)
    //@Override
    public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining)
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;
        String modeStr = "tex=mode.";
        int mode = 0;

        if (stack.getTagCompound() != null)
        {
            mode = MathHelper.clamp_int(stack.getTagCompound().getByte("Mode"), 0, 1);
        }
        modeStr += mode;

        if (this.isBroken(stack) == true)
        {
            return new ModelResourceLocation(rl, modeStr + ".broken");
        }

        int inUse = stack.getMaxItemUseDuration() - useRemaining;
        //System.out.println("max: " + stack.getMaxItemUseDuration() + " remaining: " + useRemaining + " inUse: " + inUse);

        if (player != null && player.getActiveItemStack() != null)
        {
            if (inUse >= 18)
            {
                return new ModelResourceLocation(rl, modeStr + ".pulling.2");
            }
            else if (inUse >= 13)
            {
                return new ModelResourceLocation(rl, modeStr + ".pulling.1");
            }
            else if (inUse > 0)
            {
                return new ModelResourceLocation(rl, modeStr + ".pulling.0");
            }
        }

        return new ModelResourceLocation(rl, modeStr + ".standby");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        return this.getModel(stack, null, 0);
    }
}
