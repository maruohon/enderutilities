package fi.dy.masa.enderutilities.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.inventory.container.ContainerNullifier;
import fi.dy.masa.enderutilities.inventory.item.InventoryItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemNullifier extends ItemEnderUtilities implements IKeyBound
{
    public static final String TAG_NAME_CONTAINER = "Nullifier";
    public static final String TAG_NAME_DISABLED = "Disabled";
    public static final String TAG_NAME_SLOT_SELECTION = "Slot";
    public static final int GUI_ACTION_SELECT_SLOT      = 0;
    public static final int GUI_ACTION_TOGGLE_DISABLED  = 1;
    public static final int NUM_SLOTS = 9;
    public static final int MAX_STACK_SIZE = 1024;

    public ItemNullifier()
    {
        super();
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setHasSubtypes(false);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_NULLIFIER);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        ItemStack useStack = this.getItemForUse(stack, player);

        if (useStack.isEmpty() == false && world.isBlockModifiable(player, pos.offset(facing)))
        {
            EntityUtils.setHeldItemWithoutEquipSound(player, hand, useStack);
            EnumActionResult result = useStack.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

            if (useStack.isEmpty() == false)
            {
                tryInsertItemsToNullifier(useStack, stack, player);
            }

            EntityUtils.setHeldItemWithoutEquipSound(player, hand, stack);

            return result;
        }

        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    public static boolean isNullifierEnabled(ItemStack stack)
    {
        return NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_DISABLED) == false;
    }

    private ItemStack getItemForUse(ItemStack stackNullifier, EntityPlayer player)
    {
        // Create the inventory here by pretending to be on the server side,
        // so that the readFromContainerItemStack() call actually reads the items on the client side too.
        // This is (mostly only?) required to get the block placing sounds to work on the client.
        // Ie. in other words, to get the correct item to use also on the client side.
        ItemHandlerNullifier inv = createInventoryForItem(stackNullifier, false);

        int slot = MathHelper.clamp(NBTUtils.getByte(stackNullifier, TAG_NAME_CONTAINER, TAG_NAME_SLOT_SELECTION), 0, inv.getSlots() - 1);
        boolean simulate = player.getEntityWorld().isRemote;
        return inv.extractItem(slot, 1, simulate);
    }

    public static ItemStack getSelectedStack(ItemStack stackNullifier)
    {
        // Create the inventory here by pretending to be on the server side,
        // so that the readFromContainerItemStack() call actually reads the items on the client side too.
        // This is (mostly only?) required to get the block placing sounds to work on the client.
        // Ie. in other words, to get the correct item to use also on the client side.
        ItemHandlerNullifier inv = createInventoryForItem(stackNullifier, false);

        int slot = MathHelper.clamp(NBTUtils.getByte(stackNullifier, TAG_NAME_CONTAINER, TAG_NAME_SLOT_SELECTION), 0, inv.getSlots() - 1);
        return inv.getStackInSlot(slot);
    }

    private static ItemHandlerNullifier getInventoryForItem(ItemStack stackNullifier, EntityPlayer player)
    {
        ItemHandlerNullifier inv = null;

        // If this bag is currently open, then use that inventory instead of creating a new one,
        // otherwise the open GUI/inventory will overwrite the changes from the picked up items.
        if (player.openContainer instanceof ContainerNullifier &&
            ((ContainerNullifier) player.openContainer).getContainerItem() == stackNullifier)
        {
            inv = ((ContainerNullifier) player.openContainer).inventoryItem;
        }
        else
        {
            inv = createInventoryForItem(stackNullifier, player.getEntityWorld().isRemote);
        }

        if (inv.isAccessibleBy(player) == false)
        {
            return null;
        }

        return inv;
    }

    public static ItemHandlerNullifier createInventoryForItem(ItemStack stack, boolean isRemote)
    {
        ItemHandlerNullifier inv = new ItemHandlerNullifier(stack, NUM_SLOTS, MAX_STACK_SIZE, true, isRemote);
        inv.readFromContainerItemStack();
        return inv;
    }

    /**
     * Tries to first fill the matching stacks in the player's inventory,
     * and then depending on the bag's mode, tries to add the remaining items
     * to the bag's inventory.
     * @param event
     * @return true if all items were handled and further processing of the event should not occur
     */
    public static boolean onEntityItemPickupEvent(EntityItemPickupEvent event)
    {
        EntityItem entityItem = event.getItem();
        ItemStack stackItems = entityItem.getItem();
        EntityPlayer player = event.getEntityPlayer();

        if (player.getEntityWorld().isRemote || entityItem.isDead || stackItems.isEmpty())
        {
            return true;
        }

        int origStackSize = stackItems.getCount();
        boolean ret = false;

        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        // Not all the items could fit into existing stacks in the player's inventory, move them directly to the nullifier
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.NULLIFIER);

        for (int slot : slots)
        {
            ItemStack nullifierStack = playerInv.getStackInSlot(slot);

            // Nullifier is not disabled
            if (nullifierStack.isEmpty() == false && isNullifierEnabled(nullifierStack))
            {
                stackItems = handleItems(stackItems, nullifierStack, player);

                if (stackItems.isEmpty())
                {
                    entityItem.setDead();
                    FMLCommonHandler.instance().firePlayerItemPickupEvent(player, entityItem);
                    player.onItemPickup(entityItem, origStackSize);
                    event.setCanceled(true);
                    ret = true;
                    break;
                }
            }
        }

        // Not everything was handled, update the stack
        if (entityItem.isDead == false && stackItems.getCount() != origStackSize)
        {
            entityItem.setItem(stackItems);
        }

        // At least some items were picked up
        if (entityItem.isSilent() == false && (entityItem.isDead || stackItems.getCount() != origStackSize))
        {
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER,
                    0.2F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        return ret;
    }

    /**
     * Tries to first fill the matching stacks in the player's inventory,
     * and then depending on the bag's mode, tries to add the remaining items
     * to the bag's inventory.
     * @param event
     * @return true if all items were handled and further processing of the event should not occur
     */
    public static boolean onItemPickupEvent(PlayerItemPickupEvent event)
    {
        if (event.getEntityPlayer().getEntityWorld().isRemote)
        {
            return false;
        }

        boolean pickedUp = false;
        EntityPlayer player = event.getEntityPlayer();
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        List<Integer> nullifierSlots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.NULLIFIER);
        Iterator<ItemStack> iter = event.drops.iterator();

        while (iter.hasNext())
        {
            ItemStack stack = iter.next();

            if (stack.isEmpty())
            {
                iter.remove();
                continue;
            }

            // Not all the items could fit into existing stacks in the player's inventory, move them directly to the nullifier
            for (int slot : nullifierSlots)
            {
                ItemStack nullifierStack = playerInv.getStackInSlot(slot);

                // Nullifier is not disabled
                if (nullifierStack.isEmpty() == false && isNullifierEnabled(nullifierStack))
                {
                    ItemStack stackOrig = stack;
                    stack = handleItems(stack, nullifierStack, player);

                    if (stack.isEmpty())
                    {
                        iter.remove();
                        pickedUp = true;
                    }
                    else if (stackOrig.getCount() != stack.getCount())
                    {
                        stackOrig.setCount(stack.getCount());
                        pickedUp = true;
                    }
                }
            }
        }

        // At least some items were picked up
        if (pickedUp)
        {
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.2F,
                    ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        if (event.drops.isEmpty())
        {
            event.setCanceled(true);
            return true;
        }

        return false;
    }

    private static ItemStack handleItems(ItemStack itemsIn, ItemStack nullifierStack, EntityPlayer player)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        // First try to fill all existing stacks in the player's inventory
        itemsIn = InventoryUtils.tryInsertItemStackToExistingStacksInInventory(playerInv, itemsIn);

        if (itemsIn.isEmpty() == false)
        {
            itemsIn = tryInsertItemsToNullifier(itemsIn, nullifierStack, player);
        }

        return itemsIn;
    }

    private static ItemStack tryInsertItemsToNullifier(ItemStack itemsIn, ItemStack nullifierStack, EntityPlayer player)
    {
        ItemHandlerNullifier nullifierInv = getInventoryForItem(nullifierStack, player);

        if (nullifierInv != null)
        {
            itemsIn = InventoryUtils.tryInsertItemStackToExistingStacksInInventory(nullifierInv, itemsIn);

            // Couldn't insert all items, check if there are matching items in the nullifier
            // and if so, then we just delete the excess items that didn't fit.
            if (itemsIn.isEmpty() == false && InventoryUtils.getSlotOfFirstMatchingItemStack(nullifierInv, itemsIn) != -1)
            {
                return ItemStack.EMPTY;
            }
        }

        return itemsIn;
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerNullifier)
        {
            ItemStack stack = ((ContainerNullifier) player.openContainer).getContainerItem();

            if (stack.isEmpty() == false)
            {
                if (action == GUI_ACTION_SELECT_SLOT)
                {
                    NBTUtils.setByte(stack, TAG_NAME_CONTAINER, TAG_NAME_SLOT_SELECTION, (byte) MathHelper.clamp(element, 0, NUM_SLOTS - 1));
                }
                else if (action == GUI_ACTION_TOGGLE_DISABLED)
                {
                    NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_DISABLED);
                }
            }
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode: Open the GUI
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            openGui(stack, player);
        }
        // Shift + Toggle: Toggle disabled state
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_DISABLED);
        }
        else if (EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL) || EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL))
        {
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_SLOT_SELECTION, NUM_SLOTS - 1,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String name = super.getItemStackDisplayName(stack);
        int slot = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_SLOT_SELECTION) + 1;
        ItemStack selectedStack = getSelectedStack(stack);

        if (selectedStack.isEmpty() == false)
        {
            return name + " - " + slot + " / " + NUM_SLOTS + " - " + selectedStack.getDisplayName();
        }
        else
        {
            return name + " - " + slot + " / " + NUM_SLOTS;
        }
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            return;
        }

        String preBlue = TextFormatting.BLUE.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String preRed = TextFormatting.RED.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
        String str;

        if (isNullifierEnabled(stack))
        {
            str = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
        }

        list.add(I18n.format("enderutilities.tooltip.item.enabled") + ": " + str);

        int slot = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_SLOT_SELECTION) + 1;
        list.add(I18n.format("enderutilities.tooltip.item.selected") + ": " + preBlue + slot + " / " + NUM_SLOTS + rst);

        ArrayList<String> lines = new ArrayList<String>();
        int itemCount = UtilItemModular.getFormattedItemListFromContainerItem(stack, lines, 20);

        if (lines.size() > 0)
        {
            NBTTagList tagList = NBTUtils.getStoredItemsList(stack, false);
            int stackCount = tagList != null ? tagList.tagCount() : 0;
            list.add(I18n.format("enderutilities.tooltip.item.memorycard.items.stackcount", stackCount, itemCount));
            list.addAll(lines);
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.memorycard.noitems"));
        }
    }

    public static void openGui(ItemStack stack, EntityPlayer player)
    {
        // These two lines are to fix the UUID being missing the first time the GUI opens,
        // if the item is grabbed from the creative inventory or from JEI or from /give
        NBTUtils.getUUIDFromItemStack(stack, "UUID", true);
        player.openContainer.detectAndSendChanges();

        player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_NULLIFIER, player.getEntityWorld(), 0, 0, 0);
    }

    public static class ItemHandlerNullifier extends InventoryItem
    {
        public ItemHandlerNullifier(ItemStack containerStack, int invSize, int stackLimit,
                boolean allowCustomStackSizes, boolean isRemote)
        {
            super(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            // Only allow in stackable, non-damageable items without NBT, so that there hopefully
            // won't be many issues with item usage not consuming the item or changing it...
            return stack.isEmpty() == false &&
                   stack.getMaxStackSize() > 1 &&
                   stack.isItemStackDamageable() == false &&
                   stack.getTagCompound() == null;
        }
    }
}
