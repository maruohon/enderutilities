package fi.dy.masa.enderutilities.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemInventorySwapper extends ItemInventoryModular implements IKeyBound
{
    public static final String TAG_NAME_CONTAINER = "InventorySwpapper";
    public static final String TAG_NAME_PRESET_SELECTION = "SelectedPreset";
    public static final String TAG_NAME_PRESET = "Preset_";
    public static final String TAG_NAME_LOCKED = "Locked";

    public static final int NUM_PRESETS = 4;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_CHANGE_PRESET = 1;
    public static final int GUI_ACTION_TOGGLE_ROWS = 2;
    public static final int GUI_ACTION_TOGGLE_COLUMNS = 3;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemInventorySwapper()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_INVENTORY_SWAPPER);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        return stack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String itemName = super.getItemStackDisplayName(stack); //StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
        String pre = EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        int slotNum = UtilItemModular.getStoredModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD);
        ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(stack, slotNum, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null && moduleStack.getTagCompound() != null)
        {
            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
            {
                if (itemName.length() >= 14)
                {
                    return EUStringUtils.getInitialsWithDots(itemName) + " " + pre + moduleStack.getDisplayName() + rst;
                }

                return itemName + " " + pre + moduleStack.getDisplayName() + rst;
            }

            //return itemName + " " + pre + (NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION) + 1) + rst;
        }

        // Module not renamed, show the module index instead
        return itemName + " " + pre + (slotNum + 1) + rst;
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

        String str;
        if (isEnabled(containerStack) == true)
        {
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.enabled") + ": " + preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes");
        }
        else
        {
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.enabled") + ": " + preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no");
        }
        list.add(str);

        int installed = this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (installed > 0)
        {
            int slotNum = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD);
            String preBlue = EnumChatFormatting.BLUE.toString();
            String preWhiteIta = preWhite + EnumChatFormatting.ITALIC.toString();
            String strShort = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.short");
            ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(containerStack, slotNum, ModuleType.TYPE_MEMORY_CARD);
            int max = this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD);

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

    @Override
    public int getSizeModuleInventory(ItemStack containerStack)
    {
        return this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD);
    }

    @Override
    public int getSizeInventory(ItemStack containerStack)
    {
        return 40;
    }

    @Override
    public int getInventoryStackLimit(ItemStack containerStack)
    {
        return 64;
    }

    public static boolean isEnabled(ItemStack stack)
    {
        return NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED) == false;
    }

    /**
     * Returns the slot number of the first enabled/usable Inventory Swapper in the player's inventory, or -1 if none is found.
     */
    public static int getSlotContainingEnabledItem(EntityPlayer player)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.inventorySwapper);
        for (int slot : slots)
        {
            if (isEnabled(player.inventory.getStackInSlot(slot)) == true)
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns an ItemStack containing an enabled Inventory Swapper in the player's inventory, or null if none is found.
     */
    public static ItemStack getEnabledItem(EntityPlayer player)
    {
        int slotNum = getSlotContainingEnabledItem(player);
        return slotNum != -1 ? player.inventory.getStackInSlot(slotNum) : null;
    }

    public static long getEnabledSlotsMask(ItemStack stack)
    {
        byte selected = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        return NBTUtils.getLong(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET + selected);
    }

    public static void swapInventory(final int swapperSlot, EntityPlayer player)
    {
        ItemStack swapperStack = player.inventory.getStackInSlot(swapperSlot);
        ItemStack tmpStack;
        if (swapperStack == null)
        {
            return;
        }

        InventoryItemModular inv = new InventoryItemModular(swapperStack, player, ModuleType.TYPE_MEMORY_CARD);
        if (inv.getModuleInventory().getStackInSlot(UtilItemModular.getStoredModuleSelection(swapperStack, ModuleType.TYPE_MEMORY_CARD)) == null)
        {
            return;
        }

        final long mask = getEnabledSlotsMask(swapperStack);
        final int invMax = player.inventory.getInventoryStackLimit();
        final int invSize = player.inventory.getSizeInventory();
        final int mainInvSize = player.inventory.mainInventory.length;

        long bit = 0x1;
        for (int i = 0; i < invSize; i++)
        {
            // Don't swap the swapper itself, and only swap slots that have been enabled
            if (i != swapperSlot && (mask & bit) != 0)
            {
                tmpStack = inv.getStackInSlot(i);

                // Check if the stack from the swapper can fit and is valid to be put into the player's inventory
                if (tmpStack == null ||
                    (tmpStack.stackSize <= Math.min(tmpStack.getMaxStackSize(), invMax) &&
                        player.inventory.isItemValidForSlot(i, tmpStack)))
                {
                    // Armor slots
                    if (i >= mainInvSize)
                    {
                        int pos = tmpStack != null ? EntityLiving.getArmorPosition(tmpStack) : (i - mainInvSize + 1);
                        if (pos > 0 && pos == (i - mainInvSize + 1))
                        {
                            inv.setInventorySlotContents(i, player.inventory.getStackInSlot(i));
                            player.inventory.setInventorySlotContents(i, tmpStack);
                        }
                    }
                    else
                    {
                        inv.setInventorySlotContents(i, player.inventory.getStackInSlot(i));
                        player.inventory.setInventorySlotContents(i, tmpStack);
                    }
                }
            }
            bit <<= 1;
        }

        player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.2f, 1.8f);
    }

    public static void swapInventory(EntityPlayer player)
    {
        int slot = getSlotContainingEnabledItem(player);
        if (slot != -1)
        {
            swapInventory(slot, player);
        }
    }

    public static void handleKeyPressUnselected(EntityPlayer player, int key)
    {
        // Just Toggle mode: Fire the swapping action
        /*if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            swapInventory(player);
        }*/

        ItemStack stack = getEnabledItem(player);
        if (stack != null && stack.getItem() == EnderUtilitiesItems.inventorySwapper)
        {
            ((ItemInventorySwapper)stack.getItem()).doKeyBindingAction(player, stack, key);
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
            UtilItemModular.changePrivacyModeOnSelectedModuleAbs(stack, player, ModuleType.TYPE_MEMORY_CARD);
        }
        // Just Toggle mode: Fire the swapping action
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            swapInventory(player);
        }
        // Alt + Shift + Toggle mode: Toggle the locked mode
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == true)
        {
            NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED);
        }
        // Shift + Toggle mode: Cycle the slot mask preset
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1);
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            UtilItemModular.changeSelectedModuleAbs(stack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerInventorySwapper)
        {
            ItemStack stack = ((ContainerInventorySwapper)player.openContainer).getModularItem();
            if (stack != null && stack.getItem() == EnderUtilitiesItems.inventorySwapper)
            {
                int max = ((ItemInventorySwapper)stack.getItem()).getMaxModules(stack, ModuleType.TYPE_MEMORY_CARD);
                // Changing the selected module via the GUI buttons
                if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < max)
                {
                    UtilItemModular.setModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD, element);
                    ((ContainerInventorySwapper)player.openContainer).inventoryItemModular.readFromContainerItemStack();
                }
                else if (action == GUI_ACTION_CHANGE_PRESET && element >= 0 && element < NUM_PRESETS)
                {
                    NBTUtils.setByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, (byte)element);
                }
                else if (action == GUI_ACTION_TOGGLE_ROWS && element >= 0 && element < 4)
                {
                    long mask = getEnabledSlotsMask(stack);
                    mask ^= (0x1FFL << (element * 9));
                    NBTUtils.setLong(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET + NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION), mask);
                }
                else if (action == GUI_ACTION_TOGGLE_COLUMNS)
                {
                    long mask = getEnabledSlotsMask(stack);

                    // Player inventory
                    if (element >= 0 && element < 9)
                    {
                        mask ^= (0x08040201L << element); // toggle the bits for the slots in the selected column of the inventory
                    }
                    // Armor slots
                    else if (element == 9)
                    {
                        mask ^= 0xF000000000L; // toggle bits 39..36
                    }

                    NBTUtils.setLong(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET + NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION), mask);
                }
            }
        }
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        return moduleType.equals(ModuleType.TYPE_MEMORY_CARD) ? this.getMaxModules(containerStack) : 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule)
        {
            IModule imodule = (IModule)moduleStack.getItem();

            if (imodule.getModuleType(moduleStack).equals(ModuleType.TYPE_MEMORY_CARD) &&
                imodule.getModuleTier(moduleStack) == ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B)
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
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".overlay.locked");
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
}
