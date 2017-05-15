package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSize;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.container.base.SlotRange;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class InventoryUtils
{
    public static final int SLOT_ITER_LIMIT = 256;
    public static final ItemStackHandlerBasic NULL_INV = new ItemStackHandlerBasic(0);

    public static int calcRedstoneFromInventory(IItemHandler inv)
    {
        int slots = inv.getSlots();
        int items = 0;
        int capacity = 0;

        for (int slot = 0; slot < slots; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if ((inv instanceof IItemHandlerSize) && stack.isEmpty() == false)
            {
                capacity += ((IItemHandlerSize)inv).getItemStackLimit(slot, stack);
            }
            else
            {
                capacity += inv.getSlotLimit(slot);
            }

            if (stack.isEmpty() == false)
            {
                items += stack.stackSize;
            }
        }

        if (capacity > 0)
        {
            int strength = (14 * items) / capacity;

            // Emit a signal strength of 1 as soon as there is one item in the inventory
            if (items > 0)
            {
                strength += 1;
            }

            return strength;
        }

        return 0;
    }

    /**
     * Drops all the ItemStacks from the given inventory into the world as EntityItems
     * @param world
     * @param pos
     * @param inv
     */
    public static void dropInventoryContentsInWorld(World world, BlockPos pos, IItemHandler inv)
    {
        int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack != null)
            {
                EntityUtils.dropItemStacksInWorld(world, pos, stack, -1, true);
            }
        }
    }

    /**
     * Tries to move all items from the inventory invSrc into invDst.
     */
    public static InvResult tryMoveAllItems(IItemHandler invSrc, IItemHandler invDst)
    {
        return tryMoveAllItemsWithinSlotRange(invSrc, invDst, new SlotRange(invSrc), new SlotRange(invDst));
    }

    /**
     * Tries to move all items from the inventory invSrc into invDst within the provided slot range.
     */
    public static InvResult tryMoveAllItemsWithinSlotRange(IItemHandler invSrc, IItemHandler invDst, SlotRange slotsSrc, SlotRange slotsDst)
    {
        boolean movedAll = true;
        boolean movedSome = false;

        int lastSlot = Math.min(slotsSrc.lastInc, invSrc.getSlots() - 1);

        for (int slot = slotsSrc.first; slot <= lastSlot; slot++)
        {
            ItemStack stack;

            int limit = SLOT_ITER_LIMIT;

            while (limit-- > 0)
            {
                stack = invSrc.extractItem(slot, 64, false);

                if (stack == null)
                {
                    break;
                }

                int origSize = stack.stackSize;

                stack = tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, slotsDst);

                if (stack == null || stack.stackSize != origSize)
                {
                    movedSome = true;
                }

                // Can't insert anymore items
                if (stack != null)
                {
                    // Put the rest of the items back to the source inventory
                    invSrc.insertItem(slot, stack, false);
                    movedAll = false;
                    break;
                }
            }
        }

        return movedAll ? InvResult.MOVED_ALL : (movedSome ? InvResult.MOVED_SOME : InvResult.MOVED_NOTHING);
    }

    /**
     * Tries to move matching/existing items from the inventory invSrc into invDst.
     */
    public static InvResult tryMoveMatchingItems(IItemHandler invSrc, IItemHandler invDst)
    {
        return tryMoveMatchingItemsWithinSlotRange(invSrc, invDst, new SlotRange(invSrc), new SlotRange(invDst));
    }

    /**
     * Tries to move matching/existing items from the inventory invSrc into invDst within the provided slot range.
     */
    public static InvResult tryMoveMatchingItemsWithinSlotRange(IItemHandler invSrc, IItemHandler invDst, SlotRange slotsSrc, SlotRange slotsDst)
    {
        boolean movedAll = true;
        boolean movedSome = false;
        InvResult result = InvResult.MOVED_NOTHING;

        int lastSlot = Math.min(slotsSrc.lastInc, invSrc.getSlots() - 1);

        for (int slot = slotsSrc.first; slot <= lastSlot; slot++)
        {
            ItemStack stack = invSrc.getStackInSlot(slot);

            if (stack != null)
            {
                if (getSlotOfFirstMatchingItemStackWithinSlotRange(invDst, stack, slotsDst) != -1)
                {
                    result = tryMoveAllItemsWithinSlotRange(invSrc, invDst, new SlotRange(slot, 1), slotsDst);
                }

                if (result != InvResult.MOVED_NOTHING)
                {
                    movedSome = true;
                }
                else
                {
                    movedAll = false;
                }
            }
        }

        return movedAll ? InvResult.MOVED_ALL : (movedSome ? InvResult.MOVED_SOME : InvResult.MOVED_NOTHING);
    }

    /**
     * Tries to fill all the existing stacks in invDst from invSrc.
     */
    public static InvResult fillStacksOfMatchingItems(IItemHandler invSrc, IItemHandler invDst)
    {
        return fillStacksOfMatchingItemsWithinSlotRange(invSrc, invDst, new SlotRange(invSrc), new SlotRange(invDst));
    }

    /**
     * Tries to fill all the existing stacks in invDst from invSrc within the provided slot ranges.
     */
    public static InvResult fillStacksOfMatchingItemsWithinSlotRange(IItemHandler invSrc, IItemHandler invDst, SlotRange slotsSrc, SlotRange slotsDst)
    {
        boolean movedAll = true;
        boolean movedSome = false;
        InvResult result = InvResult.MOVED_NOTHING;

        int lastSlot = Math.min(slotsSrc.lastInc, invSrc.getSlots() - 1);

        for (int slot = slotsSrc.first; slot <= lastSlot; slot++)
        {
            ItemStack stack = invSrc.getStackInSlot(slot);

            if (stack != null)
            {
                List<Integer> matchingSlots = getSlotNumbersOfMatchingStacksWithinSlotRange(invDst, stack, slotsDst);

                for (int dstSlot : matchingSlots)
                {
                    //if (dstSlot >= slotsDst.first && dstSlot <= slotsDst.lastInc)
                    {
                        result = tryMoveAllItemsWithinSlotRange(invSrc, invDst, new SlotRange(slot, 1), new SlotRange(dstSlot, 1));
                    }

                    if (result != InvResult.MOVED_NOTHING)
                    {
                        movedSome = true;
                    }
                    else
                    {
                        movedAll = false;
                    }
                }
            }
        }

        return movedAll ? InvResult.MOVED_ALL : (movedSome ? InvResult.MOVED_SOME : InvResult.MOVED_NOTHING);
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory.
     * The return value is a stack of the remaining items that couldn't be inserted.
     * If all items were successfully inserted, then null is returned.
     */
    public static ItemStack tryInsertItemStackToInventory(IItemHandler inv, @Nonnull ItemStack stackIn)
    {
        return tryInsertItemStackToInventoryWithinSlotRange(inv, stackIn, new SlotRange(inv));
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory, inside the given slot range.
     * The return value is a stack of the remaining items that couldn't be inserted.
     * If all items were successfully inserted, then null is returned.
     */
    public static ItemStack tryInsertItemStackToInventoryWithinSlotRange(IItemHandler inv, @Nonnull ItemStack stackIn, SlotRange slotRange)
    {
        int max = Math.min(slotRange.lastInc, inv.getSlots() - 1);

        // First try to add to existing stacks
        for (int slot = slotRange.first; slot <= max; slot++)
        {
            if (inv.getStackInSlot(slot) != null)
            {
                stackIn = inv.insertItem(slot, stackIn, false);

                if (stackIn == null)
                {
                    return null;
                }
            }
        }

        // Second round, try to add to any slot
        for (int slot = slotRange.first; slot <= max; slot++)
        {
            stackIn = inv.insertItem(slot, stackIn, false);

            if (stackIn == null)
            {
                return null;
            }
        }

        return stackIn;
    }

    /**
     * Try insert the items in <b>stackIn</b> into existing stacks with identical items in the inventory <b>inv</b>.
     * @return null if all items were successfully inserted, otherwise the stack containing the remaining items
     */
    public static ItemStack tryInsertItemStackToExistingStacksInInventory(IItemHandler inv, @Nonnull ItemStack stackIn)
    {
        List<Integer> slots = getSlotNumbersOfMatchingStacks(inv, stackIn);

        for (int slot : slots)
        {
            stackIn = inv.insertItem(slot, stackIn, false);

            // If the entire (remaining) stack was inserted to the current slot, then we are done
            if (stackIn == null)
            {
                return null;
            }
        }

        return stackIn;
    }

    /**
     * Checks if the given ItemStacks have the same item, damage and NBT. Ignores stack sizes.
     * Can be given null ItemStacks as input.
     * @param stack1
     * @param stack2
     * @return Returns true if the ItemStacks have the same item, damage and NBT tags.
     */
    public static boolean areItemStacksEqual(@Nullable ItemStack stack1, @Nullable ItemStack stack2)
    {
        if (stack1 == null || stack2 == null)
        {
            return stack1 == stack2;
        }

        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    /**
     * Returns the slot number of the first empty slot in the given inventory, or -1 if there are no empty slots.
     */
    public static int getFirstEmptySlot(IItemHandler inv)
    {
        return getSlotOfFirstMatchingItemStack(inv, null);
    }

    /**
     * Returns the slot number of the last empty slot in the given inventory, or -1 if there are no empty slots.
     */
    public static int getLastEmptySlot(IItemHandler inv)
    {
        return getSlotOfLastMatchingItemStack(inv, null);
    }

    /**
     * Returns the slot number of the first non-empty slot in the given inventory, or -1 if there are no items.
     */
    public static int getFirstNonEmptySlot(IItemHandler inv)
    {
        for (int i = 0; i < inv.getSlots(); i++)
        {
            if (inv.getStackInSlot(i) != null)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns an ItemStack of up to maxAmount items from the first slot possible to extract from.
     */
    public static ItemStack getItemsFromFirstNonEmptySlot(IItemHandler inv, int maxAmount, boolean simulate)
    {
        ItemStack stack = null;

        for (int i = 0; i < inv.getSlots(); i++)
        {
            stack = inv.extractItem(i, maxAmount, simulate);

            if (stack != null)
            {
                return stack;
            }
        }

        return null;
    }

    /**
     * Get the slot number of the first slot containing a matching item, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfFirstMatchingItem(IItemHandler inv, Item item)
    {
        return getSlotOfFirstMatchingItem(inv, item, OreDictionary.WILDCARD_VALUE);
    }

    /**
     * Get the slot number of the first slot containing a matching item and damage value.
     * If <b>damage</b> is OreDictionary.WILDCARD_VALUE, then the item damage is ignored.
     * @return The slot number of the first slot with a matching item and damage value, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfFirstMatchingItem(IItemHandler inv, Item item, int meta)
    {
        int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack != null && stack.getItem() == item && (stack.getMetadata() == meta || meta == OreDictionary.WILDCARD_VALUE))
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns the first ItemStack from the inventory that has the given Item in it, or null.
     */
    public static ItemStack getFirstMatchingItem(IItemHandler inv, Item item)
    {
        int slot = getSlotOfFirstMatchingItem(inv, item);
        return slot != -1 ? inv.getStackInSlot(slot) : null;
    }

    /**
     * Returns the first matching item from the player's inventory, or an empty stack.
     */
    public static ItemStack getFirstItemOfType(EntityPlayer player, Class<?> clazz)
    {
        IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        final int numSlots = inv.getSlots();

        for (int slot = 0; slot < numSlots; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack.isEmpty() == false && clazz.isAssignableFrom(stack.getItem().getClass()))
            {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Returns the first matching item from the player's inventory, or an empty stack.
     */
    public static ItemStack getFirstItemOfType(EntityPlayer player, Item item)
    {
        IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        final int numSlots = inv.getSlots();

        for (int slot = 0; slot < numSlots; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack.isEmpty() == false && stack.getItem() == item)
            {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Get the slot number of the last slot containing a matching item, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfLastMatchingItem(IItemHandler inv, Item item)
    {
        return getSlotOfLastMatchingItem(inv, item, OreDictionary.WILDCARD_VALUE);
    }

    /**
     * Get the slot number of the last slot containing a matching item and damage value.
     * If <b>damage</b> is OreDictionary.WILDCARD_VALUE, then the item damage is ignored.
     * @param inv
     * @param item
     * @return The slot number of the last slot with a matching item and damage value, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfLastMatchingItem(IItemHandler inv, Item item, int meta)
    {
        for (int slot = inv.getSlots() - 1; slot >= 0; slot--)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack != null && stack.getItem() == item && (stack.getMetadata() == meta || meta == OreDictionary.WILDCARD_VALUE))
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the first slot containing a matching ItemStack (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfFirstMatchingItemStack(IItemHandler inv, @Nullable ItemStack stackIn)
    {
        return getSlotOfFirstMatchingItemStackWithinSlotRange(inv, stackIn, new SlotRange(inv));
    }

    /**
     * Get the slot number of the first slot containing a matching ItemStack (including NBT, ignoring stackSize) within the given slot range.
     * Note: stackIn can be null.
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfFirstMatchingItemStackWithinSlotRange(IItemHandler inv, @Nullable ItemStack stackIn, SlotRange slotRange)
    {
        int max = Math.min(inv.getSlots() - 1, slotRange.lastInc);

        for (int slot = slotRange.first; slot <= max; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (areItemStacksEqual(stack, stackIn))
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the last slot containing a matching ItemStack (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @param inv
     * @param item
     * @return The slot number of the last slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfLastMatchingItemStack(IItemHandler inv, @Nullable ItemStack stackIn)
    {
        return getSlotOfLastMatchingItemStackWithinSlotRange(inv, stackIn, new SlotRange(inv));
    }

    /**
     * Get the slot number of the last slot containing a matching ItemStack (including NBT, ignoring stackSize) within the given slot range.
     * Note: stackIn can be null.
     * @return The slot number of the last slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfLastMatchingItemStackWithinSlotRange(IItemHandler inv, @Nullable ItemStack stackIn, SlotRange slotRange)
    {
        int max = Math.min(inv.getSlots() - 1, slotRange.lastInc);

        for (int slot = max; slot >= slotRange.first; slot--)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (areItemStacksEqual(stack, stackIn))
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Get all the slot numbers that have matching items in the given inventory.
     * @param inv
     * @param item
     * @return an ArrayList containing the slot numbers of the slots with matching items
     */
    public static List<Integer> getSlotNumbersOfMatchingItems(IItemHandler inv, Item item)
    {
        return getSlotNumbersOfMatchingItems(inv, item, OreDictionary.WILDCARD_VALUE);
    }

    /**
     * Get all the slot numbers that have matching items in the given inventory.
     * If <b>damage</b> is OreDictionary.WILDCARD_VALUE, then the item damage is ignored.
     * @param inv
     * @param item
     * @return an ArrayList containing the slot numbers of the slots with matching items
     */
    public static List<Integer> getSlotNumbersOfMatchingItems(IItemHandler inv, Item item, int meta)
    {
        List<Integer> slots = new ArrayList<Integer>();
        int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; ++slot)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack != null && stack.getItem() == item && (stack.getMetadata() == meta || meta == OreDictionary.WILDCARD_VALUE))
            {
                slots.add(Integer.valueOf(slot));
            }
        }

        return slots;
    }

    /**
     * Get all the slot numbers that have matching ItemStacks (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @return an ArrayList containing the slot numbers of the slots with matching ItemStacks
     */
    public static List<Integer> getSlotNumbersOfMatchingStacks(IItemHandler inv, @Nonnull ItemStack stackIn)
    {
        return getSlotNumbersOfMatchingStacksWithinSlotRange(inv, stackIn, new SlotRange(inv));
    }

    /**
     * Get all the slot numbers that have matching ItemStacks (including NBT, ignoring stackSize) within the given slot range.
     * Note: stackIn can be null.
     * @return an ArrayList containing the slot numbers of the slots with matching ItemStacks
     */
    public static List<Integer> getSlotNumbersOfMatchingStacksWithinSlotRange(IItemHandler inv, @Nonnull ItemStack stackIn, SlotRange slotRange)
    {
        List<Integer> slots = new ArrayList<Integer>();
        int max = Math.min(inv.getSlots() - 1, slotRange.lastInc);

        for (int slot = slotRange.first; slot <= max; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (areItemStacksEqual(stack, stackIn))
            {
                slots.add(Integer.valueOf(slot));
            }
        }

        return slots;
    }

    public static List<Integer> getSlotNumbersOfMatchingStacks(IItemHandler inv, @Nonnull ItemStack stackTemplate, boolean useOreDict)
    {
        List<Integer> slots = new ArrayList<Integer>();

        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack == null)
            {
                continue;
            }

            if (areItemStacksEqual(stack, stackTemplate) || (useOreDict && areItemStacksOreDictMatch(stack, stackTemplate)))
            {
                slots.add(Integer.valueOf(slot));
            }
        }

        return slots;
    }

    /**
     * Extracts up to <b>amount</b> of <b>item</b> from the first slot in the inventory that
     * has <b>item</b> in it and returns them. Does not try to fill the stack up to <b>amount</b>!
     */
    public static ItemStack extractItems(IItemHandler inv, Item item, int amount)
    {
        int slot = getSlotOfFirstMatchingItem(inv, item);

        if (slot >= 0)
        {
            return inv.extractItem(slot, amount, false);
        }

        return null;
    }

    /**
     * Extracts up to <b>amount</b> items from the first found slot with items matching <b>templateStack</b>.
     * Does not try to fill the stack up to amount if the first found slot has less than <b>amount</b> items!
     */
    public static ItemStack extractMatchingItems(IItemHandler inv, @Nonnull ItemStack templateStack, int amount, boolean simulate)
    {
        int slot = getSlotOfFirstMatchingItemStack(inv, templateStack);

        if (slot >= 0)
        {
            return inv.extractItem(slot, amount, simulate);
        }

        return null;
    }

    /**
     * Get the ItemStack that has the given UUID stored in its NBT. If <b>containerTagName</b>
     * is not null, then the UUID is read from a compound tag by that name.
     */
    public static ItemStack getItemStackByUUID(IItemHandler inv, UUID uuid, @Nullable String containerTagName)
    {
        int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack != null && uuid.equals(NBTUtils.getUUIDFromItemStack(stack, containerTagName, false)))
            {
                return stack;
            }
        }

        return null;
    }

    /**
     * Extract items from the given slot until the resulting stack's stackSize equals amount
     */
    public static ItemStack extractItemsFromSlot(IItemHandler inv, int slot, int amount)
    {
        ItemStack stackExtract = inv.extractItem(slot, amount, false);

        if (stackExtract == null)
        {
            return stackExtract;
        }

        if ((stackExtract.getMaxStackSize() * SLOT_ITER_LIMIT) < amount && inv instanceof IItemHandlerModifiable)
        {
            amount -= stackExtract.stackSize;
            ItemStack stackSlot = inv.getStackInSlot(slot);

            if (stackSlot != null)
            {
                if (stackSlot.stackSize <= amount)
                {
                    stackExtract.stackSize += stackSlot.stackSize;
                    ((IItemHandlerModifiable) inv).setStackInSlot(slot, null);
                }
                else
                {
                    stackExtract.stackSize += amount;
                    stackSlot = stackSlot.copy();
                    stackSlot.stackSize -= amount;
                    ((IItemHandlerModifiable) inv).setStackInSlot(slot, stackSlot);
                }
            }

            return stackExtract;
        }

        int loops = 0;

        while (stackExtract.stackSize < amount && loops < SLOT_ITER_LIMIT)
        {
            ItemStack stackTmp = inv.extractItem(slot, amount - stackExtract.stackSize, false);

            if (stackTmp == null)
            {
                break;
            }

            stackExtract.stackSize += stackTmp.stackSize;
            loops++;
        }

        //System.out.printf("extractItemsFromSlot(): slot: %d, requested amount: %d, loops %d, extracted: %s\n", slot, amount, loops, stack);
        return stackExtract;
    }

    /**
     * Collects items from the inventory that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. If <b>reverse</b> is true, then the items are collected
     * starting from the end of the given inventory.
     * If no matching items are found, null is returned.
     */
    public static ItemStack collectItemsFromInventory(IItemHandler inv, @Nonnull ItemStack stackTemplate, int maxAmount, boolean reverse)
    {
        return collectItemsFromInventory(inv, stackTemplate, maxAmount, reverse, false);
    }

    /**
     * Collects items from the inventory that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. If <b>reverse</b> is true, then the items are collected
     * starting from the end of the given inventory.
     * If no matching items are found, null is returned.
     */
    public static ItemStack collectItemsFromInventory(IItemHandler inv, @Nonnull ItemStack stackTemplate,
            int maxAmount, boolean reverse, boolean useOreDict)
    {
        return collectItemsFromInventoryFromSlotRange(inv, stackTemplate, new SlotRange(inv), maxAmount, reverse, useOreDict);
    }

    /**
     * Collects items from the inventory from within the given SlotRange that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. If <b>reverse</b> is true, then the items are collected
     * starting from the end of the given inventory.
     * If no matching items are found, null is returned.
     */
    public static ItemStack collectItemsFromInventoryFromSlotRange(IItemHandler inv, @Nonnull ItemStack stackTemplate,
            SlotRange range, int amount, boolean reverse, boolean useOreDict)
    {
        if (range.first >= inv.getSlots() || range.lastInc >= inv.getSlots())
        {
            return null;
        }

        int inc = reverse ? -1 : 1;
        int start = reverse ? range.lastInc : range.first;
        ItemStack stack = stackTemplate.copy();
        stack.stackSize = 0;
        //System.out.printf("amount: %d range: %s stack: %s start: %d inc: %d\n", amount, range, stack, start, inc);

        for (int slot = start; slot >= range.first && slot <= range.lastInc && stack.stackSize < amount; slot += inc)
        {
            ItemStack stackTmp = inv.getStackInSlot(slot);
            if (stackTmp == null)
            {
                continue;
            }

            if (areItemStacksEqual(stackTmp, stackTemplate))
            {
                stackTmp = extractItemsFromSlot(inv, slot, amount - stack.stackSize);
                //System.out.printf("extracted %s from slot %d\n", stackTmp, slot);

                if (stackTmp != null)
                {
                    stack.stackSize += stackTmp.stackSize;
                }
            }
            else if (useOreDict && areItemStacksOreDictMatch(stackTmp, stackTemplate))
            {
                // This is the first match, and since it's an OreDictionary match ie. different actual
                // item, we convert the stack to the matched item.
                if (stack.stackSize == 0)
                {
                    stack = stackTmp.copy();
                    stack.stackSize = 0;
                }

                stackTmp = extractItemsFromSlot(inv, slot, amount - stack.stackSize);

                if (stackTmp != null)
                {
                    stack.stackSize += stackTmp.stackSize;
                }
            }
        }

        return stack.stackSize > 0 ? stack : null;
    }

    /**
     * Collects one stack of items that are identical to stackTemplate, and fills that stack as full as possible
     * first from invTarget and if it still isn't full, then also from invStorage. All the remaining items
     * in invTarget that are identical to stackTemplate will be moved to the other inventory, invStorage.
     */
    public static ItemStack collectOneStackAndMoveOthers(IItemHandler invTarget, IItemHandler invStorage, @Nonnull ItemStack stackTemplate)
    {
        int maxStackSize = stackTemplate.getMaxStackSize();

        // Get our initial collected stack from the target inventory
        ItemStack stack = collectItemsFromInventory(invTarget, stackTemplate, maxStackSize, true);

        // Move all the remaining identical items to the storage inventory
        List<Integer> slots = getSlotNumbersOfMatchingStacks(invTarget, stackTemplate);

        for (int slot : slots)
        {
            ItemStack stackTmp;

            int limit = SLOT_ITER_LIMIT;

            while (limit-- > 0)
            {
                stackTmp = invTarget.extractItem(slot, maxStackSize, false);

                if (stackTmp == null)
                {
                    break;
                }

                stackTmp = tryInsertItemStackToInventory(invStorage, stackTmp);

                // Can't insert all of the items, return the rest
                if (stackTmp != null)
                {
                    invTarget.insertItem(slot, stackTmp, false);
                    break;
                }
            }
        }

        // If the initial collected stack wasn't full, try to fill it from the storage inventory
        if (stack != null && stack.stackSize < maxStackSize)
        {
            ItemStack stackTmp = collectItemsFromInventory(invStorage, stack, maxStackSize - stack.stackSize, true);

            if (stackTmp != null)
            {
                stack.stackSize += stackTmp.stackSize;
            }
        }

        return stack;
    }

    /**
     * Loops through the invTarget inventory and leaves one stack of every item type found
     * and moves the rest to invStorage. The stacks are also first collected from invTarget
     * and filled as full as possible and if it's still not full, then more items are moved from invStorage.
     * @param invTarget the target inventory that will be cleaned up and where the filled stacks are left in
     * @param invStorage the "external" inventory where the excess items are moved to
     * @param reverse set to true to start the looping from the end of invTarget and thus leave the last stack of each item
     */
    public static void leaveOneFullStackOfEveryItem(IItemHandler invTarget, IItemHandler invStorage, boolean reverse)
    {
        int inc = (reverse ? -1 : 1);
        int start = (reverse ? (invTarget.getSlots() - 1) : 0);

        for (int slot = start; slot >= 0 && slot < invTarget.getSlots(); slot += inc)
        {
            ItemStack stack = invTarget.getStackInSlot(slot);

            if (stack == null)
            {
                continue;
            }

            int maxSize = stack.getMaxStackSize();

            // Get all slots that have this item
            List<Integer> matchingSlots = getSlotNumbersOfMatchingStacks(invTarget, stack);

            if (matchingSlots.size() > 1)
            {
                for (int tmp : matchingSlots)
                {
                    if (tmp == slot)
                    {
                        continue;
                    }

                    // Move items from the other slots to the first slot as long as they can fit
                    int limit = SLOT_ITER_LIMIT;

                    do
                    {
                        stack = invTarget.extractItem(tmp, maxSize, false);
                        if (stack == null)
                        {
                            break;
                        }

                        stack = invTarget.insertItem(slot, stack, false);
                    } while (stack == null && --limit > 0);

                    // If there are items that didn't fit into the first slot, then move those to the other inventory
                    limit = SLOT_ITER_LIMIT;

                    while (stack != null && limit-- > 0)
                    {
                        stack = tryInsertItemStackToInventory(invStorage, stack);

                        // Couldn't move more items to the invStorage inventory, return the remaining stack to the original slot and bail out
                        if (stack != null)
                        {
                            invTarget.insertItem(tmp, stack, false);
                            return;
                        }

                        stack = invTarget.extractItem(tmp, maxSize, false);
                    }
                }
            }

            // All matching stacks inside the invTarget handled, now check if we still
            // need to re-stock more items into the first stack from invStorage.
            stack = invTarget.getStackInSlot(slot);

            if (stack != null)
            {
                maxSize = stack.getMaxStackSize();

                if (stack.stackSize < maxSize)
                {
                    ItemStack stackTmp = collectItemsFromInventory(invStorage, stack, maxSize - stack.stackSize, true);

                    if (stackTmp != null)
                    {
                        stackTmp = invTarget.insertItem(slot, stackTmp, false);

                        // If some of them didn't fit into the first slot after all, then return those
                        if (stackTmp != null)
                        {
                            tryInsertItemStackToInventory(invStorage, stackTmp);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if there is a matching ItemStack in the inventory inside the given slot range.
     */
    public static boolean matchingStackFoundInSlotRange(IItemHandler inv, SlotRange slotRange,
            @Nonnull ItemStack stackTemplate, boolean ignoreMeta, boolean ignoreNbt)
    {
        Item item = stackTemplate.getItem();
        int meta = stackTemplate.getMetadata();
        int lastSlot = Math.min(slotRange.lastInc, inv.getSlots() - 1);

        for (int slot = slotRange.first; slot <= lastSlot; slot++)
        {
            ItemStack stackTmp = inv.getStackInSlot(slot);
            if (stackTmp == null || stackTmp.getItem() != item)
            {
                continue;
            }

            if (ignoreMeta == false && (meta != OreDictionary.WILDCARD_VALUE && stackTmp.getMetadata() != meta))
            {
                continue;
            }

            if (ignoreNbt == false && ItemStack.areItemStackTagsEqual(stackTemplate, stackTmp) == false)
            {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * Checks if there is a matching ItemStack in the provided array of stacks
     */
    public static boolean matchingStackFoundOnList(NonNullList<ItemStack> list,
            @Nonnull ItemStack stackTemplate, boolean ignoreMeta, boolean ignoreNbt)
    {
        Item item = stackTemplate.getItem();
        int meta = stackTemplate.getMetadata();
        final int size = list.size();

        for (int i = 0; i < size; i++)
        {
            ItemStack stackTmp = list.get(i);

            if (stackTmp.isEmpty() || stackTmp.getItem() != item)
            {
                continue;
            }

            if (ignoreMeta == false && (meta != OreDictionary.WILDCARD_VALUE && stackTmp.getMetadata() != meta))
            {
                continue;
            }

            if (ignoreNbt == false && ItemStack.areItemStackTagsEqual(stackTemplate, stackTmp) == false)
            {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * @param inv
     * @return true if all the slots in the inventory are empty, ie. null
     */
    public static boolean isInventoryEmpty(IItemHandler inv)
    {
        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            if (inv.getStackInSlot(slot) != null)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the largest existing stack size from the inventory <b>inv</b>.
     * @param inv
     * @return largest existing stack size from the inventory, or -1 if all stacks are empty
     */
    public static int getLargestExistingStackSize(IItemHandler inv)
    {
        int largestSize = -1;

        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack != null && stack.stackSize > largestSize)
            {
                largestSize = stack.stackSize;
            }
        }

        return largestSize;
    }

    /**
     * Returns the minimum stack size from the inventory <b>inv</b> from
     * stacks that are not empty, or -1 if all stacks are empty.
     * @param inv
     * @return minimum stack size from the inventory, or -1 if all stacks are empty
     */
    public static int getMinNonEmptyStackSize(IItemHandler inv)
    {
        int minSize = -1;

        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack != null && (minSize < 0 || stack.stackSize < minSize))
            {
                minSize = stack.stackSize;
            }
        }

        return minSize;
    }

    /**
     * Checks if the ItemStack <b>stackTarget</b> is valid to be used as a substitution
     * for <b>stackReference</b> via the OreDictionary keys.
     * @param stackTarget
     * @param stackReference
     * @return
     */
    public static boolean areItemStacksOreDictMatch(@Nonnull ItemStack stackTarget, @Nonnull ItemStack stackReference)
    {
        int[] ids = OreDictionary.getOreIDs(stackReference);

        for (int id : ids)
        {
            List<ItemStack> oreStacks = OreDictionary.getOres(OreDictionary.getOreName(id), false);

            for (ItemStack oreStack : oreStacks)
            {
                if (OreDictionary.itemMatches(stackTarget, oreStack, false))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Counts the number of items in the inventory <b>inv</b> that are identical to <b>stackTemplate</b>.
     * If <b>useOreDict</b> is true, then Ore Dictionary matches are also accepted.
     */
    public static int getNumberOfMatchingItemsInInventory(IItemHandler inv, @Nonnull ItemStack stackTemplate, boolean useOreDict)
    {
        int found = 0;

        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            ItemStack stackTmp = inv.getStackInSlot(slot);

            if (stackTmp != null)
            {
                if (areItemStacksEqual(stackTmp, stackTemplate) || (useOreDict && areItemStacksOreDictMatch(stackTmp, stackTemplate)))
                {
                    found += stackTmp.stackSize;
                }
            }
        }

        return found;
    }

    /**
     * Checks if the given inventory <b>inv</b> has at least <b>amount</b> number of items
     * matching the item in <b>stackTemplate</b>.
     * If useOreDict is true, then any matches via OreDictionary are also accepted.
     */
    public static boolean checkInventoryHasItems(IItemHandler inv, @Nonnull ItemStack stackTemplate, int amount, boolean useOreDict)
    {
        int found = 0;

        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            ItemStack stackTmp = inv.getStackInSlot(slot);

            if (stackTmp != null)
            {
                if (areItemStacksEqual(stackTmp, stackTemplate) || (useOreDict && areItemStacksOreDictMatch(stackTmp, stackTemplate)))
                {
                    found += stackTmp.stackSize;
                }
            }

            if (found >= amount)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the inventory <b>invStorage</b> has all the items from the other inventory <b>invTemplate</b>
     * in at least the amountPerStack quantity per each stack from the template inventory.
     * If useOreDict is true, then any matches via OreDictionary are also accepted.
     */
    public static boolean checkInventoryHasAllItems(IItemHandler invStorage, IItemHandler invTemplate, int amountPerStack, boolean useOreDict)
    {
        Map<ItemType, Integer> quantities = new HashMap<ItemType, Integer>();

        // First get the sum of all the items required based on the template inventory
        for (int slot = 0; slot < invTemplate.getSlots(); slot++)
        {
            ItemStack stackTmp = invTemplate.getStackInSlot(slot);

            if (stackTmp != null)
            {
                ItemType item = new ItemType(stackTmp);
                Integer amount = quantities.get(item);
                amount = (amount != null) ? amount + amountPerStack : amountPerStack;
                quantities.put(item, Integer.valueOf(amount));
            }
        }

        // Then check if the storage inventory has the required amount of each of those items
        Set<ItemType> items = quantities.keySet();
        for (ItemType item : items)
        {
            Integer amount = quantities.get(item);

            if (amount != null)
            {
                if (checkInventoryHasItems(invStorage, item.getStack(), amount, useOreDict) == false)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns a map of how many slots contain the same item, for each item found in the inventory.
     */
    public static Map<ItemType, Integer> getSlotCountPerItem(IItemHandler inv)
    {
        Map<ItemType, Integer> slots = new HashMap<ItemType, Integer>();

        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            ItemStack stackTmp = inv.getStackInSlot(slot);

            if (stackTmp != null)
            {
                ItemType item = new ItemType(stackTmp);
                Integer count = slots.get(item);
                count = (count != null) ? count + 1 : 1;
                slots.put(item, Integer.valueOf(count));
            }
        }

        return slots;
    }

    /**
     * Creates a copy of the whole inventory and returns it in a new NonNullList.
     * @param inv
     * @return a NonNullList containing a copy of the entire inventory
     */
    public static NonNullList<ItemStack> createInventorySnapshot(IItemHandler inv)
    {
        final int slots = inv.getSlots();
        NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);

        for (int i = 0; i < slots; i++)
        {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.isEmpty() == false)
            {
                items.set(i, stack.copy());
            }
        }

        return items;
    }

    /**
     * Creates a copy of the non-empty stacks in the inventory and returns it in a new NonNullList.
     * @param inv
     * @return a NonNullList containing copies of the non-empty stacks
     */
    public static NonNullList<ItemStack> createInventorySnapshotOfNonEmptySlots(IItemHandler inv)
    {
        final int slots = inv.getSlots();
        NonNullList<ItemStack> items = NonNullList.create();

        for (int i = 0; i < slots; i++)
        {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.isEmpty() == false)
            {
                items.add(stack.copy());
            }
        }

        return items;
    }

    /**
     * If there are items in the invTarget inventory that do not match the template list,
     * then those are attempted to move to invStorage
     * @param invTarget
     * @param invStorage
     * @param template
     */
    public static void clearInventoryToMatchTemplate(IItemHandler invTarget, IItemHandler invStorage, NonNullList<ItemStack> template)
    {
        final int slots = Math.min(invTarget.getSlots(), template.size());
        SlotRange rangeStorage = new SlotRange(invStorage);

        for (int i = 0; i < slots; i++)
        {
            if (areItemStacksEqual(template.get(i), invTarget.getStackInSlot(i)) == false)
            {
                tryMoveAllItemsWithinSlotRange(invTarget, invStorage, new SlotRange(i, 1), rangeStorage);
            }
        }
    }

    /**
     * Adds amountPerStack items to all the stacks in invTarget based on the template inventory in <b>template</b>.
     * If the existing stack doesn't match the template, then nothing will be added to that stack.
     * If the existing stack is empty, then it will be set to a new stack based on the template.
     * All the items are taken from the inventory <b>invStorage</b>.
     * If emptySlotsOnly is true, then only slots that are empty in the target inventory will be re-stocked.
     * If useOreDict is true, then any matches via OreDictionary are also accepted.
     * @param invTarget
     * @param invStorage
     * @param template
     * @param amountPerStack
     * @param emptySlotsOnly
     * @param useOreDict
     * @return true if ALL the items from the template inventory and in the quantity amountPerStack were successfully added
     */
    public static boolean restockInventoryBasedOnTemplate(IItemHandler invTarget, IItemHandler invStorage,
            NonNullList<ItemStack> template, int amountPerStack, boolean emptySlotsOnly, boolean useOreDict)
    {
        int i = 0;
        int amount = 0;
        boolean allSuccess = true;

        for (i = 0; i < template.size() && i < invTarget.getSlots(); i++)
        {
            ItemStack stackTemplate = template.get(i);

            if (stackTemplate.isEmpty())
            {
                continue;
            }

            ItemStack stackExisting = invTarget.getStackInSlot(i);

            if (emptySlotsOnly && stackExisting.isEmpty() == false)
            {
                continue;
            }

            amount = Math.min(amountPerStack, stackTemplate.getMaxStackSize());

            // The existing stack doesn't match the template, skip it
            if (stackExisting.isEmpty() == false)
            {
                if ((useOreDict == false && areItemStacksEqual(stackExisting, stackTemplate) == false) ||
                    (useOreDict && areItemStacksOreDictMatch(stackExisting, stackTemplate) == false))
                {
                    allSuccess = false;
                    continue;
                }

                amount = Math.max(amount - stackExisting.getCount(), 0);
            }

            if (amount <= 0)
            {
                allSuccess = false;
                continue;
            }

            ItemStack stackNew = collectItemsFromInventory(invStorage, stackTemplate, amount, false, useOreDict);

            if (stackNew.isEmpty())
            {
                allSuccess = false;
                continue;
            }

            if (stackNew.getCount() < amount)
            {
                allSuccess = false;
            }

            // Used oreDict matches to collect the items, and they are not identical to the existing items
            // => we need to convert the new items to the existing item's type before they can be inserted
            if (useOreDict && stackExisting.isEmpty() == false && areItemStacksEqual(stackExisting, stackNew) == false)
            {
                int size = stackNew.getCount();
                stackNew = stackExisting.copy();
                stackNew.setCount(size);
            }

            // Try to insert the collected stack to the target slot
            stackNew = invTarget.insertItem(i, stackNew, false);

            // Failed to insert all the items, return them to the original inventory
            if (stackNew.isEmpty() == false)
            {
                tryInsertItemStackToInventory(invStorage, stackNew);
            }
        }

        return allSuccess;
    }

    public static void sortInventoryWithinRange(IItemHandlerModifiable inv, SlotRange range)
    {
        List<ItemTypeByName> blocks = new ArrayList<ItemTypeByName>();
        List<ItemTypeByName> items = new ArrayList<ItemTypeByName>();
        int last = Math.min(range.lastInc, inv.getSlots() - 1);

        // Get the different items that are present in the inventory
        for (int i = range.first; i <= last; i++)
        {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack != null)
            {
                ItemTypeByName type = new ItemTypeByName(stack);

                if (stack.getItem() instanceof ItemBlock)
                {
                    if (blocks.contains(type) == false)
                    {
                        blocks.add(type);
                    }
                }
                else if (items.contains(type) == false)
                {
                    items.add(type);
                }
            }
        }

        Collections.sort(blocks);
        Collections.sort(items);

        int slots = sortInventoryWithinRangeByTypes(inv, blocks, range);
        sortInventoryWithinRangeByTypes(inv, items, new SlotRange(range.first + slots, range.lastExc - (range.first + slots)));
    }

    private static int sortInventoryWithinRangeByTypes(IItemHandlerModifiable inv, List<ItemTypeByName> types, SlotRange range)
    {
        int slot = range.first;
        int slots = 0;

        for (ItemTypeByName type : types)
        {
            ItemStack stack = type.getStack();

            while (true)
            {
                int max = inv instanceof IItemHandlerSize ? ((IItemHandlerSize) inv).getItemStackLimit(slot, stack) : stack.getMaxStackSize();
                //System.out.printf("sorting for: %s - slot: %d, max: %d\n", stack.toString(), slot, max);

                if (slot >= range.lastInc)
                {
                    //System.out.printf("slot >= range.lastInc\n");
                    return slots;
                }

                SlotRange rangeTmp = new SlotRange(slot, range.lastExc - slot);
                stack = collectItemsFromInventoryFromSlotRange(inv, stack, rangeTmp, max, false, false);
                //System.out.printf("collected stack: %s from range: %s\n", stack, rangeTmp.toString());

                if (stack == null)
                {
                    break;
                }

                // There is a stack in the slot that we are moving items to, try to move the stack towards the end of the inventory
                if (inv.getStackInSlot(slot) != null)
                {
                    //System.out.printf("existing stack: %s\n", inv.getStackInSlot(slot).toString());
                    ItemStack stackTmp = inv.getStackInSlot(slot);
                    rangeTmp = new SlotRange(slot + 1, range.lastExc - (slot + 1));
                    stackTmp = tryInsertItemStackToInventoryWithinSlotRange(inv, stackTmp, rangeTmp);
                    //System.out.printf("tried moving stack to range: %s - remaining: %s\n", rangeTmp.toString(), stackTmp);

                    // Failed to move the stack - this shouldn't happen, we are in trouble now!
                    if (stackTmp != null)
                    {
                        //System.out.printf("failed moving existing stack, panic mode!\n");
                        // Try to return all the items currently being worked on and then bail out
                        tryInsertItemStackToInventoryWithinSlotRange(inv, stackTmp, range);
                        tryInsertItemStackToInventoryWithinSlotRange(inv, stack, range);
                        return slots;
                    }
                }

                //System.out.printf("setting stack: %s to slot: %d - slots: %d\n", stack, slot, slots + 1);
                // Put the stack (collected starting from this slot towards the end of the inventory) into this slot
                inv.setStackInSlot(slot, stack);

                /*if (inv instanceof IItemHandlerModifiable)
                {
                    ((IItemHandlerModifiable)inv).setStackInSlot(slot, stack);
                }
                else
                {
                    tryToEmptySlot(inv, slots, 128);
                    inv.insertItem(slot, stack, false);
                }*/

                slot++;
                slots++;
            }
        }

        return slots;
    }

    /**
     * Tries to empty out the given slot by repeatedly calling extractItem() on it and just ignoring the items
     */
    public static boolean tryToEmptySlot(IItemHandler inv, int slot, int maxIterations)
    {
        for (int i = 0; i < maxIterations && inv.getStackInSlot(slot) != null; i++)
        {
            inv.extractItem(slot, 1048576, false); // 1M because why not :p
        }

        return inv.getStackInSlot(slot) == null;
    }

    public static class ItemTypeByName extends ItemType implements Comparable<ItemTypeByName>
    {
        public ItemTypeByName(ItemStack stack)
        {
            super(stack);
        }

        @Override
        public int compareTo(ItemTypeByName other)
        {
            if (other == null)
            {
                throw new NullPointerException();
            }

            String name1 = this.getStack().getItem().getRegistryName().toString();
            String name2 = other.getStack().getItem().getRegistryName().toString();
            int comp = name1.compareToIgnoreCase(name2);

            if (comp != 0)
            {
                return comp;
            }

            int meta1 = this.getStack().getMetadata();
            int meta2 = other.getStack().getMetadata();

            if (meta1 != meta2)
            {
                return meta1 < meta2 ? -1 : 1;
            }

            return 0;
        }
    }

    public static enum InvResult
    {
        MOVED_NOTHING,
        MOVED_SOME,
        MOVED_ALL;
    }
}
