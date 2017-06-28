package fi.dy.masa.enderutilities.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.inventory.container.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.container.base.SlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote == false)
        {
            // These two lines are to fix the UUID being missing the first time the GUI opens,
            // if the item is grabbed from the creative inventory or from JEI or from /give
            NBTUtils.getUUIDFromItemStack(stack, "UUID", true);
            player.openContainer.detectAndSendChanges();

            player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, world,
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
    public String getItemStackDisplayName(ItemStack stack)
    {
        int preset = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        int lc = UtilItemModular.getStoredModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL);
        String pre = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();
        String str = " - pre: " + pre + (preset + 1) + rst + " LC: " + pre + lc + rst;
        String target = this.getTargetDisplayName(stack);

        if (target != null)
        {
            str = str + " - " + pre + target + rst;
        }

        return this.getBaseItemDisplayName(stack) + str;
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
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        String str;

        if (isEnabled(containerStack))
        {
            str = I18n.format("enderutilities.tooltip.item.enabled") + ": " + preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str = I18n.format("enderutilities.tooltip.item.enabled") + ": " + preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
        }

        list.add(str);

        int preset = NBTUtils.getByte(containerStack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION) + 1;
        list.add(I18n.format("enderutilities.tooltip.item.preset") + ": " + TextFormatting.BLUE.toString() + preset + rst);

        super.addInformationSelective(containerStack, player, list, advancedTooltips, verbose);
    }

    public static NBTTagCompound getSelectedPresetTag(ItemStack stack, boolean create)
    {
        if (stack.isEmpty())
        {
            return null;
        }

        NBTTagCompound containerTag = NBTUtils.getCompoundTag(stack, TAG_NAME_CONTAINER, create);

        if (containerTag != null)
        {
            int selection = containerTag.getByte(TAG_NAME_PRESET_SELECTION);

            if (containerTag.hasKey(TAG_NAME_PRESET + selection, Constants.NBT.TAG_COMPOUND))
            {
                return containerTag.getCompoundTag(TAG_NAME_PRESET + selection);
            }
            else if (create)
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
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.PICKUP_MANAGER);
        List<ItemStack> enabledItems = new ArrayList<ItemStack>();

        for (int slot : slots)
        {
            ItemStack stack = playerInv.getStackInSlot(slot);

            if (isEnabled(stack))
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
        return items.size() > 0 ? items.get(0) : ItemStack.EMPTY;
    }

    public boolean tryTransportItemsFromTransportSlot(InventoryItem inv, EntityPlayer player, ItemStack manager)
    {
        ItemStack stack = inv.getStackInSlot(0);

        if (stack.isEmpty())
        {
            return false;
        }

        int sizeOrig = stack.getCount();
        int max = stack.getMaxStackSize();
        stack = inv.extractItem(0, max, false);

        while (stack.isEmpty() == false)
        {
            stack = this.tryTransportItems(player, manager, stack);

            // Could not transport the whole stack (anymore)
            if (stack.isEmpty() == false)
            {
                inv.insertItem(0, stack, false);
                break;
            }

            stack = inv.extractItem(0, max, false);
        }

        stack = inv.getStackInSlot(0);

        return stack.isEmpty() || stack.getCount() != sizeOrig;
    }

    public ItemStack tryTransportItems(EntityPlayer player, ItemStack manager, ItemStack itemsIn)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(manager, ModuleType.TYPE_LINKCRYSTAL);

        if (moduleStack.isEmpty() || itemsIn.isEmpty())
        {
            return itemsIn;
        }

        OwnerData owner = OwnerData.getOwnerDataFromItem(moduleStack);

        if (owner != null && owner.canAccess(player) == false)
        {
            return itemsIn;
        }

        TargetData target = TargetData.getTargetFromItem(moduleStack);

        if (target != null)
        {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(target.dimension);

            // Force load the target chunk with a 30 second unload delay.
            if (world == null || ChunkLoading.getInstance().loadChunkForcedWithModTicket(target.dimension,
                    target.pos.getX() >> 4, target.pos.getZ() >> 4, 30) == false)
            {
                return itemsIn;
            }

            TileEntity te = world.getTileEntity(target.pos);

            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing))
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
                if (UtilItemModular.useEnderCharge(manager, cost * itemsIn.getCount(), true) == false)
                {
                    int available = UtilItemModular.getAvailableEnderCharge(manager);

                    if (available < cost)
                    {
                        return itemsIn;
                    }

                    stackToSend.setCount(Math.min(itemsIn.getCount(), available / cost));
                }

                int numTransported = stackToSend.getCount();
                ItemStack itemsRemaining = InventoryUtils.tryInsertItemStackToInventory(inv, stackToSend);

                if (itemsRemaining.isEmpty() == false)
                {
                    numTransported -= itemsRemaining.getCount();
                }

                itemsIn.shrink(numTransported);

                // Get the final charge amount
                UtilItemModular.useEnderCharge(manager, numTransported * cost, false);

                if (itemsIn.isEmpty())
                {
                    return ItemStack.EMPTY;
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

        InventoryItem inv = new InventoryItem(manager, 36, 1, false, player.getEntityWorld().isRemote, TAG_NAME_FILTER_INVENTORY_PRE + preset);
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
            if ((mode != 0 && match) || (mode == 0 && match == false))
            {
                ItemStack stackTmp = this.tryTransportItems(player, manager, itemsIn);

                // All items successfully transported
                if (stackTmp.isEmpty())
                {
                    return Result.TRANSPORTED;
                }
                else
                {
                    itemsIn.setCount(stackTmp.getCount());
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
                return match ? Result.WHITELISTED : Result.NOT_WHITELISTED;
            }
            // Black list
            else if (mode == 0)
            {
                return match ? Result.BLACKLISTED : Result.NOT_BLACKLISTED;
            }
        }

        return Result.NOT_HANDLED;
    }

    /**
     * Try to handle the items being picked up.
     * @param event
     * @return true to prevent further processing of the event
     */
    public static boolean onItemPickupEvent(PlayerItemPickupEvent event)
    {
        if (event.getEntityPlayer().getEntityWorld().isRemote)
        {
            return false;
        }

        EntityPlayer player = event.getEntityPlayer();
        List<ItemStack> managers = getEnabledItems(player);
        boolean deny = managers.size() > 0;
        boolean blackListed = false;
        boolean transported = false;

        Iterator<ItemStack> iter = event.drops.iterator();

        while (iter.hasNext())
        {
            ItemStack stackIn = iter.next();

            if (stackIn.isEmpty())
            {
                continue;
            }

            //int i = 0;
            for (ItemStack manager : managers)
            {
                Result result = ((ItemPickupManager) manager.getItem()).handleItems(player, manager, stackIn);

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
        if (transported)
        {
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.2F,
                    ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        if (deny)
        {
            event.setCanceled(true);
        }

        return deny;
    }

    /**
     * Try to handle the items being picked up.
     * @param event
     * @return true to prevent further processing of the event
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
        List<ItemStack> managers = getEnabledItems(player);
        // If there are enabled managers in the player's inventory, then initialize to "deny"
        boolean deny = managers.size() > 0;

        //int i = 0;
        for (ItemStack manager : managers)
        {
            Result result = ((ItemPickupManager) manager.getItem()).handleItems(player, manager, stack);

            //System.out.println("i: " + i++ + " result: " + result);
            // Blacklisted or successfully transported, cancel further processing
            if (result == Result.BLACKLISTED || result == Result.TRANSPORTED)
            {
                if (result == Result.TRANSPORTED)
                {
                    entityItem.setDead();
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
        if (stack.getCount() != origStackSize || entityItem.isDead)
        {
            if (entityItem.isSilent() == false)
            {
                player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.2F,
                        ((player.getEntityWorld().rand.nextFloat() - player.getEntityWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            if (stack.isEmpty() || entityItem.isDead)
            {
                FMLCommonHandler.instance().firePlayerItemPickupEvent(player, entityItem);
                player.onItemPickup(entityItem, origStackSize);
            }
        }

        if (deny)
        {
            event.setCanceled(true);
        }

        return deny;
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode: Toggle locked state
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED);
        }
        // Shift + Toggle mode: Cycle the slot mask preset
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT))
        {
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1,
                    EnumKey.keypressActionIsReversed(key));
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if ((player.openContainer instanceof ContainerPickupManager) == false)
        {
            return;
        }

        ContainerPickupManager container = (ContainerPickupManager)player.openContainer;
        ItemStack stack = container.getContainerItem();

        if (stack.isEmpty() || (stack.getItem() instanceof ItemPickupManager) == false)
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
