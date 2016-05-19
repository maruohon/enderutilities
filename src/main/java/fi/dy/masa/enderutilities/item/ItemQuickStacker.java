package fi.dy.masa.enderutilities.item;

import java.util.ArrayList;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStacker;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
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

    public ItemQuickStacker()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_QUICK_STACKER);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote == false)
        {
            player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_QUICK_STACKER, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking() == true)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) == true)
            {
                IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                if (world.isRemote == false && inv != null)
                {
                    this.quickStackItems(player, stack, inv);
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
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
    public void addInformationSelective(ItemStack containerStack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
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
        if (isEnabled(containerStack) == true)
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
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.quickStacker);

        for (int slot : slots)
        {
            ItemStack stack = playerInv.getStackInSlot(slot);

            if (isEnabled(stack) == true)
            {
                return stack;
            }
        }

        return null;
    }

    public static long getEnabledSlotsMask(ItemStack stack)
    {
        byte selected = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        return NBTUtils.getLong(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET + selected);
    }

    /**
     * Tries to move all items from enabled slots in the player's inventory to the given external inventory
     */
    public Result quickStackItems(IItemHandler playerInv, IItemHandler externalInv, long slotMask, boolean matchingOnly)
    {
        Result ret = Result.MOVED_NONE;
        boolean movedAll = true;

        long bit = 0x1;
        for (int slotPlayer = 0; slotPlayer < playerInv.getSlots(); slotPlayer++)
        {
            // Only swap slots that have been enabled
            if ((slotMask & bit) != 0 && playerInv.getStackInSlot(slotPlayer) != null)
            {
                ItemStack stack = playerInv.extractItem(slotPlayer, 64, false);
                if (stack == null)
                {
                    continue;
                }

                if (matchingOnly == false ||
                    InventoryUtils.getSlotOfLastMatchingItemStack(externalInv, stack) != -1)
                {
                    int sizeOrig = stack.stackSize;
                    stack = InventoryUtils.tryInsertItemStackToInventory(externalInv, stack);

                    if (ret == Result.MOVED_NONE && (stack == null || stack.stackSize != sizeOrig))
                    {
                        ret = Result.MOVED_SOME;
                    }
                }

                // Return the items that were left over
                if (stack != null)
                {
                    playerInv.insertItem(slotPlayer, stack, false);
                    movedAll = false;
                }
            }

            bit <<= 1;
        }

        if (movedAll == true && ret == Result.MOVED_SOME)
        {
            ret = Result.MOVED_ALL;
        }

        return ret;
    }

    public Result quickStackItems(EntityPlayer player, ItemStack stackerStack, IItemHandler inventory)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        Result ret = this.quickStackItems(playerInv, inventory, getEnabledSlotsMask(stackerStack), player.isSneaking() == false);
        if (ret != Result.MOVED_NONE)
        {
            player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
        }

        return ret;
    }

    public static void quickStackItems(EntityPlayer player)
    {
        ItemStack stackerStack = getEnabledItem(player);
        if (stackerStack == null)
        {
            return;
        }

        World world = player.worldObj;
        //PlayerTaskScheduler.getInstance().addTask(player, new TaskPositionDebug(world, getPositions(player), 2), 2);

        ItemQuickStacker item = (ItemQuickStacker) stackerStack.getItem();
        long slotMask = getEnabledSlotsMask(stackerStack);
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        for (BlockPos pos : getPositions(player))
        {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP) == true)
            {
                IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

                if (inv != null)
                {
                    Result result = item.quickStackItems(playerInv, inv, slotMask, player.isSneaking() == false);

                    if (result != Result.MOVED_NONE)
                    {
                        player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
                        Effects.spawnParticlesFromServer(world.provider.getDimension(), pos, EnumParticleTypes.VILLAGER_HAPPY);
                    }

                    if (result == Result.MOVED_ALL)
                    {
                        break;
                    }
                }
            }
        }
    }

    public static List<BlockPos> getPositions(EntityPlayer player)
    {
        List<BlockPos> positions = new ArrayList<BlockPos>();

        PositionUtils.getPositionsInBoxSpiralingOutwards(positions, 3, 4, (int)(player.posY + 1.0d), (int)player.posX, (int)player.posZ);

        return positions;
    }

    @Override
    public void doUnselectedKeyAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Re-fetch the item to check if it's enabled
        stack = getEnabledItem(player);

        if (stack != null)
        {
            ((ItemQuickStacker)stack.getItem()).doKeyBindingAction(player, stack, key);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Just Toggle mode or Shift + Toggle mode: Fire the swapping action
        if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            quickStackItems(player);
        }
        // Alt + Shift + Toggle mode: Toggle the locked mode
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == true)
        {
            NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED);
        }
        // Ctrl + Toggle mode: Cycle the slot mask preset
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1,
                    ReferenceKeys.keypressActionIsReversed(key));
        }
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerQuickStacker)
        {
            ItemStack stack = ((ContainerQuickStacker)player.openContainer).getContainerItem();
            if (stack != null && stack.getItem() == EnderUtilitiesItems.quickStacker)
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

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "locked=false"),
                new ModelResourceLocation(rl, "locked=true")
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;
        return new ModelResourceLocation(rl, "locked=" + NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED));
    }
}
