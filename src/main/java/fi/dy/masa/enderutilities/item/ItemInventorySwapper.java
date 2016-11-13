package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
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
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemInventorySwapper extends ItemInventoryModular implements IKeyBound, IKeyBoundUnselected
{
    public static final String TAG_NAME_CONTAINER = "InventorySwpapper";
    public static final String TAG_NAME_PRESET_SELECTION = "SelectedPreset";
    public static final String TAG_NAME_PRESET = "Preset_";
    public static final String TAG_NAME_LOCKED = "Locked";
    public static final String TAG_NAME_CYCLE_MODE = "Cycle";

    public static final int NUM_PRESETS = 4;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_CHANGE_PRESET = 1;
    public static final int GUI_ACTION_TOGGLE_ROWS = 2;
    public static final int GUI_ACTION_TOGGLE_COLUMNS = 3;

    public ItemInventorySwapper()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_INVENTORY_SWAPPER);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote == false)
        {
            // These two lines are to fix the UUID being missing the first time the GUI opens,
            // if the item is grabbed from the creative inventory or from JEI or from /give
            NBTUtils.getUUIDFromItemStack(stack, "UUID", true);
            player.openContainer.detectAndSendChanges();

            player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking() == true)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) == true)
            {
                IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                if (world.isRemote == false && inv != null)
                {
                    this.swapInventory(stack, inv, player);
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
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
        String itemName = super.getItemStackDisplayName(stack);
        String preGreenIta = TextFormatting.GREEN.toString() + TextFormatting.ITALIC.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        int slotNum = UtilItemModular.getStoredModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(stack, slotNum, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (moduleStack != null && moduleStack.getTagCompound() != null)
        {
            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
            {
                if (itemName.length() >= 14)
                {
                    itemName = EUStringUtils.getInitialsWithDots(itemName) + " " + preGreenIta + moduleStack.getDisplayName() + rst;
                }
                else
                {
                    itemName = itemName + " " + preGreenIta + moduleStack.getDisplayName() + rst;
                }
            }

            //return itemName + " " + pre + (NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION) + 1) + rst;
        }

        // Module not renamed, show the module index instead
        itemName = itemName + " MC: " + preGreen + (slotNum + 1) + rst;

        byte selected = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        itemName = itemName + " P: " + preGreen + (selected + 1) + rst;

        return itemName;
    }

    @Override
    public void addInformationSelective(ItemStack containerStack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (containerStack.getTagCompound() == null)
        {
            return;
        }

        String preGreen = TextFormatting.GREEN.toString();
        String preBlue = TextFormatting.BLUE.toString();
        String preRed = TextFormatting.RED.toString();
        String preWhite = TextFormatting.WHITE.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
        String strYes = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
        String strNo = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;

        if (this.isEnabled(containerStack) == true)
        {
            list.add(I18n.format("enderutilities.tooltip.item.enabled") + ": " + strYes);
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.enabled") + ": " + strNo);
        }

        if (NBTUtils.getBoolean(containerStack, TAG_NAME_CONTAINER, TAG_NAME_CYCLE_MODE))
        {
            list.add(I18n.format("enderutilities.tooltip.item.cyclemode") + ": " + strYes);
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.cyclemode") + ": " + strNo);
        }

        byte selected = NBTUtils.getByte(containerStack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        list.add(I18n.format("enderutilities.tooltip.item.preset") + ": " + preBlue + (selected + 1) + rst);

        int installed = this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (installed > 0)
        {
            int slotNum = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
            String preWhiteIta = preWhite + TextFormatting.ITALIC.toString();
            String strShort = I18n.format("enderutilities.tooltip.item.selectedmemorycard.short");
            ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(containerStack, slotNum, ModuleType.TYPE_MEMORY_CARD_ITEMS);
            int max = this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);

            if (moduleStack != null && moduleStack.getItem() == EnderUtilitiesItems.enderPart)
            {
                String dName = (moduleStack.hasDisplayName() ? preWhiteIta + moduleStack.getDisplayName() + rst + " " : "");
                list.add(String.format("%s %s (%s%d%s / %s%d%s)", strShort, dName, preBlue, slotNum + 1, rst, preBlue, max, rst));

                ((ItemEnderPart)moduleStack.getItem()).addInformationSelective(moduleStack, player, list, advancedTooltips, false);
                return;
            }
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.nomemorycards"));
        }
    }

    @Override
    public int getSizeInventory(ItemStack containerStack)
    {
        return 41;
    }

    public boolean isEnabled(ItemStack stack)
    {
        return NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_LOCKED) == false;
    }

    /**
     * Returns the slot number of the first enabled/usable Inventory Swapper in the player's inventory, or -1 if none is found.
     */
    private int getSlotContainingEnabledItem(EntityPlayer player)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(playerInv, EnderUtilitiesItems.inventorySwapper);
        for (int slot : slots)
        {
            if (this.isEnabled(playerInv.getStackInSlot(slot)) == true)
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns an ItemStack containing an enabled Inventory Swapper in the player's inventory, or null if none is found.
     */
    private ItemStack getEnabledItem(EntityPlayer player)
    {
        int slotNum = this.getSlotContainingEnabledItem(player);
        return slotNum != -1 ? player.inventory.getStackInSlot(slotNum) : null;
    }

    public long getEnabledSlotsMask(ItemStack stack)
    {
        byte selected = NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        return NBTUtils.getLong(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET + selected);
    }

    private void swapInventory(long slotMask, InventoryItemModular swapperInv, IItemHandler externalInv)
    {
        // Only swap up to 36 slots (which fit in the swapper's GUI, excluding armor slots)
        final int invSize = Math.min(36, externalInv.getSlots());

        long bit = 0x1;
        for (int i = 0; i < invSize; i++)
        {
            // Only swap slots that have been enabled
            if ((slotMask & bit) != 0)
            {
                ItemStack stackSwapper = swapperInv.extractItem(i, 64, false);
                ItemStack stackExternal = externalInv.extractItem(i, 64, false);

                // Check that both stacks can be successfully inserted into the other inventory
                if (swapperInv.insertItem(i, stackExternal, true) == null && externalInv.insertItem(i, stackSwapper, true) == null)
                {
                    swapperInv.insertItem(i, stackExternal, false);
                    externalInv.insertItem(i, stackSwapper, false);
                }
                // Can't swap the stacks, return them to the original inventories
                else
                {
                    swapperInv.insertItem(i, stackSwapper, false);
                    externalInv.insertItem(i, stackExternal, false);
                }
            }

            bit <<= 1;
        }
    }

    private void swapInventory(ItemStack swapperStack, IItemHandler inv, EntityPlayer player)
    {
        InventoryItemModular swapperInv = new InventoryItemModular(swapperStack, player, false, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (swapperInv.isAccessibleByPlayer(player) == false)
        {
            return;
        }

        this.swapInventory(getEnabledSlotsMask(swapperStack), swapperInv, inv);

        player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
    }

    private void swapPlayerInventory(final int swapperSlot, EntityPlayer player)
    {
        ItemStack swapperStack = player.inventory.getStackInSlot(swapperSlot);
        if (swapperStack == null)
        {
            return;
        }

        InventoryItemModular inv = new InventoryItemModular(swapperStack, player, false, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (inv.isAccessibleByPlayer(player) == false)
        {
            return;
        }

        final long mask = getEnabledSlotsMask(swapperStack);
        final int invMax = player.inventory.getInventoryStackLimit();
        final int invSize = player.inventory.getSizeInventory();
        final int mainInvSize = player.inventory.mainInventory.length;

        long bit = 0x1;
        for (int slot = 0; slot < invSize; slot++)
        {
            // Don't swap the swapper itself, and only swap slots that have been enabled
            if (slot != swapperSlot && (mask & bit) != 0)
            {
                ItemStack tmpStack = inv.getStackInSlot(slot);

                // Check if the stack from the swapper can fit and is valid to be put into the player's inventory
                if (tmpStack == null || (tmpStack.stackSize <= Math.min(tmpStack.getMaxStackSize(), invMax) &&
                        player.inventory.isItemValidForSlot(slot, tmpStack)))
                {
                    // Armor slots
                    if (slot >= mainInvSize && slot < (mainInvSize + 4))
                    {
                        int pos = -1;

                        // Armor present in the swappers's inventory slot, get the corresponding armor slot
                        if (tmpStack != null)
                        {
                            EntityEquipmentSlot equipmentSlot = EntityLiving.getSlotForItemStack(tmpStack);

                            if (tmpStack.stackSize == 1 && equipmentSlot.getSlotType() == EntityEquipmentSlot.Type.ARMOR)
                            {
                                pos = equipmentSlot.getIndex();
                            }
                        }
                        else if (player.inventory.getStackInSlot(slot) != null)
                        {
                            pos = slot - mainInvSize;
                        }

                        if (pos >= 0 && pos == (slot - mainInvSize))
                        {
                            inv.setStackInSlot(slot, player.inventory.getStackInSlot(slot));
                            player.inventory.setInventorySlotContents(slot, tmpStack);
                        }
                    }
                    // Main inventory and Off Hand slot
                    else
                    {
                        inv.setStackInSlot(slot, player.inventory.getStackInSlot(slot));
                        player.inventory.setInventorySlotContents(slot, tmpStack);
                    }
                }
            }
            bit <<= 1;
        }

        player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
    }

    private void swapPlayerInventory(EntityPlayer player)
    {
        int slot = this.getSlotContainingEnabledItem(player);

        if (slot != -1)
        {
            this.swapPlayerInventory(slot, player);
        }
    }

    private void cycleInventory(EntityPlayer player, boolean reverse)
    {
        int slot = this.getSlotContainingEnabledItem(player);

        if (slot != -1)
        {
            ItemStack stack = player.inventory.getStackInSlot(slot);

            if (this.getInstalledModuleCount(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS) > 1)
            {
                this.swapPlayerInventory(slot, player);
                this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS, reverse);
                this.swapPlayerInventory(slot, player);
            }
        }
    }

    @Override
    public void doUnselectedKeyAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Re-fetch the item to check if it's enabled
        stack = this.getEnabledItem(player);

        if (stack != null && stack.getItem() == EnderUtilitiesItems.inventorySwapper)
        {
            ((ItemInventorySwapper)stack.getItem()).doKeyBindingAction(player, stack, key);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode: Fire the swapping action
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            this.swapPlayerInventory(player);
        }
        // Alt + Toggle mode: Toggle the private/public mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_ALT))
        {
            UtilItemModular.changePrivacyModeOnSelectedModuleAbs(stack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        }
        // Alt + Shift + Toggle mode: Toggle the locked mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
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
        // Ctrl + Alt + Shift + Toggle: Toggle cycle mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_CTRL_ALT))
        {
            NBTUtils.toggleBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_CYCLE_MODE);
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card, or Cycle the inventory to the next card
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            if (NBTUtils.getBoolean(stack, TAG_NAME_CONTAINER, TAG_NAME_CYCLE_MODE))
            {
                this.cycleInventory(player, EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
            }
            else
            {
                this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
            }
        }
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerInventorySwapper)
        {
            ItemStack stack = ((ContainerInventorySwapper)player.openContainer).getContainerItem();
            if (stack != null && stack.getItem() == EnderUtilitiesItems.inventorySwapper)
            {
                ItemInventorySwapper swapper = (ItemInventorySwapper)stack.getItem();
                int max = swapper.getMaxModules(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);

                // Changing the selected module via the GUI buttons
                if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < max)
                {
                    UtilItemModular.setModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS, element);
                    ((ContainerInventorySwapper)player.openContainer).inventoryItemModular.readFromContainerItemStack();
                }
                else if (action == GUI_ACTION_CHANGE_PRESET && element >= 0 && element < NUM_PRESETS)
                {
                    NBTUtils.setByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, (byte)element);
                }
                else if (action == GUI_ACTION_TOGGLE_ROWS && element >= 0 && element < 4)
                {
                    long mask = swapper.getEnabledSlotsMask(stack);
                    mask ^= (0x1FFL << (element * 9));
                    NBTUtils.setLong(stack, TAG_NAME_CONTAINER,
                            TAG_NAME_PRESET + NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION), mask);
                }
                else if (action == GUI_ACTION_TOGGLE_COLUMNS)
                {
                    long mask = swapper.getEnabledSlotsMask(stack);

                    // Player inventory
                    if (element >= 0 && element < 9)
                    {
                        mask ^= (0x08040201L << element); // toggle the bits for the slots in the selected column of the inventory
                    }
                    // Armor slots
                    else if (element == 9)
                    {
                        mask ^= 0x1F000000000L; // toggle bits 40..36
                    }

                    NBTUtils.setLong(stack, TAG_NAME_CONTAINER,
                            TAG_NAME_PRESET + NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION), mask);
                }
            }
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

            if (imodule.getModuleType(moduleStack).equals(ModuleType.TYPE_MEMORY_CARD_ITEMS) &&
                imodule.getModuleTier(moduleStack) == ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B)
            {
                return this.getMaxModules(containerStack);
            }
        }

        return 0;
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
