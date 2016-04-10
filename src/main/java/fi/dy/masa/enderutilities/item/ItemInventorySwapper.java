package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ItemInventorySwapper extends ItemInventoryModular implements IKeyBound, IKeyBoundUnselected
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
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
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

        String str;
        if (isEnabled(containerStack) == true)
        {
            str = I18n.translateToLocal("enderutilities.tooltip.item.enabled") + ": " +
                    preGreen + I18n.translateToLocal("enderutilities.tooltip.item.yes");
        }
        else
        {
            str = I18n.translateToLocal("enderutilities.tooltip.item.enabled") + ": " +
                    preRed + I18n.translateToLocal("enderutilities.tooltip.item.no");
        }
        list.add(str);

        byte selected = NBTUtils.getByte(containerStack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION);
        list.add(I18n.translateToLocal("enderutilities.tooltip.item.preset") + ": " + preBlue + (selected + 1) + rst);

        int installed = this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (installed > 0)
        {
            int slotNum = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
            String preWhiteIta = preWhite + TextFormatting.ITALIC.toString();
            String strShort = I18n.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.short");
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
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.nomemorycards"));
        }
    }

    @Override
    public int getSizeInventory(ItemStack containerStack)
    {
        return 40;
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
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(new PlayerMainInvWrapper(player.inventory), EnderUtilitiesItems.inventorySwapper);
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

    public void swapInventory(long slotMask, InventoryItemModular swapperInv, IItemHandler externalInv)
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

    public void swapInventory(ItemStack swapperStack, IItemHandler inv, EntityPlayer player)
    {
        InventoryItemModular swapperInv = new InventoryItemModular(swapperStack, player, false, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (swapperInv.isUseableByPlayer(player) == false)
        {
            return;
        }

        this.swapInventory(getEnabledSlotsMask(swapperStack), swapperInv, inv);

        player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
    }

    public static void swapPlayerInventory(final int swapperSlot, EntityPlayer player)
    {
        ItemStack swapperStack = player.inventory.getStackInSlot(swapperSlot);
        if (swapperStack == null)
        {
            return;
        }

        InventoryItemModular inv = new InventoryItemModular(swapperStack, player, false, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        if (inv.isUseableByPlayer(player) == false)
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
                    if (slot >= mainInvSize && slot < player.inventory.getSizeInventory())
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
                    else if (slot < mainInvSize)
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

    public static void swapPlayerInventory(EntityPlayer player)
    {
        int slot = getSlotContainingEnabledItem(player);
        if (slot != -1)
        {
            swapPlayerInventory(slot, player);
        }
    }

    @Override
    public void doUnselectedKeyAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode: Fire the swapping action
        /*if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            swapInventory(player);
        }*/

        // Re-fetch the item to check if it's enabled
        stack = getEnabledItem(player);

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
            UtilItemModular.changePrivacyModeOnSelectedModuleAbs(stack, player, ModuleType.TYPE_MEMORY_CARD_ITEMS);
        }
        // Just Toggle mode: Fire the swapping action
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            swapPlayerInventory(player);
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
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1,
                    ReferenceKeys.keypressActionIsReversed(key));
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS,
                    ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerInventorySwapper)
        {
            ItemStack stack = ((ContainerInventorySwapper)player.openContainer).getModularItem();
            if (stack != null && stack.getItem() == EnderUtilitiesItems.inventorySwapper)
            {
                int max = ((ItemInventorySwapper)stack.getItem()).getMaxModules(stack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
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
                    long mask = getEnabledSlotsMask(stack);
                    mask ^= (0x1FFL << (element * 9));
                    NBTUtils.setLong(stack, TAG_NAME_CONTAINER,
                            TAG_NAME_PRESET + NBTUtils.getByte(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION), mask);
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
