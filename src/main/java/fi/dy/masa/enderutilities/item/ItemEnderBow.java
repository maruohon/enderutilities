package fi.dy.masa.enderutilities.item;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.item.base.IAnvilRepairable;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBow extends ItemLocationBoundModular implements IKeyBound, IAnvilRepairable
{
    public static final int ENDER_CHARGE_COST_MOB_TP = 1000;
    public static final byte BOW_MODE_TP_TARGET = 0;
    public static final byte BOW_MODE_TP_SELF = 1;

    private final ItemStack repairMaterial;

    public ItemEnderBow(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(384);
        this.setNoRepair();
        this.commonTooltip = "enderutilities.tooltips.itemlocationboundmodular";
        this.repairMaterial = new ItemStack(EnderUtilitiesItems.ENDER_PART, 1, 1); // Enhanced Ender Alloy
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

        if (this.isBroken(stack))
        {
            return;
        }

        byte mode = this.getBowMode(stack);

        // Do nothing on the client side
        if (world.isRemote || (mode == BOW_MODE_TP_TARGET && player != null &&
                OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false))
        {
            return;
        }

        // If self teleporting is disabled in the configs, do nothing
        if (mode == BOW_MODE_TP_SELF && Configs.enderBowAllowSelfTP == false)
        {
            return;
        }

        if (player != null && player.capabilities.isCreativeMode == false &&
            player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.ENDER_ARROW)) == false)
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
            TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

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

            IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null); // null: joined wrapper
            InventoryUtils.extractItems(inv, EnderUtilitiesItems.ENDER_ARROW, 1);

            stack.damageItem(1, player);

            // Tool just broke FIXME this doesn't work when called for the player, for some reason...
            if (this.isBroken(stack))
            {
                player.renderBrokenItemStack(stack);
            }
        }

        if (f == 1.0F)
        {
            entityenderarrow.setIsCritical(true);
        }

        world.playSound(null, livingBase.posX, livingBase.posY, livingBase.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
        world.spawnEntity(entityenderarrow);
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        // This method needs to also be executed on the client, otherwise the bow won't be set to in use

        if (this.isBroken(stack))
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        if (this.getBowMode(stack) == BOW_MODE_TP_TARGET)
        {
            // In survival teleporting targets requires Ender Charge
            if ((player.capabilities.isCreativeMode == false && UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST_MOB_TP, true) == false) ||
                OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false ||
                TargetData.selectedModuleHasTargetTag(stack, ModuleType.TYPE_LINKCRYSTAL) == false)
            {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }
        }

        if (player.capabilities.isCreativeMode || player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.ENDER_ARROW)))
        {
            player.setActiveHand(hand);

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    public boolean isBroken(ItemStack stack)
    {
        return stack.getItemDamage() >= this.getMaxDamage(stack);
    }

    @Override
    public boolean repairItem(ItemStack stack, int amount)
    {
        if (amount == -1)
        {
            amount = this.getMaxDamage(stack);
        }

        int damage = Math.max(this.getDamage(stack) - amount, 0);
        boolean repaired = damage != this.getDamage(stack);

        this.setDamage(stack, damage);

        return repaired;
    }

    @Override
    public boolean isRepairItem(@Nonnull ItemStack stackTool, @Nonnull ItemStack stackMaterial)
    {
        return InventoryUtils.areItemStacksEqual(stackMaterial, this.repairMaterial);
    }

    @Override
    public boolean canApplyEnchantment(ItemStack stackTool, Enchantment enchantment)
    {
        return enchantment.type == EnumEnchantmentType.ALL ||
               enchantment.type == EnumEnchantmentType.BREAKABLE;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (this.getBowMode(stack) == BOW_MODE_TP_SELF)
        {
            return this.getBaseItemDisplayName(stack);
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        String preDGreen = TextFormatting.DARK_GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        // TP self to impact point
        if (this.getBowMode(stack) == BOW_MODE_TP_SELF)
        {
            list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + preDGreen + I18n.format("enderutilities.tooltip.item.tpself") + rst);
        }
        // TP the target entity
        else
        {
            super.addTooltipLines(stack, player, list, advancedTooltips, verbose);
            list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + preDGreen + I18n.format("enderutilities.tooltip.item.tptarget") + rst);
        }
    }

    private byte getBowMode(ItemStack stack)
    {
        if (stack.getTagCompound() != null)
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
        if (key == HotKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.toggleBowMode(player, stack);
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    @Override
    public boolean useBindLocking(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 10;
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
            return 8;
        }

        if (moduleType.equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            return 1;
        }

        return 0;
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "pull"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                if (entityIn == null)
                {
                    return 0F;
                }
                else
                {
                    stack = entityIn.getActiveItemStack();

                    if (stack.isEmpty() == false)
                    {
                        return (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F;
                    }

                    return 0F;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "pulling"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "broken"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return ItemEnderBow.this.isBroken(stack) ? 1.0F : 0.0F;
            }
        });
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "mode"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return ItemEnderBow.this.getBowMode(stack) == BOW_MODE_TP_SELF ? 1.0F : 0.0F;
            }
        });
    }
}
