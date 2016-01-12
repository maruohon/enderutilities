package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerPickupManager;
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
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemPickupManager extends ItemLocationBoundModular implements IKeyBound
{
    public static final String TAG_NAME_CONTAINER = "PickupManager";
    public static final String TAG_NAME_PRESET_SELECTION = "SelectedPreset";
    public static final String TAG_NAME_PRESET = "Preset_";
    public static final String TAG_NAME_LOCKED = "Locked";

    public static final String TAG_NAME_INFILTER_ENABLED = "InFilterEnabled";
    public static final String TAG_NAME_INFILTER_MODE = "InFilterMode";
    public static final String TAG_NAME_INFILTER_META = "InFilterMeta";
    public static final String TAG_NAME_INFILTER_NBT = "InFilterNBT";

    public static final String TAG_NAME_TXFILTER_ENABLED = "TxFilterEnabled";
    public static final String TAG_NAME_TXFILTER_MODE = "TxFilterMode";
    public static final String TAG_NAME_TXFILTER_META = "TxFilterMeta";
    public static final String TAG_NAME_TXFILTER_NBT = "TxFilterNBT";

    public static final int NUM_PRESETS = 4;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_CHANGE_PRESET = 1;
    public static final int GUI_ACTION_TOGGLE_INPUT_SETTINGS = 2;
    public static final int GUI_ACTION_TOGGLE_TRANSPORT_SETTINGS = 3;

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
        String itemName = super.getItemStackDisplayName(stack);
        String pre = EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
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

            int index = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL);
            return itemName + " " + pre + (index + 1) + rst;
        }

        return itemName;
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

        super.addInformationSelective(containerStack, player, list, advancedTooltips, verbose);
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
     * Returns the slot number of the first enabled/usable Inventory Swapper in the player's inventory, or -1 if none is found.
     */
    public static int getSlotContainingEnabledItem(EntityPlayer player)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.pickupManager);
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
            UtilItemModular.changePrivacyModeOnSelectedModule(stack, player, ModuleType.TYPE_MEMORY_CARD);
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
            NBTUtils.cycleByteValue(stack, TAG_NAME_CONTAINER, TAG_NAME_PRESET_SELECTION, NUM_PRESETS - 1);
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            UtilItemModular.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
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
        else if (action == GUI_ACTION_TOGGLE_INPUT_SETTINGS && element >= 0 && element < 4)
        {
            String tagName;
            if (element == 0) tagName = TAG_NAME_INFILTER_ENABLED;
            else if (element == 1) tagName = TAG_NAME_INFILTER_MODE;
            else if (element == 2) tagName = TAG_NAME_INFILTER_META;
            else tagName = TAG_NAME_INFILTER_NBT;

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
}
