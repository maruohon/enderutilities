package fi.dy.masa.enderutilities.item;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemHandyBag extends ItemInventoryModular
{
    public static final String[] VARIANT_PICKUP_MODES = new String[] { "none", "matching", "all" };

    public static final int MODE_RESTOCK_ENABLED = 1;
    public static final int MODE_PICKUP_MATCHING = 1;
    public static final int MODE_PICKUP_ALL = 2;

    public static final int DAMAGE_TIER_1 = 0;
    public static final int DAMAGE_TIER_2 = 1;

    public static final int INV_SIZE_TIER_1 = 27;
    public static final int INV_SIZE_TIER_2 = 55;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_MOVE_ITEMS    = 1;

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

        this.restockPlayerInventory(stack, world, entity);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        // If the bag is sneak + right clicked on an inventory, then we try to dump all the contents to that inventory
        if (player.isSneaking() == true)
        {
            return this.tryMoveItems(stack, world, player, pos, side);
        }

        return super.onItemUse(stack, player,world, pos, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        if (worldIn.isRemote == false)
        {
            playerIn.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_HANDY_BAG_RIGHT_CLICK, worldIn, (int)playerIn.posX, (int)playerIn.posY, (int)playerIn.posZ);
        }

        return itemStackIn;
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
        return super.getUnlocalizedName() + "." + stack.getItemDamage();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (moduleStack != null && moduleStack.getTagCompound() != null)
        {
            String itemName = super.getItemStackDisplayName(stack); //StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
            {
                String pre = EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC.toString();
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

        String preGreen = EnumChatFormatting.GREEN.toString();
        String preYellow = EnumChatFormatting.YELLOW.toString();
        String preRed = EnumChatFormatting.RED.toString();
        String preWhite = EnumChatFormatting.WHITE.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        String strPickupMode = StatCollector.translateToLocal("enderutilities.tooltip.item.pickupmode" + (verbose ? "" : ".short")) + ": ";
        String strRestockMode = StatCollector.translateToLocal("enderutilities.tooltip.item.restockmode" + (verbose ? "" : ".short")) + ": ";
        int mode = this.getModeByName(containerStack, "PickupMode");
        if (mode == 0)
            strPickupMode += preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.disabled") + rst;
        else if (mode == MODE_PICKUP_MATCHING)
            strPickupMode += preYellow + StatCollector.translateToLocal("enderutilities.tooltip.item.matching") + rst;
        else// if (mode == 2)
            strPickupMode += preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.all") + rst;

        mode = this.getModeByName(containerStack, "RestockMode");
        if (mode == 0)
            strRestockMode += preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.disabled") + rst;
        else
            strRestockMode += preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.enabled") + rst;

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
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.enabled") + ": " +
                    preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes");
        }
        else
        {
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.enabled") + ": " +
                    preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no");
        }
        list.add(str);

        int installed = this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (installed > 0)
        {
            int slotNum = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
            String preBlue = EnumChatFormatting.BLUE.toString();
            String preWhiteIta = preWhite + EnumChatFormatting.ITALIC.toString();
            String strShort = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.short");
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
                String strNo = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.notinstalled");
                list.add(String.format("%s %s (%s%d%s / %s%d%s)", strShort, strNo, preBlue, slotNum + 1, rst, preBlue, max, rst));
            }
        }
        else
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.nomemorycards"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(super.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    public void restockPlayerInventory(ItemStack stack, World world, Entity entity)
    {
        // If Restock mode is enabled, then we will fill the stacks in the player's inventory from the bag
        if (world.isRemote == false && entity instanceof EntityPlayer && this.getModeByName(stack, "RestockMode") == MODE_RESTOCK_ENABLED)
        {
            EntityPlayer player = (EntityPlayer)entity;
            InventoryItemModular inv;
            // Only re-stock stacks when the player doesn't have a GUI open
            //if (player.openContainer == player.inventoryContainer)
            {
                if (player.openContainer instanceof ContainerHandyBag)
                {
                    inv = ((ContainerHandyBag)player.openContainer).inventoryItemModular;
                }
                else
                {
                    inv = new InventoryItemModular(stack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
                }

                if (inv.isUseableByPlayer(player) == false)
                {
                    return;
                }

                InventoryUtils.fillStacksOfMatchingItems(inv, player.inventory);

                //if (player.openContainer instanceof ContainerHandyBag)
                {
                    player.openContainer.detectAndSendChanges();
                    player.inventory.markDirty();
                }
            }
        }
    }

    public boolean tryMoveItems(ItemStack stack, World world, EntityPlayer player, BlockPos pos, EnumFacing side)
    {
        TileEntity te = world.getTileEntity(pos);
        if (world.isRemote == false && te != null && te instanceof IInventory)
        {
            InventoryItemModular inv = new InventoryItemModular(stack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
            if (inv.isUseableByPlayer(player) == false)
            {
                return false;
            }

            int mode = this.getModeByName(stack, "RestockMode");
            if (mode == MODE_RESTOCK_ENABLED)
            {
                InventoryUtils.tryMoveAllItems(inv, (IInventory)te, EnumFacing.UP, side);
                player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.2f, 1.8f);
                return true;
            }

            mode = this.getModeByName(stack, "PickupMode");
            if (mode == MODE_PICKUP_MATCHING)
            {
                InventoryUtils.tryMoveMatchingItems((IInventory)te, inv, side, EnumFacing.UP, true);
                player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.2f, 1.8f);
                return true;
            }
            else if (mode == MODE_PICKUP_ALL)
            {
                InventoryUtils.tryMoveAllItems((IInventory)te, inv, side, EnumFacing.UP, true);
                player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.2f, 1.8f);
                return true;
            }
        }

        return false;
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
        if (event.entityPlayer.worldObj.isRemote == true)
        {
            return true;
        }

        boolean ret = true;
        boolean pickedUp = false;
        EntityPlayer player = event.entityPlayer;
        List<Integer> bagSlots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.handyBag);

        Iterator<ItemStack> iter = event.drops.iterator();
        while (iter.hasNext() == true)
        {
            ItemStack stack = iter.next();
            if (stack == null)
            {
                continue;
            }

            // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
            for (int slot : bagSlots)
            {
                ItemStack bagStack = player.inventory.getStackInSlot(slot);
                // Bag is not locked
                if (bagStack != null && bagStack.getItem() == EnderUtilitiesItems.handyBag && ItemHandyBag.bagIsOpenable(bagStack) == true)
                {
                    InventoryItemModular bagInv = new InventoryItemModular(bagStack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
                    int pickupMode = NBTUtils.getByte(bagStack, "HandyBag", "PickupMode");

                    // Some pickup mode enabled and all the items fit into existing stacks in the player's inventory
                    if ((pickupMode == 1 || pickupMode == 2) &&
                       (InventoryUtils.tryInsertItemStackToExistingStacksInInventory(player.inventory, stack, EnumFacing.UP, false) == null))
                    {
                        iter.remove();
                        pickedUp = true;
                        break;
                    }

                    // Pickup mode is All, or Matching and the bag already contains the same item type
                    if (pickupMode == 2 || (pickupMode == 1 && InventoryUtils.getSlotOfFirstMatchingItemStack(bagInv, stack) != -1))
                    {
                        // All items successfully inserted
                        if (InventoryUtils.tryInsertItemStackToInventory(bagInv, stack, EnumFacing.UP, true) == null)
                        {
                            iter.remove();
                            pickedUp = true;
                            break;
                        }
                    }
                }
            }
        }

        if (event.drops.isEmpty() == true)
        {
            event.setCanceled(true);
            ret = false;
        }

        // At least some items were picked up
        if (pickedUp == true)
        {
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F,
                    ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        return ret;
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
        if (event.entityPlayer.worldObj.isRemote == true || event.item.isDead == true ||
            event.item.getEntityItem() == null || event.item.getEntityItem().getItem() == null)
        {
            return true;
        }

        int origStackSize = event.item.getEntityItem().stackSize;
        EntityPlayer player = event.entityPlayer;

        // If all the items fit into existing stacks in the player's inventory, then we do nothing more here
        if (InventoryUtils.tryInsertItemStackToExistingStacksInInventory(player.inventory, event.item.getEntityItem(), EnumFacing.UP, false) == null)
        {
            event.setCanceled(true);
            FMLCommonHandler.instance().firePlayerItemPickupEvent(player, event.item);
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F,
                    ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.onItemPickup(event.item, origStackSize);
            return false;
        }

        boolean ret = true;
        // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.handyBag);
        for (int slot : slots)
        {
            ItemStack bagStack = player.inventory.getStackInSlot(slot);
            // Bag is not locked
            if (bagStack != null && bagStack.getItem() == EnderUtilitiesItems.handyBag && ItemHandyBag.bagIsOpenable(bagStack) == true)
            {
                InventoryItemModular inv = new InventoryItemModular(bagStack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
                int pickupMode = NBTUtils.getByte(bagStack, "HandyBag", "PickupMode");

                // Pickup mode is All, or Matching and the bag already contains the same item type
                if (pickupMode == 2 || (pickupMode == 1 && InventoryUtils.getSlotOfFirstMatchingItemStack(inv, event.item.getEntityItem()) != -1))
                {
                    // All items successfully inserted
                    if (InventoryUtils.tryInsertItemStackToInventory(inv, event.item.getEntityItem(), EnumFacing.UP, true) == null)
                    {
                        event.item.setDead();
                        event.setCanceled(true);
                        ret = false;
                        break;
                    }
                }
            }
        }

        // At least some items were picked up
        if (event.item.getEntityItem().stackSize != origStackSize)
        {
            FMLCommonHandler.instance().firePlayerItemPickupEvent(player, event.item);
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F,
                    ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.onItemPickup(event.item, origStackSize);
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
     * Returns the slot number of the first open-able Handy Bag in the player's inventory, or -1 if none is found.
     */
    public static int getSlotContainingOpenableBag(EntityPlayer player)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.handyBag);
        for (int slot : slots)
        {
            if (bagIsOpenable(player.inventory.getStackInSlot(slot)) == true)
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns an ItemStack containing an enabled Handy Bag in the player's inventory, or null if none is found.
     */
    public static ItemStack getOpenableBag(EntityPlayer player)
    {
        int slotNum = getSlotContainingOpenableBag(player);
        return slotNum != -1 ? player.inventory.getStackInSlot(slotNum) : null;
    }

    @Override
    public int getSizeModuleInventory(ItemStack containerStack)
    {
        return this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
    }

    @Override
    public int getSizeInventory(ItemStack containerStack)
    {
        return containerStack.getItemDamage() == DAMAGE_TIER_2 ? INV_SIZE_TIER_2 : INV_SIZE_TIER_1;
    }

    @Override
    public int getInventoryStackLimit(ItemStack containerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (moduleStack != null && moduleStack.getItem() instanceof IModule)
        {
            int tier = ((IModule) moduleStack.getItem()).getModuleTier(moduleStack);
            if (tier >= 6 && tier <= 12)
            {
                return (int)Math.pow(2, tier);
            }
        }

        return 0;
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
                else if (action == GUI_ACTION_MOVE_ITEMS && element >= 0 && element <= 5)
                {
                    int bagMaxSlot = inv.getSlots() - 1;
                    int playerMaxSlot = player.inventory.getSizeInventory() - 5;
                    EnumFacing up = EnumFacing.UP;

                    switch(element)
                    {
                        case 0: // Move all items to Bag
                            InventoryUtils.tryMoveAllItemsWithinSlotRange(player.inventory, inv, up, up, 0, playerMaxSlot, 0, bagMaxSlot, true);
                            break;
                        case 1: // Move matching items to Bag
                            InventoryUtils.tryMoveMatchingItemsWithinSlotRange(player.inventory, inv, up, up, 0, playerMaxSlot, 0, bagMaxSlot, true);
                            break;
                        case 2: // Leave one stack of each item type and fill that stack
                            InventoryUtils.leaveOneFullStackOfEveryItem(player.inventory, inv, false, false, true);
                            break;
                        case 3: // Fill stacks in player inventory from bag
                            InventoryUtils.fillStacksOfMatchingItemsWithinSlotRange(inv, player.inventory, up, up, 0, bagMaxSlot, 0, playerMaxSlot, false);
                            break;
                        case 4: // Move matching items to player inventory
                            InventoryUtils.tryMoveMatchingItemsWithinSlotRange(inv, player.inventory, up, up, 0, bagMaxSlot, 0, playerMaxSlot, false);
                            break;
                        case 5: // Move all items to player inventory
                            InventoryUtils.tryMoveAllItemsWithinSlotRange(inv, player.inventory, up, up, 0, bagMaxSlot, 0, playerMaxSlot, false);
                            break;
                    }
                }
            }
        }
    }

    public int getModeByName(ItemStack stack, String name)
    {
        return NBTUtils.getByte(stack, "HandyBag", name);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Alt + Toggle mode: Toggle the private/public mode
        if (ReferenceKeys.keypressContainsAlt(key) == true
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsControl(key) == false)
        {
            UtilItemModular.changePrivacyModeOnSelectedModuleAbs(stack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        }
        // Just Toggle mode: Cycle Pickup Mode
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            // 0: None, 1: Matching, 2: All
            NBTUtils.cycleByteValue(stack, "HandyBag", "PickupMode", 2);
        }
        // Shift + Toggle mode: Toggle Locked Mode
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            NBTUtils.toggleBoolean(stack, "HandyBag", "DisableOpen");
        }
        // Alt + Shift + Toggle mode: Toggle Restock mode
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == true)
        {
            // 0: None, 1: Matching, 2: All
            NBTUtils.cycleByteValue(stack, "HandyBag", "RestockMode", 1);
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS,
                    ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
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
        ResourceLocation[] variants = new ResourceLocation[24];
        int i = 0;

        for (String strL : new String[] { "false", "true" })
        {
            for (String strP : new String[] { "none", "matching", "all" })
            {
                for (String strR : new String[] { "false", "true" })
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
        int p = MathHelper.clamp_int(this.getModeByName(stack, "PickupMode"), 0, 2);
        String variant = "locked=" + (bagIsOpenable(stack) == true ? "false" : "true") +
                         ",pickupmode=" + VARIANT_PICKUP_MODES[p] +
                         ",restockmode=" + (this.getModeByName(stack, "RestockMode") != 0 ? "true" : "false") +
                         ",tier=" + MathHelper.clamp_int(stack.getItemDamage(), 0, 1);

        return new ModelResourceLocation(Reference.MOD_ID + ":" + "item_" + this.name, variant);
    }
}
