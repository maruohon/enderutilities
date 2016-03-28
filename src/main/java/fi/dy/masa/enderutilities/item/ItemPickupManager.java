package fi.dy.masa.enderutilities.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.inventory.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.InventoryItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.SlotRange;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ItemPickupManager extends ItemLocationBoundModular implements IKeyBound
{
    public static final String TAG_NAME_CONTAINER = "PickupManager";
    public static final String TAG_NAME_TX_INVENTORY = "TransportItems";
    public static final String TAG_NAME_FILTER_INVENTORY_PRE = "FilterItems_";
    public static final String TAG_NAME_PRESET_SELECTION = "SelPreset";
    public static final String TAG_NAME_PRESET = "Preset_";
    public static final String TAG_NAME_LOCKED = "Locked";

    public static final String TAG_NAME_INVFILTER_ENABLED = "InvFiltEnabled";
    public static final String TAG_NAME_INVFILTER_MODE = "InvFiltMode";
    public static final String TAG_NAME_INVFILTER_META = "InvFiltMeta";
    public static final String TAG_NAME_INVFILTER_NBT = "InvFiltNBT";

    public static final String TAG_NAME_TXFILTER_ENABLED = "TxFiltEnabled";
    public static final String TAG_NAME_TXFILTER_MODE = "TxFiltMode";
    public static final String TAG_NAME_TXFILTER_META = "TxFiltMeta";
    public static final String TAG_NAME_TXFILTER_NBT = "TxFiltNBT";

    public static final int ENDER_CHARGE_COST_PER_SENT_ITEM = 2;

    public static final int NUM_PRESETS = 4;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_CHANGE_PRESET = 1;
    public static final int GUI_ACTION_TOGGLE_INVENTORY_SETTINGS = 2;
    public static final int GUI_ACTION_TOGGLE_TRANSPORT_SETTINGS = 3;

    public static final SlotRange TRANSPORT_FILTER_SLOTS = new SlotRange(0, 18);
    public static final SlotRange INVENTORY_FILTER_SLOTS = new SlotRange(18, 18);

    public ItemPickupManager()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_PICKUP_MANAGER);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        return stack;
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player)
    {
        super.onCreated(stack, world, player);
        // Create the UUID when the item is crafted
        NBTUtils.getUUIDFromItemStack(stack, "UUID", true);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        int preset = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        String pre = EnumChatFormatting.GREEN.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        return super.getItemStackDisplayName(stack) + " - P: " + pre + (preset + 1) + rst;
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
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        String str;
        if (isEnabled(containerStack) == true)
        {
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.enabled") + ": " + preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.enabled") + ": " + preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst;
        }

        list.add(str);

        int preset = NBTUtils.getByte(containerStack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION) + 1;
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.preset") + ": " + EnumChatFormatting.BLUE.toString() + preset + rst);

        super.addInformationSelective(containerStack, player, list, advancedTooltips, verbose);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(super.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    public static NBTTagCompound getSelectedPresetTag(ItemStack stack, boolean create)
    {
        if (stack == null)
        {
            return null;
        }

        NBTTagCompound containerTag = NBTUtils.getCompoundTag(stack, TAG_NAME_CONTAINER, create);
        if (containerTag != null)
        {
            int selection = containerTag.getByte(TAG_NAME_PRESET_SELECTION);
            if (containerTag.hasKey(TAG_NAME_PRESET + selection, Constants.NBT.TAG_COMPOUND) == true)
            {
                return containerTag.getCompoundTag(TAG_NAME_PRESET + selection);
            }
            else if (create == true)
            {
                NBTTagCompound tag = new NBTTagCompound();
                containerTag.setTag(TAG_NAME_PRESET + selection, tag);
                return tag;
            }
        }

        return null;
    }

    public static byte getSettingValue(ItemStack stack, String tagName)
    {
        NBTTagCompound tag = getSelectedPresetTag(stack, false);
        return tag != null ? tag.getByte(tagName) : 0;
    }

    public static void setSettingValue(ItemStack stack, String tagName, byte value)
    {
        NBTTagCompound tag = getSelectedPresetTag(stack, true);
        tag.setByte(tagName, value);
    }

    public static boolean isEnabled(ItemStack stack)
    {
        return NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED) == false;
    }

    /**
     * Returns the ItemStacks of enabled Pickup Managers in the player's inventory
     */
    public static List<ItemStack> getEnabledItems(EntityPlayer player)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(new PlayerMainInvWrapper(player.inventory), EnderUtilitiesItems.pickupManager);
        List<ItemStack> enabledItems = new ArrayList<ItemStack>();

        for (int slot : slots)
        {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (isEnabled(stack) == true)
            {
                enabledItems.add(stack);
            }
        }

        return enabledItems;
    }

    /**
     * Returns the first found ItemStack containing an enabled Pickup Manager in the player's inventory, or null if none are found.
     */
    public static ItemStack getFirstEnabledItem(EntityPlayer player)
    {
        List<ItemStack> items = getEnabledItems(player);
        return items.size() > 0 ? items.get(0) : null;
    }

    public boolean tryTransportItemsFromTransportSlot(InventoryItem inv, EntityPlayer player, ItemStack manager)
    {
        ItemStack stack = inv.getStackInSlot(0);
        if (stack == null)
        {
            return false;
        }

        int sizeOrig = stack.stackSize;
        int max = stack.getMaxStackSize();
        stack = inv.extractItem(0, max, false);

        while (stack != null && stack.stackSize > 0)
        {
            stack = this.tryTransportItems(player, manager, stack);

            // Could not transport the whole stack (anymore)
            if (stack != null)
            {
                inv.insertItem(0, stack, false);
                break;
            }

            stack = inv.extractItem(0, max, false);
        }

        stack = inv.getStackInSlot(0);

        return stack == null || stack.stackSize != sizeOrig;
    }

    public ItemStack tryTransportItems(EntityPlayer player, ItemStack manager, ItemStack itemsIn)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(manager, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null || itemsIn == null)
        {
            return itemsIn;
        }

        NBTHelperPlayer owner = NBTHelperPlayer.getPlayerDataFromItem(moduleStack);
        if (owner != null && owner.canAccess(player) == false)
        {
            return itemsIn;
        }

        NBTHelperTarget target = NBTHelperTarget.getTargetFromItem(moduleStack);
        if (target != null)
        {
            World world = MinecraftServer.getServer().worldServerForDimension(target.dimension);
            // Force load the target chunk with a 30 second unload delay.
            if (world == null || ChunkLoading.getInstance().loadChunkForcedWithModTicket(target.dimension,
                    target.pos.getX() >> 4, target.pos.getZ() >> 4, 30) == false)
            {
                return itemsIn;
            }

            TileEntity te = world.getTileEntity(target.pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing) == true)
            {
                IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing);
                if (inv == null)
                {
                    return itemsIn;
                }

                //return InventoryUtils.tryInsertItemStackToInventory(inv, itemsIn);
                ItemStack stackToSend = itemsIn.copy();

                int cost = ENDER_CHARGE_COST_PER_SENT_ITEM;
                // Not enough Ender Charge to send all the items
                if (UtilItemModular.useEnderCharge(manager, cost * itemsIn.stackSize, true) == false)
                {
                    int available = UtilItemModular.getAvailableEnderCharge(manager);
                    if (available < cost)
                    {
                        return itemsIn;
                    }

                    stackToSend.stackSize = Math.min(itemsIn.stackSize, available / cost);
                }

                int numTransported = stackToSend.stackSize;
                ItemStack itemsRemaining = InventoryUtils.tryInsertItemStackToInventory(inv, stackToSend);

                if (itemsRemaining != null)
                {
                    numTransported -= itemsRemaining.stackSize;
                }

                itemsIn.stackSize -= numTransported;

                // Get the final charge amount
                UtilItemModular.useEnderCharge(manager, numTransported * cost, false);

                if (itemsIn.stackSize <= 0)
                {
                    return null;
                }
            }
        }

        return itemsIn;
    }

    /**
     * Tries to handle the given ItemStack. Returns true if all items were handled and further processing should be canceled.
     */
    public Result handleItems(EntityPlayer player, ItemStack manager, ItemStack itemsIn)
    {
        byte preset = NBTUtils.getByte(manager, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);

        InventoryItem inv = new InventoryItem(manager, 36, 1, false, player.worldObj.isRemote, player, TAG_NAME_FILTER_INVENTORY_PRE + preset);
        inv.readFromContainerItemStack();

        // Transport filters/functionality enabled
        if (getSettingValue(manager, TAG_NAME_TXFILTER_ENABLED) != 0)
        {
            boolean ignoreMeta = getSettingValue(manager, TAG_NAME_TXFILTER_META) != 0;
            boolean ignoreNbt = getSettingValue(manager, TAG_NAME_TXFILTER_NBT) == 0;
            boolean match = InventoryUtils.matchingStackFoundInSlotRange(inv, TRANSPORT_FILTER_SLOTS, itemsIn, ignoreMeta, ignoreNbt);

            // 0 = Black list
            byte mode = getSettingValue(manager, TAG_NAME_TXFILTER_MODE);

            // White list and match found, or black list and no match found
            if ((mode != 0 && match == true) || (mode == 0 && match == false))
            {
                ItemStack stackTmp = this.tryTransportItems(player, manager, itemsIn);
                // All items successfully transported
                if (stackTmp == null)
                {
                    return Result.TRANSPORTED;
                }
                else
                {
                    itemsIn.stackSize = stackTmp.stackSize;
                }
            }
        }

        // Pickup-to-inventory filtering enabled
        if (getSettingValue(manager, TAG_NAME_INVFILTER_ENABLED) != 0)
        {
            boolean ignoreMeta = getSettingValue(manager, TAG_NAME_INVFILTER_META) != 0;
            boolean ignoreNbt = getSettingValue(manager, TAG_NAME_INVFILTER_NBT) == 0;
            boolean match = InventoryUtils.matchingStackFoundInSlotRange(inv, INVENTORY_FILTER_SLOTS, itemsIn, ignoreMeta, ignoreNbt);

            // 0 = Black list
            byte mode = getSettingValue(manager, TAG_NAME_INVFILTER_MODE);

            // White list
            if (mode != 0)
            {
                return match == true ? Result.WHITELISTED : Result.NOT_WHITELISTED;
            }
            // Black list
            else if (mode == 0)
            {
                return match == true ? Result.BLACKLISTED : Result.NOT_BLACKLISTED;
            }
        }

        return Result.NOT_HANDLED;
    }

    /**
     * Try to handle the items being picked up.
     * @param event
     * @return false to prevent further processing of the event
     */
    public static boolean onItemPickupEvent(PlayerItemPickupEvent event)
    {
        if (event.entityPlayer.worldObj.isRemote == true)
        {
            return true;
        }

        EntityPlayer player = event.entityPlayer;
        List<ItemStack> managers = getEnabledItems(player);
        boolean deny = managers.size() > 0;
        boolean blackListed = false;
        boolean transported = false;

        Iterator<ItemStack> iter = event.drops.iterator();

        while (iter.hasNext() == true)
        {
            ItemStack stackIn = iter.next();

            //int i = 0;
            for (ItemStack manager : managers)
            {
                Result result = ((ItemPickupManager)manager.getItem()).handleItems(player, manager, stackIn);

                //System.out.println("i: " + i++ + " result: " + result);
                // Blacklisted or successfully transported, cancel further processing
                if (result == Result.BLACKLISTED)
                {
                    blackListed = true;
                    deny = true;
                    break;
                }
                else if (result == Result.TRANSPORTED)
                {
                    iter.remove();
                    transported = true;
                    break;
                }
                // Whitelisted, no need to check any further managers, just allow picking it up
                else if (result == Result.WHITELISTED && blackListed == false)
                {
                    deny = false;
                    break;
                }

                // Filters disabled or filtering mode was black list, and the item was not on the black list => allow through
                if (blackListed == false && (result == Result.NOT_HANDLED || result == Result.NOT_BLACKLISTED))
                {
                    deny = false;
                }
            }
        }

        // At least some items were picked up
        if (transported == true)
        {
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        if (deny == true)
        {
            event.setCanceled(true);
        }

        return deny == false;
    }

    /**
     * Try to handle the items being picked up.
     * @param event
     * @return false to prevent further processing of the event
     */
    public static boolean onEntityItemPickupEvent(EntityItemPickupEvent event)
    {
        if (event.entityPlayer.worldObj.isRemote == true || event.item.isDead == true ||
            event.item.getEntityItem() == null || event.item.getEntityItem().getItem() == null)
        {
            return true;
        }

        ItemStack stack = event.item.getEntityItem();
        int origStackSize = stack.stackSize;
        EntityPlayer player = event.entityPlayer;
        List<ItemStack> managers = getEnabledItems(player);
        // If there are enabled managers in the player's inventory, then initialize to "deny"
        boolean deny = managers.size() > 0;

        //int i = 0;
        for (ItemStack manager : managers)
        {
            Result result = ((ItemPickupManager)manager.getItem()).handleItems(player, manager, stack);

            //System.out.println("i: " + i++ + " result: " + result);
            // Blacklisted or successfully transported, cancel further processing
            if (result == Result.BLACKLISTED || result == Result.TRANSPORTED)
            {
                if (result == Result.TRANSPORTED)
                {
                    event.item.setDead();
                }

                deny = true;
                break;
            }

            // Whitelisted, no need to check any further managers, just allow picking it up
            if (result == Result.WHITELISTED)
            {
                deny = false;
                break;
            }

            // Filters disabled or filtering mode was black list, and the item was not on the black list => allow through
            if (result == Result.NOT_HANDLED || result == Result.NOT_BLACKLISTED)
            {
                deny = false;
            }
        }

        // At least some items were picked up
        if (stack.stackSize != origStackSize || event.item.isDead == true)
        {
            if (event.item.isSilent() == false)
            {
                player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            if (stack.stackSize <= 0 || event.item.isDead == true)
            {
                FMLCommonHandler.instance().firePlayerItemPickupEvent(player, event.item);
                player.onItemPickup(event.item, origStackSize);
            }
        }

        if (deny == true)
        {
            event.setCanceled(true);
        }

        return deny == false;
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
            UtilItemModular.changePrivacyModeOnSelectedModuleAbs(stack, player, ModuleType.TYPE_LINKCRYSTAL);
        }
        // Just Toggle mode: Toggle locked state
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED);
        }
        // Shift + Toggle mode: Cycle the slot mask preset
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1,
                    ReferenceKeys.keypressActionIsReversed(key));
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Link Crystal
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL,
                    ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if ((player.openContainer instanceof ContainerPickupManager) == false)
        {
            return;
        }

        ContainerPickupManager container = (ContainerPickupManager)player.openContainer;
        ItemStack stack = container.getModularItem();
        if (stack == null || (stack.getItem() instanceof ItemPickupManager) == false)
        {
            return;
        }

        if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < 3)
        {
            UtilItemModular.setModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL, element);
            ((ItemPickupManager) stack.getItem()).tryTransportItemsFromTransportSlot(container.inventoryItemTransmit, player, stack);
        }
        else if (action == GUI_ACTION_CHANGE_PRESET && element >= 0 && element < NUM_PRESETS)
        {
            NBTUtils.setByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, (byte)element);
            container.inventoryItemFilters.setItemStorageTagName("FilterItems_" + element);
            container.inventoryItemFilters.readFromContainerItemStack();
        }
        else if (action == GUI_ACTION_TOGGLE_INVENTORY_SETTINGS && element >= 0 && element < 4)
        {
            String tagName;
            if (element == 0) tagName = TAG_NAME_INVFILTER_ENABLED;
            else if (element == 1) tagName = TAG_NAME_INVFILTER_MODE;
            else if (element == 2) tagName = TAG_NAME_INVFILTER_META;
            else tagName = TAG_NAME_INVFILTER_NBT;

            setSettingValue(stack, tagName, (byte)(getSettingValue(stack, tagName) ^ 0x1));
        }
        else if (action == GUI_ACTION_TOGGLE_TRANSPORT_SETTINGS && element >= 0 && element < 4)
        {
            String tagName;
            if (element == 0) tagName = TAG_NAME_TXFILTER_ENABLED;
            else if (element == 1) tagName = TAG_NAME_TXFILTER_MODE;
            else if (element == 2) tagName = TAG_NAME_TXFILTER_META;
            else tagName = TAG_NAME_TXFILTER_NBT;

            setSettingValue(stack, tagName, (byte)(getSettingValue(stack, tagName) ^ 0x1));
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
        if (ModuleType.TYPE_LINKCRYSTAL.equals(moduleType))
        {
            return 3;
        }

        if (ModuleType.TYPE_ENDERCAPACITOR.equals(moduleType))
        {
            return 1;
        }

        return 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule)
        {
            IModule imodule = (IModule)moduleStack.getItem();
            ModuleType moduleType = imodule.getModuleType(moduleStack);

            if (ModuleType.TYPE_LINKCRYSTAL.equals(moduleType))
            {
                if (imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
                {
                    return this.getMaxModules(containerStack, ModuleType.TYPE_LINKCRYSTAL);
                }
            }
            else
            {
                return this.getMaxModules(containerStack, moduleType);
            }
        }

        return 0;
    }

    public enum Result
    {
        TRANSPORTED,
        WHITELISTED,
        NOT_WHITELISTED,
        BLACKLISTED,
        NOT_BLACKLISTED,
        NOT_HANDLED;
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
