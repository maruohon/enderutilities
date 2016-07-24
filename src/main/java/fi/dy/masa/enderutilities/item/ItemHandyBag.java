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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.SlotRange;
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

    public ItemHandyBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_HANDY_BAG);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
        super.onUpdate(stack, world, entity, slot, isCurrent);

        if (entity instanceof EntityPlayer)
        {
            this.restockPlayerInventory(stack, world, (EntityPlayer) entity);
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        // If the bag is sneak + right clicked on an inventory, then we try to dump all the contents to that inventory
        if (player.isSneaking() == true)
        {
            this.tryMoveItems(stack, world, player, pos, side);

            return EnumActionResult.SUCCESS;
        }

        return super.onItemUse(stack, player,world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote == false)
        {
            player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_HANDY_BAG_RIGHT_CLICK, world, (int)player.posX, (int)player.posY, (int)player.posZ);
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
        if (moduleStack != null && moduleStack.getTagCompound() != null)
        {
            String itemName = super.getItemStackDisplayName(stack); //I18n.format(this.getUnlocalizedName(stack) + ".name").trim();
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
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
    public void addInformationSelective(ItemStack containerStack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
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

        if (verbose == true)
        {
            list.add(strPickupMode);
            list.add(strRestockMode);
        }
        else
        {
            list.add(strPickupMode + " / " + strRestockMode);
        }

        String str;
        if (bagIsOpenable(containerStack) == true)
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

            if (moduleStack != null && moduleStack.getItem() == EnderUtilitiesItems.enderPart)
            {
                String dName = (moduleStack.hasDisplayName() ? preWhiteIta + moduleStack.getDisplayName() + rst + " " : "");
                list.add(String.format("%s %s(%s%d%s / %s%d%s)", strShort, dName, preBlue, slotNum + 1, rst, preBlue, max, rst));

                ((ItemEnderPart)moduleStack.getItem()).addInformationSelective(moduleStack, player, list, advancedTooltips, false);
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

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(super.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    public void restockPlayerInventory(ItemStack stack, World world, EntityPlayer player)
    {
        RestockMode mode = RestockMode.fromStack(stack);

        // If Restock mode is enabled, then we will fill the stacks in the player's inventory from the bag
        if (world.isRemote == false && mode != RestockMode.DISABLED)
        {
            InventoryItemModular bagInv;

            // Only re-stock stacks when the player doesn't have a GUI open
            //if (player.openContainer == player.inventoryContainer)
            {
                if (player.openContainer instanceof ContainerHandyBag)
                {
                    bagInv = ((ContainerHandyBag)player.openContainer).inventoryItemModular;
                }
                else
                {
                    bagInv = new InventoryItemModular(stack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS);
                }

                if (bagInv.isAccessibleByPlayer(player) == false)
                {
                    return;
                }

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

    public EnumActionResult tryMoveItems(ItemStack stack, World world, EntityPlayer player, BlockPos pos, EnumFacing side)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te == null || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) == false)
        {
            return EnumActionResult.PASS;
        }

        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        InventoryItemModular bagInv = new InventoryItemModular(stack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (inv == null || bagInv.isAccessibleByPlayer(player) == false)
        {
            return EnumActionResult.PASS;
        }

        IItemHandler wrappedBagInv = getWrappedEnabledInv(stack, bagInv);

        if (RestockMode.fromStack(stack) != RestockMode.DISABLED)
        {
            if (world.isRemote == false)
            {
                InventoryUtils.tryMoveAllItems(wrappedBagInv, inv);
                player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
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

                player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public static ItemStack handleItems(ItemStack itemsIn, ItemStack bagStack, EntityPlayer player)
    {
        PickupMode pickupMode = PickupMode.fromStack(bagStack);
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        InventoryItemModular bagInv = null;

        // First try to fill all existing stacks in the player's inventory
        if (pickupMode != PickupMode.NONE)
        {
            itemsIn = InventoryUtils.tryInsertItemStackToExistingStacksInInventory(playerInv, itemsIn);
        }

        if (itemsIn == null)
        {
            return null;
        }

        // If this bag is currently open, then use that inventory instead of creating a new one,
        // otherwise the open GUI/inventory will overwrite the changes from the picked up items.
        if (player.openContainer instanceof ContainerHandyBag &&
            ((ContainerHandyBag)player.openContainer).inventoryItemModular.getModularItemStack() == bagStack)
        {
            bagInv = ((ContainerHandyBag)player.openContainer).inventoryItemModular;
        }
        else
        {
            bagInv = new InventoryItemModular(bagStack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        }

        IItemHandler wrappedBagInv = getWrappedEnabledInv(bagStack, bagInv);

        // If there is no space left in existing stacks in the player's inventory
        // then add the items to the bag, if one of the pickup modes is enabled.
        if (pickupMode == PickupMode.ALL ||
            (pickupMode == PickupMode.MATCHING && InventoryUtils.getSlotOfFirstMatchingItemStack(wrappedBagInv, itemsIn) != -1))
        {
            itemsIn = InventoryUtils.tryInsertItemStackToInventory(wrappedBagInv, itemsIn);
        }

        return itemsIn;
    }

    /**
     * Tries to first fill the matching stacks in the player's inventory,
     * and then depending on the bag's mode, tries to add the remaining items
     * to the bag's inventory.
     * @param event
     * @return false if all items were handled and further processing of the event should not occur
     */
    public static boolean onItemPickupEvent(PlayerItemPickupEvent event)
    {
        if (event.getEntityPlayer().worldObj.isRemote == true)
        {
            return true;
        }

        boolean pickedUp = false;
        EntityPlayer player = event.getEntityPlayer();
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        List<Integer> bagSlots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.handyBag);

        Iterator<ItemStack> iter = event.drops.iterator();
        while (iter.hasNext() == true)
        {
            ItemStack stack = iter.next();
            if (stack == null)
            {
                iter.remove();
                continue;
            }

            // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
            for (int slot : bagSlots)
            {
                ItemStack bagStack = playerInv.getStackInSlot(slot);
                // Bag is not locked
                if (bagStack != null && bagStack.getItem() == EnderUtilitiesItems.handyBag && ItemHandyBag.bagIsOpenable(bagStack) == true)
                {
                    ItemStack stackOrig = stack;
                    stack = handleItems(stack, bagStack, player);

                    if (stack == null)
                    {
                        iter.remove();
                        pickedUp = true;
                    }
                    else if (stackOrig.stackSize != stack.stackSize)
                    {
                        stackOrig.stackSize = stack.stackSize;
                        pickedUp = true;
                    }
                }
            }
        }

        // At least some items were picked up
        if (pickedUp == true)
        {
            player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.2F,
                    ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        if (event.drops.isEmpty() == true)
        {
            event.setCanceled(true);
            return false;
        }

        return true;
    }

    /**
     * Tries to first fill the matching stacks in the player's inventory,
     * and then depending on the bag's mode, tries to add the remaining items
     * to the bag's inventory.
     * @param event
     * @return false if all items were handled and further processing of the event should not occur
     */
    public static boolean onEntityItemPickupEvent(EntityItemPickupEvent event)
    {
        EntityItem entityItem = event.getItem();

        if (event.getEntityPlayer().worldObj.isRemote == true || entityItem.isDead == true ||
            entityItem.getEntityItem() == null || entityItem.getEntityItem().getItem() == null ||
            entityItem.getEntityItem().stackSize <= 0)
        {
            return true;
        }

        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = entityItem.getEntityItem();
        int origStackSize = stack.stackSize;
        boolean ret = true;

        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.handyBag);
        for (int slot : slots)
        {
            ItemStack bagStack = playerInv.getStackInSlot(slot);
            // Bag is not locked
            if (bagStack != null && bagStack.getItem() == EnderUtilitiesItems.handyBag && ItemHandyBag.bagIsOpenable(bagStack) == true)
            {
                stack = handleItems(stack, bagStack, player);

                if (stack == null || stack.stackSize <= 0)
                {
                    entityItem.setDead();
                    break;
                }
            }
        }

        if (entityItem.isDead == true)
        {
            FMLCommonHandler.instance().firePlayerItemPickupEvent(player, entityItem);
            player.onItemPickup(entityItem, origStackSize);
            event.setCanceled(true);
        }
        // Not everything was handled, update the stack
        else
        {
            entityItem.setEntityItemStack(stack);
        }

        // At least some items were picked up
        if (entityItem.isSilent() == false && (entityItem.isDead == true || stack.stackSize != origStackSize))
        {
            player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER,
                    0.2F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        return ret;
    }

    public static boolean bagIsOpenable(ItemStack stack)
    {
        // Can open a fresh bag with no data
        if (stack.getTagCompound() == null)
        {
            return true;
        }

        // If the bag is locked from opening
        if (stack.getTagCompound().getCompoundTag("HandyBag").getBoolean("DisableOpen") == true)
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
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.handyBag);

        for (int slot : slots)
        {
            ItemStack stack = playerInv.getStackInSlot(slot);

            if (bagIsOpenable(stack) == true)
            {
                return stack;
            }
        }

        return null;
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
            if (stack != null && stack.getItem() == EnderUtilitiesItems.handyBag)
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

                    int meta = stack.getMetadata();
                    // The basic tier bag only has one sort button/inventory section
                    if (element > 0 && meta == 0)
                    {
                        return;
                    }

                    InventoryUtils.sortInventoryWithinRange(inv, getSlotRangeForSection(element));
                }
                else if (action == GUI_ACTION_TOGGLE_BLOCK && element >= 0 && element <= 2)
                {
                    int slot = inv.getSelectedModuleIndex();

                    if (slot >= 0)
                    {
                        ItemStack cardStack = inv.getModuleInventory().getStackInSlot(slot);

                        if (cardStack != null)
                        {
                            long[] masks = new long[] { 0x1FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };
                            long lockMask = NBTUtils.getLong(cardStack, "HandyBag", "LockMask");
                            lockMask ^= masks[element];
                            NBTUtils.setLong(cardStack, "HandyBag", "LockMask", lockMask);
                            UtilItemModular.setSelectedModuleStackAbs(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS, cardStack);
                        }
                    }
                }
            }
        }
    }

    public static SlotRange getSlotRangeForSection(int section)
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

    public static IItemHandler getWrappedEnabledInv(ItemStack stack, IItemHandlerModifiable baseInv)
    {
        // For the basic version of the bag, there is no locking/sections, so just return the base inventory
        if (stack.getMetadata() != 1)
        {
            return baseInv;
        }

        long[] masks = new long[] { 0x1FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };

        ItemStack cardStack = UtilItemModular.getSelectedModuleStackAbs(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (cardStack == null)
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
        // Just Toggle mode: Cycle Pickup Mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            // 0: None, 1: Matching, 2: All
            NBTUtils.cycleByteValue(stack, "HandyBag", "PickupMode", 2);
        }
        // Shift + Toggle mode: Toggle Locked Mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            NBTUtils.toggleBoolean(stack, "HandyBag", "DisableOpen");
        }
        // Alt + Shift + Toggle mode: Toggle Restock mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            RestockMode.cycleMode(stack, EnumKey.keypressActionIsReversed(key));
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

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // Tier 1
        list.add(new ItemStack(this, 1, 1)); // Tier 2
    }

    @SideOnly(Side.CLIENT)
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

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        String variant = "locked=" + (bagIsOpenable(stack) == true ? "false" : "true") +
                         ",pickupmode=" + PickupMode.fromStack(stack).getVariantName() +
                         ",restockmode=" + RestockMode.fromStack(stack).getName() +
                         ",tier=" + MathHelper.clamp_int(stack.getMetadata(), 0, 1);

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
}
