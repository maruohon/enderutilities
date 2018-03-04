package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStacker;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemQuickStacker extends ItemEnderUtilities implements IKeyBound, IKeyBoundUnselected
{
    public static final String TAG_NAME_CONTAINER = "QuickStacker";
    public static final String TAG_NAME_PRESET_SELECTION = "SelectedPreset";
    public static final String TAG_NAME_PRESET = "Preset_";
    public static final String TAG_NAME_LOCKED = "Locked";

    public static final int NUM_PRESETS = 4;

    public static final int GUI_ACTION_CHANGE_PRESET = 0;
    public static final int GUI_ACTION_TOGGLE_ROWS = 1;
    public static final int GUI_ACTION_TOGGLE_COLUMNS = 2;

    public static final int MAX_RANGE_HORIZONTAL = 4;
    public static final int MAX_RANGE_VERTICAL   = 3;

    public ItemQuickStacker(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote == false)
        {
            player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_QUICK_STACKER, world,
                    (int)player.posX, (int)player.posY, (int)player.posZ);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking())
        {
            TileEntity te = world.getTileEntity(pos);

            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
            {
                IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);

                if (world.isRemote == false && inv != null)
                {
                    quickStackItems(player, player.getHeldItem(hand), inv);
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String itemName = super.getItemStackDisplayName(stack);
        String preGreen = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        byte selected = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        itemName = itemName + " P: " + preGreen + (selected + 1) + rst;

        return itemName;
    }

    @Override
    public void addTooltipLines(ItemStack containerStack, EntityPlayer player, List<String> list, boolean verbose)
    {
        if (containerStack.getTagCompound() == null)
        {
            return;
        }

        String preGreen = TextFormatting.GREEN.toString();
        String preBlue = TextFormatting.BLUE.toString();
        String preRed = TextFormatting.RED.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        String str;

        if (isEnabled(containerStack))
        {
            str = I18n.format("enderutilities.tooltip.item.enabled") + ": " +
                    preGreen + I18n.format("enderutilities.tooltip.item.yes");
        }
        else
        {
            str = I18n.format("enderutilities.tooltip.item.enabled") + ": " +
                    preRed + I18n.format("enderutilities.tooltip.item.no");
        }

        list.add(str);

        byte selected = NBTUtils.getByte(containerStack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        list.add(I18n.format("enderutilities.tooltip.item.preset") + ": " + preBlue + (selected + 1) + rst);
    }

    public static boolean isEnabled(ItemStack stack)
    {
        return NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED) == false;
    }

    /**
     * Returns an ItemStack containing an enabled Inventory Swapper in the player's inventory, or null if none is found.
     */
    public static ItemStack getEnabledItem(EntityPlayer player)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.QUICK_STACKER);

        for (int slot : slots)
        {
            ItemStack stack = playerInv.getStackInSlot(slot);

            if (isEnabled(stack))
            {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static long getEnabledSlotsMask(ItemStack stack)
    {
        byte selected = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        return NBTUtils.getLong(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET + selected);
    }

    public static Result quickStackItems(EntityPlayer player, ItemStack stackerStack, IItemHandler inventory)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        Result ret = TileEntityQuickStackerAdvanced.quickStackItems(playerInv, inventory,
                getEnabledSlotsMask(stackerStack), player.isSneaking() == false, null);

        if (ret != Result.MOVED_NONE)
        {
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
        }

        return ret;
    }

    public static void quickStackItems(EntityPlayer player)
    {
        ItemStack stack = getEnabledItem(player);

        if (stack.isEmpty() == false)
        {
            //PlayerTaskScheduler.getInstance().addTask(player, new TaskPositionDebug(world, getPositions(player), 2), 2);

            TileEntityQuickStackerAdvanced.quickStackToInventories(player.getEntityWorld(), player, getEnabledSlotsMask(stack),
                    PositionUtils.getTileEntityPositions(player.getEntityWorld(),
                            player.getPosition(), MAX_RANGE_HORIZONTAL, MAX_RANGE_VERTICAL, MAX_RANGE_VERTICAL));
        }
    }

    @Override
    public boolean doUnselectedKeyAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Re-fetch the item to check if it's enabled
        stack = getEnabledItem(player);

        if (stack.isEmpty() == false)
        {
            return ((ItemQuickStacker) stack.getItem()).doKeyBindingAction(player, stack, key);
        }

        return false;
    }

    @Override
    public boolean doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode or Shift + Toggle mode: Fire the swapping action
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE, HotKeys.MOD_SHIFT))
        {
            quickStackItems(player);
            return true;
        }
        // Alt + Shift + Toggle mode: Toggle the locked mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED);
            return true;
        }
        // Ctrl + Toggle mode: Cycle the slot mask preset
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
            return true;
        }

        return false;
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerQuickStacker)
        {
            ItemStack stack = ((ContainerQuickStacker) player.openContainer).getContainerItem();

            if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.QUICK_STACKER)
            {
                if (action == GUI_ACTION_CHANGE_PRESET && element >= 0 && element < NUM_PRESETS)
                {
                    NBTUtils.setByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, (byte)element);
                }
                else if (action == GUI_ACTION_TOGGLE_ROWS && element >= 0 && element < 4)
                {
                    long mask = getEnabledSlotsMask(stack);
                    mask ^= (0x1FFL << (element * 9));
                    NBTUtils.setLong(stack, TAG_NAME_CONTAINER,
                            TAG_NAME_PRESET + NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION), mask);
                }
                else if (action == GUI_ACTION_TOGGLE_COLUMNS)
                {
                    long mask = getEnabledSlotsMask(stack);

                    // Player inventory
                    if (element >= 0 && element < 9)
                    {
                        mask ^= (0x08040201L << element); // toggle the bits for the slots in the selected column of the inventory
                    }

                    NBTUtils.setLong(stack, TAG_NAME_CONTAINER,
                            TAG_NAME_PRESET + NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION), mask);
                }
            }
        }
    }

    public enum Result
    {
        MOVED_NONE,
        MOVED_SOME,
        MOVED_ALL;
    }

    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "locked=false"),
                new ModelResourceLocation(rl, "locked=true")
        };
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;
        return new ModelResourceLocation(rl, "locked=" + NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED));
    }
}
