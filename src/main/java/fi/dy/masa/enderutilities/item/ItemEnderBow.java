package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBow extends ItemLocationBoundModular implements IKeyBound
{
    public static final int ENDER_CHARGE_COST_MOB_TP = 1000;
    public static final byte BOW_MODE_TP_TARGET = 0;
    public static final byte BOW_MODE_TP_SELF = 1;

    /*public static final String[] bowPullIconNameArray = new String[] {"standby", "pulling.0", "pulling.1", "pulling.2",
                            "mode2.standby", "mode2.pulling.0", "mode2.pulling.1", "mode2.pulling.2"};*/

    public ItemEnderBow()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(384);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BOW);
    }

    /**
     * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int itemInUseCount)
    {
        // Remove the client-side useCount counter that is used for the texture animation
        if (world.isRemote == true)
        {
            if (stack.getTagCompound() != null)
            {
                stack.getTagCompound().removeTag("useCount");
            }
            return;
        }

        byte mode = this.getBowMode(stack);
        if (mode == BOW_MODE_TP_TARGET && NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return;
        }

        // If self teleporting is disabled in the configs, do nothing
        if (mode == BOW_MODE_TP_SELF && Configs.enderBowAllowSelfTP.getBoolean(true) == false)
        {
            return;
        }

        if (player.capabilities.isCreativeMode == false && player.inventory.hasItem(EnderUtilitiesItems.enderArrow) == false)
        {
            return;
        }

        int j = this.getMaxItemUseDuration(stack) - itemInUseCount;

        ArrowLooseEvent event = new ArrowLooseEvent(player, stack, j);
        if (MinecraftForge.EVENT_BUS.post(event) == true || event.isCanceled() == true)
        {
            return;
        }

        j = event.charge;
        float f = (float)j / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        if (f < 0.1f) { return; }
        if (f > 1.0f) { f = 1.0f; }

        EntityEnderArrow entityenderarrow = new EntityEnderArrow(world, player, f * 2.0f);
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
            if (UtilItemModular.getModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                entityenderarrow.setPersistence(true);
            }
        }

        if (player.capabilities.isCreativeMode == false)
        {
            if (mode == BOW_MODE_TP_TARGET && UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST_MOB_TP, true) == false)
            {
                return;
            }

            player.inventory.consumeInventoryItem(EnderUtilitiesItems.enderArrow);
            stack.damageItem(1, player);
        }

        if (f == 1.0F)
        {
            entityenderarrow.setIsCritical(true);
        }

        world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
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
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        // This method needs to also be executed on the client, otherwise the bow won't be set to in use

        // In survival teleporting targets requires Ender Charge
        if (player.capabilities.isCreativeMode == false && this.getBowMode(stack) == BOW_MODE_TP_TARGET
            && UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST_MOB_TP, false) == false)
        {
            return stack;
        }

        if (this.getBowMode(stack) == BOW_MODE_TP_TARGET && NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return stack;
        }

        ArrowNockEvent event = new ArrowNockEvent(player, stack);
        if (MinecraftForge.EVENT_BUS.post(event) == true || event.isCanceled() == true)
        {
            return event.result;
        }

        // Don't shoot when sneaking and looking at a block, aka. binding the bow to a new location
        if (player.isSneaking() == true)
        {
            MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
            if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                return stack;
            }
        }

        if (player.capabilities.isCreativeMode == true || player.inventory.hasItem(EnderUtilitiesItems.enderArrow))
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null)
            {
                return stack;
            }

            // If the bow is in 'TP target' mode, it has to have a valid target set
            if (nbt.getByte("Mode") == BOW_MODE_TP_TARGET)
            {
                if (NBTHelperTarget.selectedModuleHasTargetTag(stack, ModuleType.TYPE_LINKCRYSTAL) == false)
                {
                    return stack;
                }
            }

            player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        }

        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (this.getBowMode(stack) == BOW_MODE_TP_SELF)
        {
            return StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

        // TP self to impact point
        if (nbt != null && nbt.hasKey("Mode", Constants.NBT.TAG_BYTE) && nbt.getByte("Mode") == BOW_MODE_TP_SELF)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + EnumChatFormatting.RED + StatCollector.translateToLocal("enderutilities.tooltip.item.tpself") + rst);
        }
        // TP the target entity
        else
        {
            super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + EnumChatFormatting.DARK_AQUA + StatCollector.translateToLocal("enderutilities.tooltip.item.tptarget") + rst);
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
    {
        // TODO: Add a method to get the alloy types/tiers
        if (stack1 != null && stack1.getItem() == EnderUtilitiesItems.enderBow
            && stack2 != null && stack2.getItem() == EnderUtilitiesItems.enderPart && stack2.getItemDamage() == 1)
        {
            return true;
        }

        return false;
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
        if (stack == null)
        {
            return;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        byte val = this.getBowMode(stack);
        if (++val > 1)
        {
            val = 0;
        }

        // If self teleporting is disabled in the configs, always set the mode to TP target
        if (Configs.enderBowAllowSelfTP.getBoolean(true) == false)
        {
            val = BOW_MODE_TP_TARGET;
        }

        nbt.setByte("Mode", val);
        stack.setTagCompound(nbt);
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
    public int getMaxModules(ItemStack stack)
    {
        return 5;
    }

    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
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

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
    {
        // Only do this on the _CLIENT_
        if (player.worldObj.isRemote == false)
        {
            return;
        }

        // Add the use count to the NBT on the client side, to be used for changing texture
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        nbt.setInteger("useCount", stack.getMaxItemUseDuration() - count);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getBaseModelName(String variant)
    {
        return ReferenceNames.NAME_ITEM_ENDER_BOW;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerVariants()
    {
        this.addVariants(   this.name + ".standby",
                            this.name + ".pulling.0",
                            this.name + ".pulling.1",
                            this.name + ".pulling.2",
                            this.name + ".mode2.standby",
                            this.name + ".mode2.pulling.0",
                            this.name + ".mode2.pulling.1",
                            this.name + ".mode2.pulling.2");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IFlexibleBakedModel getItemModel(ItemStack stack)
    {
        int index = 0;

        if (stack.getTagCompound() != null)
        {
            if (stack.getTagCompound().getByte("Mode") == 1)
            {
                index = 4;
            }

            int inUse = stack.getTagCompound().getInteger("useCount");

            if (inUse >= 18)
            {
                index += 3;
            }
            else if (inUse >= 13)
            {
                index += 2;
            }
            else if (inUse > 0)
            {
                index += 1;
            }
        }

        return this.models[index < this.textures.length ? index : 0];
    }
}
