package fi.dy.masa.enderutilities.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import fi.dy.masa.enderutilities.util.SlotRange;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ItemHandyBag extends ItemInventoryModular
{
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
            this.tryMoveItems(stack, world, player, pos, side);

            return true;
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
        String preRed = EnumChatFormatting.RED.toString();
        String preWhite = EnumChatFormatting.WHITE.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        String strPickupMode = StatCollector.translateToLocal("enderutilities.tooltip.item.pickupmode" + (verbose ? "" : ".short")) + ": ";
        String strRestockMode = StatCollector.translateToLocal("enderutilities.tooltip.item.restockmode" + (verbose ? "" : ".short")) + ": ";

        PickupMode pickupMode = PickupMode.fromStack(containerStack);
        if (pickupMode == PickupMode.NONE) strPickupMode += preRed;
        else if (pickupMode == PickupMode.MATCHING) strPickupMode += EnumChatFormatting.YELLOW.toString();
        else if (pickupMode == PickupMode.ALL) strPickupMode += preGreen;
        strPickupMode += pickupMode.getDisplayName() + rst;

        RestockMode restockMode = RestockMode.fromStack(containerStack);
        if (restockMode == RestockMode.DISABLED) strRestockMode += preRed;
        else strRestockMode += preGreen;
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
        if (world.isRemote == false && entity instanceof EntityPlayer && RestockMode.fromStack(stack) == RestockMode.ENABLED)
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
                    inv = new InventoryItemModular(stack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS);
                }

                if (inv.isUseableByPlayer(player) == false)
                {
                    return;
                }

                InventoryUtils.fillStacksOfMatchingItems(inv, new PlayerMainInvWrapper(player.inventory));

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
        if (te == null || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) == false)
        {
            return false;
        }

        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        InventoryItemModular bagInvnv = new InventoryItemModular(stack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (inv == null || bagInvnv.isUseableByPlayer(player) == false)
        {
            return false;
        }

        if (RestockMode.fromStack(stack) == RestockMode.ENABLED)
        {
            if (world.isRemote == false)
            {
                InventoryUtils.tryMoveAllItems(bagInvnv, inv);
                player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.2f, 1.8f);
            }

            return true;
        }

        PickupMode pickupMode = PickupMode.fromStack(stack);
        if (pickupMode == PickupMode.MATCHING || pickupMode == PickupMode.ALL)
        {
            if (world.isRemote == false)
            {
                if (pickupMode == PickupMode.MATCHING)
                {
                    InventoryUtils.tryMoveMatchingItems(inv, bagInvnv);
                }
                else
                {
                    InventoryUtils.tryMoveAllItems(inv, bagInvnv);
                }

                player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.2f, 1.8f);
            }

            return true;
        }

        return false;
    }

    public static ItemStack handleItems(ItemStack itemsIn, ItemStack bagStack, EntityPlayer player)
    {
        PickupMode pickupMode = PickupMode.fromStack(bagStack);
        IItemHandler playerInv = new PlayerMainInvWrapper(player.inventory);
        InventoryItemModular bagInv = new InventoryItemModular(bagStack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS);

        // First try to fill all existing stacks in the player's inventory
        if (pickupMode != PickupMode.NONE)
        {
            itemsIn = InventoryUtils.tryInsertItemStackToExistingStacksInInventory(playerInv, itemsIn);
        }

        if (itemsIn == null)
        {
            return null;
        }

        // If there is no space left in existing stacks in the player's inventory
        // then add the items to the bag, if one of the pickup modes is enabled.
        if (pickupMode == PickupMode.ALL ||
            (pickupMode == PickupMode.MATCHING && InventoryUtils.getSlotOfFirstMatchingItemStack(bagInv, itemsIn) != -1))
        {
            itemsIn = InventoryUtils.tryInsertItemStackToInventory(bagInv, itemsIn);
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
        if (event.entityPlayer.worldObj.isRemote == true)
        {
            return true;
        }

        boolean pickedUp = false;
        EntityPlayer player = event.entityPlayer;
        List<Integer> bagSlots = InventoryUtils.getSlotNumbersOfMatchingItems(new PlayerMainInvWrapper(player.inventory), EnderUtilitiesItems.handyBag);

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
                ItemStack bagStack = player.inventory.getStackInSlot(slot);
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
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
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
        if (event.entityPlayer.worldObj.isRemote == true || event.item.isDead == true ||
            event.item.getEntityItem() == null || event.item.getEntityItem().getItem() == null)
        {
            return true;
        }

        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.item.getEntityItem();
        int origStackSize = stack.stackSize;
        boolean ret = true;

        // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(new PlayerMainInvWrapper(player.inventory), EnderUtilitiesItems.handyBag);
        for (int slot : slots)
        {
            ItemStack bagStack = player.inventory.getStackInSlot(slot);
            // Bag is not locked
            if (bagStack != null && bagStack.getItem() == EnderUtilitiesItems.handyBag && ItemHandyBag.bagIsOpenable(bagStack) == true)
            {
                stack = handleItems(stack, bagStack, player);

                if (stack == null || stack.stackSize <= 0)
                {
                    event.item.setDead();
                    break;
                }
            }
        }

        if (event.item.isDead == true)
        {
            FMLCommonHandler.instance().firePlayerItemPickupEvent(player, event.item);
            player.onItemPickup(event.item, origStackSize);
            event.setCanceled(true);
        }
        // Not everything was handled, update the stack
        else
        {
            event.item.setEntityItemStack(stack);
        }

        // At least some items were picked up
        if (event.item.isSilent() == false && (event.item.isDead == true || stack.stackSize != origStackSize))
        {
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
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
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(new PlayerMainInvWrapper(player.inventory), EnderUtilitiesItems.handyBag);
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
    public int getSizeInventory(ItemStack containerStack)
    {
        return containerStack.getItemDamage() == DAMAGE_TIER_2 ? INV_SIZE_TIER_2 : INV_SIZE_TIER_1;
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
                    IItemHandler playerInv = new PlayerMainInvWrapper(player.inventory);

                    switch(element & 0x7FFF)
                    {
                        case 0: // Move all items to Bag
                            // Holding shift, move all items, even from hotbar
                            if ((element & 0x8000) != 0)
                            {
                                InventoryUtils.tryMoveAllItems(playerInv, inv);
                            }
                            else
                            {
                                InventoryUtils.tryMoveAllItemsWithinSlotRange(playerInv, inv, new SlotRange(9, 27), new SlotRange(inv));
                            }
                            break;
                        case 1: // Move matching items to Bag
                            InventoryUtils.tryMoveMatchingItems(playerInv, inv);
                            break;
                        case 2: // Leave one stack of each item type and fill that stack
                            InventoryUtils.leaveOneFullStackOfEveryItem(playerInv, inv, true);
                            break;
                        case 3: // Fill stacks in player inventory from bag
                            InventoryUtils.fillStacksOfMatchingItems(inv, playerInv);
                            break;
                        case 4: // Move matching items to player inventory
                            InventoryUtils.tryMoveMatchingItems(inv, playerInv);
                            break;
                        case 5: // Move all items to player inventory
                            InventoryUtils.tryMoveAllItems(inv, playerInv);
                            break;
                    }
                }
            }
        }
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
        String variant = "locked=" + (bagIsOpenable(stack) == true ? "false" : "true") +
                         ",pickupmode=" + PickupMode.fromStack(stack).getVariantName() +
                         ",restockmode=" + (RestockMode.fromStack(stack) == RestockMode.ENABLED ? "true" : "false") +
                         ",tier=" + MathHelper.clamp_int(stack.getItemDamage(), 0, 1);

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
            return StatCollector.translateToLocal(this.displayName);
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
        DISABLED (0, "enderutilities.tooltip.item.disabled"),
        ENABLED  (1, "enderutilities.tooltip.item.enabled");

        private final String displayName;

        private RestockMode (int id, String displayName)
        {
            this.displayName = displayName;
        }

        public String getDisplayName()
        {
            return StatCollector.translateToLocal(this.displayName);
        }

        public static RestockMode fromStack(ItemStack stack)
        {
            int id = NBTUtils.getByte(stack, "HandyBag", "RestockMode");
            return (id >= 0 && id < values().length) ? values()[id] : DISABLED;
        }
    }
}
