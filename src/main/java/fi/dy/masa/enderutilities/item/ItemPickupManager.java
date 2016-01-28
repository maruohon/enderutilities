package fi.dy.masa.enderutilities.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.inventory.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.InventoryItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.SlotRange;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
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

    public static final int NUM_PRESETS = 4;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_CHANGE_PRESET = 1;
    public static final int GUI_ACTION_TOGGLE_INVENTORY_SETTINGS = 2;
    public static final int GUI_ACTION_TOGGLE_TRANSPORT_SETTINGS = 3;

    public static final SlotRange TRANSPORT_FILTER_SLOTS = new SlotRange(0, 18);
    public static final SlotRange INVENTORY_FILTER_SLOTS = new SlotRange(18, 18);

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

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
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
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
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.pickupManager);
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

    public static boolean tryTransportItemsFromTransportSlot(InventoryItem inv, EntityPlayer player, ItemStack manager)
    {
        /*InventoryItem inv = new InventoryItem(manager, 1, player.worldObj.isRemote, player, TAG_NAME_TX_INVENTORY);
        inv.setInventoryStackLimit(1024);
        inv.readFromContainerItemStack();*/
        ItemStack stack = inv.getStackInSlot(0);
        if (stack == null)
        {
            return false;
        }

        int sizeOrig = stack.stackSize;
        int max = stack.getMaxStackSize();

        while (stack.stackSize > 0)
        {
            int size = Math.min(max, stack.stackSize);
            if (size <= 0)
            {
                break;
            }

            ItemStack stackTmp = stack.copy();
            stackTmp.stackSize = size;
            stackTmp = tryTransportItems(player, manager, stackTmp);

            // Could not transport he whole stack (anymore)
            if (stackTmp != null)
            {
                stack.stackSize -= (size - stackTmp.stackSize);
                break;
            }

            stack.stackSize -= size;
        }

        inv.setInventorySlotContents(0, stack.stackSize > 0 ? stack : null);

        return stack.stackSize != sizeOrig;
    }

    public static ItemStack tryTransportItems(EntityPlayer player, ItemStack manager, ItemStack itemsIn)
    {
        int index = UtilItemModular.getStoredModuleSelection(manager, ModuleType.TYPE_LINKCRYSTAL);
        ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(manager, index, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null)
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
            if (world != null)
            {
                // Force load the target chunk with a 30 second unload delay.
                if (ChunkLoading.getInstance().loadChunkForcedWithModTicket(target.dimension, target.posX >> 4, target.posZ >> 4, 30) == false)
                {
                    return itemsIn;
                }

                TileEntity te = world.getTileEntity(target.posX, target.posY, target.posZ);
                if (te instanceof IInventory)
                {
                    return InventoryUtils.tryInsertItemStackToInventory((IInventory)te, itemsIn, target.blockFace);
                }
            }
        }

        return itemsIn;
    }

    /**
     * Tries to handle the given ItemStack. Returns true if all items were handled and further processing should be canceled.
     */
    public static Result handleItems(EntityPlayer player, ItemStack manager, ItemStack itemsIn)
    {
        byte preset = NBTUtils.getByte(manager, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);

        InventoryItem inv = new InventoryItem(manager, 36, player.worldObj.isRemote, player, TAG_NAME_FILTER_INVENTORY_PRE + preset);
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
                // All items successfully transported
                ItemStack stackTmp = tryTransportItems(player, manager, itemsIn);
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
                Result result = handleItems(player, manager, stackIn);

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
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        if (deny == true)
        {
            event.setCanceled(true);
            return false;
        }

        return true;
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

        ItemStack stackIn = event.item.getEntityItem();
        int origStackSize = stackIn.stackSize;
        EntityPlayer player = event.entityPlayer;
        List<ItemStack> managers = getEnabledItems(player);
        boolean deny = managers.size() > 0;
        boolean ret = true;

        //int i = 0;
        for (ItemStack manager : managers)
        {
            Result result = handleItems(player, manager, stackIn);

            //System.out.println("i: " + i++ + " result: " + result);
            // Blacklisted or successfully transported, cancel further processing
            if (result == Result.BLACKLISTED || result == Result.TRANSPORTED)
            {
                if (result == Result.TRANSPORTED)
                {
                    event.item.setDead();
                }

                deny = true;
                ret = false;
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
        if (event.item.getEntityItem().stackSize != origStackSize)
        {
            FMLCommonHandler.instance().firePlayerItemPickupEvent(player, event.item);
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.onItemPickup(event.item, origStackSize);
        }

        if (deny == true)
        {
            event.setCanceled(true);
            return false;
        }

        return ret;
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
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1, ReferenceKeys.keypressActionIsReversed(key));
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
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
        if (stack == null)
        {
            return;
        }

        if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < 3)
        {
            UtilItemModular.setModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL, element);
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
        return 3;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        return moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) ? this.getMaxModules(containerStack) : 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule)
        {
            IModule imodule = (IModule)moduleStack.getItem();

            if (imodule.getModuleType(moduleStack).equals(ModuleType.TYPE_LINKCRYSTAL) &&
                imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
            {
                return this.getMaxModules(containerStack);
            }
        }

        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses(int metadata)
    {
        return 2;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString());
        this.iconArray = new IIcon[3];

        this.iconArray[0] = iconRegister.registerIcon(this.getIconString());

        // Overlay textures
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".locked");
        this.iconArray[2] = iconRegister.registerIcon(ReferenceTextures.getItemTextureName("empty"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        switch (renderPass)
        {
            case 0: // Main texture
                return this.iconArray[0];
            case 1: // Locked icon
                boolean isLocked = NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED);
                return isLocked == true ? this.iconArray[1] : this.iconArray[2];
        }

        return this.itemIcon;
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
}
