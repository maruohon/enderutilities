package fi.dy.masa.enderutilities.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.container.base.SlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.inventory.wrapper.PlayerMainInvWrapperNoSync;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemHandyBag extends ItemInventoryModular
{
    public static final int META_TIER_1 = 0;
    public static final int META_TIER_2 = 1;

    public static final int INV_SIZE_TIER_1 = 27;
    public static final int INV_SIZE_TIER_2 = 55;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_MOVE_ITEMS    = 1;
    public static final int GUI_ACTION_SORT_ITEMS    = 2;
    public static final int GUI_ACTION_TOGGLE_BLOCK  = 3;
    public static final int GUI_ACTION_TOGGLE_UPDATE = 4;
    public static final int GUI_ACTION_TOGGLE_MODES  = 5;
    public static final int GUI_ACTION_TOGGLE_SHIFTCLICK            = 6;
    public static final int GUI_ACTION_TOGGLE_SHIFTCLICK_DOUBLETAP  = 7;
    public static final int GUI_ACTION_OPEN_BAUBLES  = 100;

    public ItemHandyBag(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
        super.onUpdate(stack, world, entity, slot, isCurrent);

        if (world.isRemote == false)
        {
            if (entity instanceof EntityPlayer)
            {
                this.restockPlayerInventory(stack, world, (EntityPlayer) entity);
            }

            if (Configs.handyBagEnableItemUpdate)
            {
                this.updateItems(stack, world, entity, slot);
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        // If the bag is sneak + right clicked on an inventory, then we try to dump all the contents to that inventory
        if (player.isSneaking())
        {
            this.tryMoveItems(world, pos, side, player.getHeldItem(hand), player);

            return EnumActionResult.SUCCESS;
        }

        return super.onItemUse(player,world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote == false)
        {
            // These two lines are to fix the UUID being missing the first time the GUI opens,
            // if the item is grabbed from the creative inventory or from JEI or from /give
            NBTUtils.getUUIDFromItemStack(stack, "UUID", true);
            player.openContainer.detectAndSendChanges();

            player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_HANDY_BAG_RIGHT_CLICK, world,
                    (int)player.posX, (int)player.posY, (int)player.posZ);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player)
    {
        super.onCreated(stack, world, player);
        // Create the UUID when the item is crafted
        NBTUtils.getUUIDFromItemStack(stack, "UUID", true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName() + "_" + stack.getMetadata();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);

        if (moduleStack.isEmpty() == false && moduleStack.getTagCompound() != null)
        {
            String itemName = super.getItemStackDisplayName(stack); //I18n.format(this.getUnlocalizedName(stack) + ".name").trim();
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName())
            {
                String pre = TextFormatting.GREEN.toString() + TextFormatting.ITALIC.toString();

                if (itemName.length() >= 14)
                {
                    return EUStringUtils.getInitialsWithDots(itemName) + " " + pre + moduleStack.getDisplayName() + rst;
                }

                return itemName + " " + pre + moduleStack.getDisplayName() + rst;
            }

            return itemName;
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addTooltipLines(ItemStack containerStack, EntityPlayer player, List<String> list, boolean verbose)
    {
        if (containerStack.getTagCompound() == null)
        {
            return;
        }

        String preGreen = TextFormatting.GREEN.toString();
        String preRed = TextFormatting.RED.toString();
        String preWhite = TextFormatting.WHITE.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        String strPickupMode = I18n.format("enderutilities.tooltip.item.pickupmode" + (verbose ? "" : ".short")) + ": ";
        String strRestockMode = I18n.format("enderutilities.tooltip.item.restockmode" + (verbose ? "" : ".short")) + ": ";

        PickupMode pickupMode = PickupMode.fromStack(containerStack);
        if (pickupMode == PickupMode.NONE) strPickupMode += preRed;
        else if (pickupMode == PickupMode.MATCHING) strPickupMode += TextFormatting.YELLOW.toString();
        else if (pickupMode == PickupMode.ALL) strPickupMode += preGreen;
        strPickupMode += pickupMode.getDisplayName() + rst;

        RestockMode restockMode = RestockMode.fromStack(containerStack);
        if (restockMode == RestockMode.DISABLED) strRestockMode += preRed;
        else if (restockMode == RestockMode.ALL) strRestockMode += preGreen;
        else strRestockMode += TextFormatting.YELLOW.toString();

        strRestockMode += restockMode.getDisplayName() + rst;

        if (verbose)
        {
            list.add(strPickupMode);
            list.add(strRestockMode);
        }
        else
        {
            list.add(strPickupMode + " / " + strRestockMode);
        }

        String str;

        if (bagIsOpenable(containerStack))
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

        int installed = this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);

        if (installed > 0)
        {
            int slotNum = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
            String preBlue = TextFormatting.BLUE.toString();
            String preWhiteIta = preWhite + TextFormatting.ITALIC.toString();
            String strShort = I18n.format("enderutilities.tooltip.item.selectedmemorycard.short");
            ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
            int max = this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);

            if (moduleStack.isEmpty() == false && moduleStack.getItem() == EnderUtilitiesItems.ENDER_PART)
            {
                String dName = (moduleStack.hasDisplayName() ? preWhiteIta + moduleStack.getDisplayName() + rst + " " : "");
                list.add(String.format("%s %s(%s%d%s / %s%d%s)", strShort, dName, preBlue, slotNum + 1, rst, preBlue, max, rst));

                ((ItemEnderPart) moduleStack.getItem()).addTooltipLines(moduleStack, player, list, false);
                return;
            }
            else
            {
                String strNo = I18n.format("enderutilities.tooltip.item.selectedmemorycard.notinstalled");
                list.add(String.format("%s %s (%s%d%s / %s%d%s)", strShort, strNo, preBlue, slotNum + 1, rst, preBlue, max, rst));
            }
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.nomemorycards"));
        }
    }

    private static InventoryItemModular getInventoryForBag(ItemStack bagStack, EntityPlayer player)
    {
        InventoryItemModular bagInv = null;

        // If this bag is currently open, then use that inventory instead of creating a new one,
        // otherwise the open GUI/inventory will overwrite the changes from the picked up items.
        if (player.openContainer instanceof ContainerHandyBag &&
            ((ContainerHandyBag) player.openContainer).inventoryItemModular.getModularItemStack() == bagStack)
        {
            bagInv = ((ContainerHandyBag) player.openContainer).inventoryItemModular;
        }
        else
        {
            bagInv = new InventoryItemModular(bagStack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        }

        if (bagInv.isAccessibleBy(player) == false)
        {
            return null;
        }

        return bagInv;
    }

    private void updateItems(ItemStack bagStack, World world, Entity entity, int bagSlot)
    {
        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            InventoryItemModular bagInv = getInventoryForBag(bagStack, player);

            if (bagInv != null && player.inventory.getStackInSlot(bagSlot) == bagStack)
            {
                int moduleSlot = bagInv.getSelectedModuleIndex();

                if (moduleSlot >= 0)
                {
                    ItemStack cardStack = bagInv.getModuleInventory().getStackInSlot(moduleSlot);

                    if (cardStack.isEmpty() == false)
                    {
                        // Try to find an empty slot in the player's inventory, for temporarily moving the updated item to
                        int tmpSlot = InventoryUtils.getFirstEmptySlot(new PlayerMainInvWrapperNoSync(player.inventory));
                        long[] masks = new long[] { 0x1FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };
                        long mask = NBTUtils.getLong(cardStack, "HandyBag", "UpdateMask");
                        int numSections = bagStack.getMetadata() == 1 ? 3 : 1;
                        int invSize = bagInv.getSlots();

                        // If there were no empty slots, then we use the bag's slot instead... risky!
                        if (tmpSlot == -1)
                        {
                            tmpSlot = bagSlot;
                        }

                        ItemStack stackInTmpSlot = player.inventory.getStackInSlot(tmpSlot);
                        boolean isCurrentItem = tmpSlot == player.inventory.currentItem;

                        for (int section = 0; section < numSections; section++)
                        {
                            if ((mask & masks[section]) != 0)
                            {
                                SlotRange range = getSlotRangeForSection(section);

                                for (int slot = range.first; slot < range.lastExc && slot < invSize; slot++)
                                {
                                    if ((mask & (1L << slot)) != 0)
                                    {
                                        ItemStack stackTmp = bagInv.getStackInSlot(slot);

                                        if (stackTmp.isEmpty() == false)
                                        {
                                            ItemStack stackOrig = stackTmp.copy();
                                            // Temporarily move the item-being-updated into the temporary slot in the player's inventory
                                            player.inventory.setInventorySlotContents(tmpSlot, stackTmp);

                                            try
                                            {
                                                stackTmp.updateAnimation(world, entity, tmpSlot, isCurrentItem);
                                            }
                                            catch (Throwable t)
                                            {
                                                EnderUtilities.logger.warn("Exception while updating items inside a Handy Bag!", t);
                                            }

                                            // The stack changed while being updated, write it back to the bag's inventory
                                            if (ItemStack.areItemStacksEqual(stackTmp, stackOrig) == false)
                                            {
                                                bagInv.setStackInSlot(slot, stackTmp.isEmpty() ? ItemStack.EMPTY : stackTmp);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Restore the Handy Bag into the original slot it was in
                        player.inventory.setInventorySlotContents(tmpSlot, stackInTmpSlot);
                    }
                }
            }
        }
    }

    private void restockPlayerInventory(ItemStack stack, World world, EntityPlayer player)
    {
        RestockMode mode = RestockMode.fromStack(stack);

        // If Restock mode is enabled, then we will fill the stacks in the player's inventory from the bag
        if (world.isRemote == false && mode != RestockMode.DISABLED)
        {
            InventoryItemModular bagInv = getInventoryForBag(stack, player);

            if (bagInv != null)
            {
                IItemHandler wrappedBagInv = getWrappedEnabledInv(stack, bagInv);
                IItemHandlerModifiable playerInv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

                // Only re-stock the hotbar and the offhand slot
                if (mode == RestockMode.HOTBAR)
                {
                    playerInv = new CombinedInvWrapper(new RangedWrapper(playerInv, 0, 9), new PlayerOffhandInvWrapper(player.inventory));
                }

                InventoryUtils.fillStacksOfMatchingItems(wrappedBagInv, playerInv);
                player.openContainer.detectAndSendChanges();
            }
        }
    }

    private EnumActionResult tryMoveItems(World world, BlockPos pos, EnumFacing side, ItemStack stack, EntityPlayer player)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te == null || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) == false)
        {
            return EnumActionResult.PASS;
        }

        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        InventoryItemModular bagInv = getInventoryForBag(stack, player);

        if (inv == null || bagInv == null)
        {
            return EnumActionResult.PASS;
        }

        IItemHandler wrappedBagInv = getWrappedEnabledInv(stack, bagInv);
        RestockMode restockMode = RestockMode.fromStack(stack);

        if (restockMode == RestockMode.HOTBAR || restockMode == RestockMode.ALL)
        {
            if (world.isRemote == false)
            {
                if (restockMode == RestockMode.HOTBAR)
                {
                    InventoryUtils.tryMoveMatchingItems(wrappedBagInv, inv);
                }
                else
                {
                    InventoryUtils.tryMoveAllItems(wrappedBagInv, inv);
                }

                player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
            }

            return EnumActionResult.SUCCESS;
        }

        PickupMode pickupMode = PickupMode.fromStack(stack);

        if (pickupMode == PickupMode.MATCHING || pickupMode == PickupMode.ALL)
        {
            if (world.isRemote == false)
            {
                if (pickupMode == PickupMode.MATCHING)
                {
                    InventoryUtils.tryMoveMatchingItems(inv, wrappedBagInv);
                }
                else
                {
                    InventoryUtils.tryMoveAllItems(inv, wrappedBagInv);
                }

                player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    private static ItemStack handleItems(ItemStack itemsIn, ItemStack bagStack, EntityPlayer player)
    {
        PickupMode pickupMode = PickupMode.fromStack(bagStack);
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        // First try to fill all existing stacks in the player's inventory
        if (pickupMode != PickupMode.NONE)
        {
            itemsIn = InventoryUtils.tryInsertItemStackToExistingStacksInInventory(playerInv, itemsIn);
        }

        if (itemsIn.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        InventoryItemModular bagInv = getInventoryForBag(bagStack, player);

        if (bagInv != null)
        {
            IItemHandler wrappedBagInv = getWrappedEnabledInv(bagStack, bagInv);

            // If there is no space left in existing stacks in the player's inventory
            // then add the items to the bag, if one of the pickup modes is enabled.
            if (pickupMode == PickupMode.ALL ||
                (pickupMode == PickupMode.MATCHING && InventoryUtils.getSlotOfFirstMatchingItemStack(wrappedBagInv, itemsIn) != -1))
            {
                itemsIn = InventoryUtils.tryInsertItemStackToInventory(wrappedBagInv, itemsIn);
            }
        }

        return itemsIn;
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
        List<Integer> bagSlots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.HANDY_BAG);
        Iterator<ItemStack> iter = event.drops.iterator();

        while (iter.hasNext())
        {
            ItemStack stack = iter.next();

            if (stack.isEmpty())
            {
                iter.remove();
                continue;
            }

            // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
            for (int slot : bagSlots)
            {
                ItemStack bagStack = playerInv.getStackInSlot(slot);

                // Bag is not locked
                if (bagStack.isEmpty() == false && bagStack.getItem() == EnderUtilitiesItems.HANDY_BAG && ItemHandyBag.bagIsOpenable(bagStack))
                {
                    ItemStack stackOrig = stack;
                    stack = handleItems(stack, bagStack, player);

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
        ItemStack stack = entityItem.getItem();
        EntityPlayer player = event.getEntityPlayer();

        if (player.getEntityWorld().isRemote || entityItem.isDead || stack.isEmpty())
        {
            return true;
        }

        int origStackSize = stack.getCount();
        boolean ret = false;

        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.HANDY_BAG);

        for (int slot : slots)
        {
            ItemStack bagStack = playerInv.getStackInSlot(slot);

            // Bag is not locked
            if (bagStack.isEmpty() == false && bagStack.getItem() == EnderUtilitiesItems.HANDY_BAG && ItemHandyBag.bagIsOpenable(bagStack))
            {
                stack = handleItems(stack, bagStack, player);

                if (stack.isEmpty())
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
        if (entityItem.isDead == false && stack.getCount() != origStackSize)
        {
            entityItem.setItem(stack);
        }

        // At least some items were picked up
        if (entityItem.isSilent() == false && (entityItem.isDead || stack.getCount() != origStackSize))
        {
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER,
                    0.2F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        return ret;
    }

    private static boolean bagIsOpenable(ItemStack stack)
    {
        // Can open a fresh bag with no data
        if (stack.getTagCompound() == null)
        {
            return true;
        }

        // If the bag is locked from opening
        if (stack.getTagCompound().getCompoundTag("HandyBag").getBoolean("DisableOpen"))
        {
            return false;
        }

        return true;
    }

    /**
     * Returns an ItemStack containing an enabled Handy Bag in the player's inventory, or null if none is found.
     */
    public static ItemStack getOpenableBag(EntityPlayer player)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.HANDY_BAG);

        for (int slot : slots)
        {
            ItemStack stack = playerInv.getStackInSlot(slot);

            if (bagIsOpenable(stack))
            {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int getSizeInventory(ItemStack containerStack)
    {
        return containerStack.getMetadata() == META_TIER_2 ? INV_SIZE_TIER_2 : INV_SIZE_TIER_1;
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerHandyBag)
        {
            ContainerHandyBag container = (ContainerHandyBag)player.openContainer;
            InventoryItemModular inv = container.inventoryItemModular;
            ItemStack stack = inv.getModularItemStack();

            if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.HANDY_BAG)
            {
                int max = ((ItemHandyBag)stack.getItem()).getMaxModules(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);

                // Changing the selected module via the GUI buttons
                if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < max)
                {
                    UtilItemModular.setModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS, element);
                    inv.readFromContainerItemStack();
                }
                else if (action == GUI_ACTION_MOVE_ITEMS)
                {
                    IItemHandlerModifiable playerMainInv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                    IItemHandlerModifiable offhandInv = new PlayerOffhandInvWrapper(player.inventory);
                    IItemHandler playerInv = new CombinedInvWrapper(playerMainInv, offhandInv);
                    IItemHandler wrappedBagInv = getWrappedEnabledInv(stack, inv);

                    switch (element & 0x7FFF)
                    {
                        case 0: // Move all items to Bag
                            // Holding shift, move all items, even from hotbar
                            if ((element & 0x8000) != 0)
                            {
                                InventoryUtils.tryMoveAllItems(playerInv, wrappedBagInv);
                            }
                            else
                            {
                                InventoryUtils.tryMoveAllItemsWithinSlotRange(playerInv, wrappedBagInv, new SlotRange(9, 27), new SlotRange(wrappedBagInv));
                            }
                            break;

                        case 1: // Move matching items to Bag
                            // Holding shift, move all items, even from hotbar
                            if ((element & 0x8000) != 0)
                            {
                                InventoryUtils.tryMoveMatchingItems(playerInv, wrappedBagInv);
                            }
                            else
                            {
                                InventoryUtils.tryMoveMatchingItemsWithinSlotRange(playerInv, wrappedBagInv, new SlotRange(9, 27), new SlotRange(wrappedBagInv));
                            }
                            break;

                        case 2: // Leave one stack of each item type and fill that stack
                            InventoryUtils.leaveOneFullStackOfEveryItem(playerInv, wrappedBagInv, true);
                            break;

                        case 3: // Fill stacks in player inventory from bag
                            InventoryUtils.fillStacksOfMatchingItems(wrappedBagInv, playerInv);
                            break;

                        case 4: // Move matching items to player inventory
                            InventoryUtils.tryMoveMatchingItems(wrappedBagInv, playerInv);
                            break;

                        case 5: // Move all items to player inventory
                            InventoryUtils.tryMoveAllItems(wrappedBagInv, playerInv);
                            break;
                    }
                }
                else if (action == GUI_ACTION_SORT_ITEMS && element >= 0 && element <= 3)
                {
                    // Player inventory
                    if (element == 3)
                    {
                        IItemHandlerModifiable playerMainInv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                        InventoryUtils.sortInventoryWithinRange(playerMainInv, new SlotRange(9, 27));

                        return;
                    }

                    // The basic tier bag only has one sort button/inventory section
                    if (element > 0 && stack.getMetadata() == 0)
                    {
                        return;
                    }

                    InventoryUtils.sortInventoryWithinRange(inv, getSlotRangeForSection(element));
                }
                else if (action == GUI_ACTION_TOGGLE_BLOCK && element >= 0 && element <= 2)
                {
                    setSlotMask(inv, stack, element, "LockMask");
                }
                else if (action == GUI_ACTION_TOGGLE_UPDATE && element >= 0 && element <= 2)
                {
                    setSlotMask(inv, stack, element, "UpdateMask");
                }
                else if (action == GUI_ACTION_TOGGLE_MODES && (element & 0x03) >= 0 && (element & 0x03) <= 2)
                {
                    switch (element & 0x03)
                    {
                        case 0:
                            NBTUtils.toggleBoolean(stack, "HandyBag", "DisableOpen");
                            break;
                        case 1:
                            PickupMode.cycleMode(stack, (element & 0x8000) != 0);
                            break;
                        case 2:
                            RestockMode.cycleMode(stack, (element & 0x8000) != 0);
                            break;
                        default:
                    }
                }
                else if (action == GUI_ACTION_TOGGLE_SHIFTCLICK)
                {
                    ShiftMode.cycleMode(stack, element != 0);
                }
                else if (action == GUI_ACTION_TOGGLE_SHIFTCLICK_DOUBLETAP)
                {
                    if (ShiftMode.fromStack(stack) == ShiftMode.DOUBLE_TAP)
                    {
                        ShiftMode.toggleDoubleTapEffectiveMode(stack);
                    }
                }
                else if (action == GUI_ACTION_OPEN_BAUBLES && ModRegistry.isModLoadedBaubles())
                {
                    try
                    {
                        ModContainer baublesContainer = Loader.instance().getIndexedModList().get(ModRegistry.MODID_BAUBLES);

                        if (baublesContainer != null)
                        {
                            Object baubles = baublesContainer.getMod();
                            BlockPos pos = player.getPosition();
                            player.openGui(baubles, 0, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                        }
                    }
                    catch (Exception e)
                    {
                        EnderUtilities.logger.warn("Failed to open the Baubles GUI from Handy Bag", e);
                    }
                }
            }
        }
    }

    private static void setSlotMask(InventoryItemModular inv, ItemStack bagStack, int bagSection, String tagName)
    {
        int slot = inv.getSelectedModuleIndex();

        if (slot >= 0)
        {
            ItemStack cardStack = inv.getModuleInventory().getStackInSlot(slot);

            if (cardStack.isEmpty() == false)
            {
                long[] masks = new long[] { 0x1FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };
                long mask = NBTUtils.getLong(cardStack, "HandyBag", tagName);
                mask ^= masks[bagSection];
                NBTUtils.setLong(cardStack, "HandyBag", tagName, mask);
                UtilItemModular.setSelectedModuleStackAbs(bagStack, ModuleType.TYPE_MEMORY_CARD_ITEMS, cardStack);
            }
        }
    }

    private static SlotRange getSlotRangeForSection(int section)
    {
        if (section == 0)
        {
            return new SlotRange(0, 27);
        }
        else if (section == 1)
        {
            return new SlotRange(27, 14);
        }

        return new SlotRange(41, 14);
    }

    private static IItemHandler getWrappedEnabledInv(ItemStack stack, IItemHandlerModifiable baseInv)
    {
        // For the basic version of the bag, there is no locking/sections, so just return the base inventory
        if (stack.getMetadata() != 1)
        {
            return baseInv;
        }

        long[] masks = new long[] { 0x1FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };

        ItemStack cardStack = UtilItemModular.getSelectedModuleStackAbs(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);

        if (cardStack.isEmpty())
        {
            return InventoryUtils.NULL_INV;
        }

        long lockMask = NBTUtils.getLong(cardStack, "HandyBag", "LockMask");

        IItemHandlerModifiable inv = null;

        for (int i = 0; i < 3; i++)
        {
            if ((lockMask & masks[i]) == 0)
            {
                SlotRange range = getSlotRangeForSection(i);

                if (inv == null)
                {
                    inv = new RangedWrapper(baseInv, range.first, range.lastExc);
                }
                else
                {
                    inv = new CombinedInvWrapper(inv, new RangedWrapper(baseInv, range.first, range.lastExc));
                }
            }
        }

        return inv != null ? inv : InventoryUtils.NULL_INV;
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Alt + Toggle mode: Toggle the private/public mode
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_ALT))
        {
            UtilItemModular.changePrivacyModeOnSelectedModuleAbs(stack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        }
        // Shift + Toggle mode: Cycle Pickup Mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            PickupMode.cycleMode(stack, EnumKey.keypressActionIsReversed(key));
        }
        // Just Toggle mode: Toggle Restock mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            RestockMode.cycleMode(stack, EnumKey.keypressActionIsReversed(key));
        }
        // Alt + Shift + Toggle mode: Toggle Locked Mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            NBTUtils.toggleBoolean(stack, "HandyBag", "DisableOpen");
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
        }
    }

    @Override
    public boolean useAbsoluteModuleIndexing(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        return moduleType.equals(ModuleType.TYPE_MEMORY_CARD_ITEMS) ? this.getMaxModules(containerStack) : 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule)
        {
            IModule imodule = (IModule)moduleStack.getItem();

            if (imodule.getModuleType(moduleStack).equals(ModuleType.TYPE_MEMORY_CARD_ITEMS))
            {
                int tier = imodule.getModuleTier(moduleStack);
                if (tier >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B &&
                    tier <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B)
                {
                    return this.getMaxModules(containerStack);
                }
            }
        }

        return 0;
    }

    @Override
    public void getSubItemsCustom(CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // Tier 1
        list.add(new ItemStack(this, 1, 1)); // Tier 2
    }

    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;
        ResourceLocation[] variants = new ResourceLocation[36];
        int i = 0;

        for (String strL : new String[] { "false", "true" })
        {
            for (String strP : new String[] { "none", "matching", "all" })
            {
                for (String strR : new String[] { "disabled", "all", "hotbar" })
                {
                    for (String strT : new String[] { "0", "1" })
                    {
                        String variant = String.format("locked=%s,pickupmode=%s,restockmode=%s,tier=%s", strL, strP, strR, strT);
                        variants[i++] = new ModelResourceLocation(rl, variant);
                    }
                }
            }
        }

        return variants;
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        String variant = "locked=" + (bagIsOpenable(stack) ? "false" : "true") +
                         ",pickupmode=" + PickupMode.fromStack(stack).getVariantName() +
                         ",restockmode=" + RestockMode.fromStack(stack).getName() +
                         ",tier=" + MathHelper.clamp(stack.getMetadata(), 0, 1);

        return new ModelResourceLocation(Reference.MOD_ID + ":" + "item_" + this.name, variant);
    }

    public enum PickupMode
    {
        NONE     (0, "enderutilities.tooltip.item.disabled", "none"),
        MATCHING (1, "enderutilities.tooltip.item.matching", "matching"),
        ALL      (2, "enderutilities.tooltip.item.all",      "all");

        private final String displayName;
        private final String variantName;

        private PickupMode (int id, String displayName, String variantName)
        {
            this.displayName = displayName;
            this.variantName = variantName;
        }

        public String getDisplayName()
        {
            return I18n.format(this.displayName);
        }

        public String getVariantName()
        {
            return this.variantName;
        }

        public static PickupMode fromStack(ItemStack stack)
        {
            int id = NBTUtils.getByte(stack, "HandyBag", "PickupMode");
            return (id >= 0 && id < values().length) ? values()[id] : NONE;
        }

        public static void cycleMode(ItemStack stack, boolean reverse)
        {
            PickupMode mode = PickupMode.fromStack(stack);
            int id = mode.ordinal() + (reverse ? -1 : 1);

            if (id < 0)
            {
                id = values().length - 1;
            }
            else if (id >= values().length)
            {
                id = 0;
            }

            NBTUtils.setByte(stack, "HandyBag", "PickupMode", (byte) id);
        }
    }

    public enum RestockMode
    {
        DISABLED ("disabled"),
        ALL      ("all"),
        HOTBAR   ("hotbar");

        private final String name;

        private RestockMode (String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

        public String getDisplayName()
        {
            return I18n.format("enderutilities.tooltip.item." + this.getName());
        }

        public static RestockMode fromStack(ItemStack stack)
        {
            int id = NBTUtils.getByte(stack, "HandyBag", "RestockMode");
            return (id >= 0 && id < values().length) ? values()[id] : DISABLED;
        }

        public static void cycleMode(ItemStack stack, boolean reverse)
        {
            RestockMode mode = RestockMode.fromStack(stack);
            int id = mode.ordinal() + (reverse ? -1 : 1);

            if (id < 0)
            {
                id = values().length - 1;
            }
            else if (id >= values().length)
            {
                id = 0;
            }

            NBTUtils.setByte(stack, "HandyBag", "RestockMode", (byte) id);
        }
    }

    public enum ShiftMode
    {
        TO_BAG      ("enderutilities.gui.label.handybag.shiftclick.tobag"),
        INV_HOTBAR  ("enderutilities.gui.label.handybag.shiftclick.invhotbar"),
        DOUBLE_TAP  ("enderutilities.gui.label.handybag.shiftclick.doubletapshift");

        private final String unlocName;

        private ShiftMode (String unlocName)
        {
            this.unlocName = unlocName;
        }

        public String getUnlocName()
        {
            return this.unlocName;
        }

        public String getDisplayName()
        {
            return I18n.format(this.getUnlocName());
        }

        public static ShiftMode fromId(int id)
        {
            return (id >= 0 && id < values().length) ? values()[id] : TO_BAG;
        }

        public static ShiftMode fromStack(ItemStack stack)
        {
            return fromId(NBTUtils.getByte(stack, "HandyBag", "ShiftMode") & 0x03);
        }

        public static void cycleMode(ItemStack stack, boolean reverse)
        {
            // The topmost bit indicates the current "double-tapped-mode"
            // So when the main mode is "double-tap-to-toggle", then the topmost bit indicates
            // whether the currently active mode is to-bag or between-inventory-and-hotbar
            int rawMode = NBTUtils.getByte(stack, "HandyBag", "ShiftMode");
            int id = (rawMode & 0x03) + (reverse ? -1 : 1);

            if (id < 0)
            {
                id = values().length - 1;
            }
            else if (id >= values().length)
            {
                id = 0;
            }

            rawMode = (rawMode & 0x80) + id;
            NBTUtils.setByte(stack, "HandyBag", "ShiftMode", (byte) rawMode);
        }

        public static void toggleDoubleTapEffectiveMode(ItemStack stack)
        {
            // The topmost bit indicates the current "double-tapped-mode"
            // So when the main mode is "double-tap-to-toggle", then the topmost bit indicates
            // whether the currently active mode is to-bag or between-inventory-and-hotbar
            byte rawMode = (byte) (NBTUtils.getByte(stack, "HandyBag", "ShiftMode") ^ 0x80);
            NBTUtils.setByte(stack, "HandyBag", "ShiftMode", rawMode);
        }

        /**
         * Returns either TO_BAG or INV_HOTBAR, taking into account
         * a possible active DOUBLE_TAP mode's current "double-tap-status".
         */
        public static ShiftMode getEffectiveMode(ItemStack stack)
        {
            int rawMode = NBTUtils.getByte(stack, "HandyBag", "ShiftMode");
            ShiftMode mode = fromId(rawMode & 0x03);

            if (mode == ShiftMode.DOUBLE_TAP)
            {
                return (rawMode & 0x80) != 0 ? ShiftMode.INV_HOTBAR : ShiftMode.TO_BAG;
            }
            else
            {
                return mode;
            }
        }
    }
}
