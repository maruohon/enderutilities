package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemHandyBag extends ItemInventoryModular
{
    public static final int DAMAGE_TIER_1 = 0;
    public static final int DAMAGE_TIER_2 = 1;

    public static final int INV_SIZE_TIER_1 = 27;
    public static final int INV_SIZE_TIER_2 = 59;

    public static final int MAX_STACKSIZE_TIER_1 =   256;
    public static final int MAX_STACKSIZE_TIER_2 =  4096;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_QUICK_STACK   = 1;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemHandyBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_HANDY_BAG);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName() + "." + stack.getItemDamage();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);
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
        int installed = this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (installed > 0)
        {
            int slotNum = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD);
            String preBlue = EnumChatFormatting.BLUE.toString();
            String preWhiteIta = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
            String strShort = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.short");
            ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(containerStack, slotNum, ModuleType.TYPE_MEMORY_CARD);

            int max = this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD);
            //list.add(String.format("%s %s%s%s (%d / %d)%s", str, preWhite, name, rst, slotNum, max, strNo));

            if (moduleStack != null && moduleStack.getItem() == EnderUtilitiesItems.enderPart)
            {
                String dName = (moduleStack.hasDisplayName() ? preWhiteIta + moduleStack.getDisplayName() + rst + " " : "");
                list.add(String.format("%s %s(%s%d%s / %s%d%s)", strShort, dName, preBlue, slotNum + 1, rst, preBlue, max, rst));

                ItemEnderPart module = (ItemEnderPart)moduleStack.getItem();
                module.addInformationSelective(moduleStack, player, list, advancedTooltips, false);
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
    public int getSizeInventory(ItemStack containerStack)
    {
        return containerStack.getItemDamage() == DAMAGE_TIER_2 ? INV_SIZE_TIER_2 : INV_SIZE_TIER_1;
    }

    @Override
    public int getInventoryStackLimit(ItemStack containerStack)
    {
        return containerStack.getItemDamage() == DAMAGE_TIER_2 ? 64 : 16; // FIXME testing, MAX_STACKSIZE_TIER_2 : MAX_STACKSIZE_TIER_1;
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerHandyBag)
        {
            ContainerHandyBag container = (ContainerHandyBag)player.openContainer;
            ItemStack containerStack = container.inventory.getContainerItemStack();
            if (containerStack != null && containerStack.getItem() == EnderUtilitiesItems.handyBag)
            {
                int max = ((ItemHandyBag)containerStack.getItem()).getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD);
                // Changing the selected module via the GUI buttons
                if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < max)
                {
                    UtilItemModular.setModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD, element);
                    container.inventory.updateContainerItems();
                }
                else if (action == GUI_ACTION_QUICK_STACK)
                {
                }
            }
        }
    }

    public int getModeByName(ItemStack stack, String name)
    {
        if (stack.getTagCompound() == null || stack.getTagCompound().hasKey("HandyBag", Constants.NBT.TAG_COMPOUND) == false)
        {
            return 0;
        }

        return stack.getTagCompound().getCompoundTag("HandyBag").getByte(name);
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
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
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
        return moduleType.equals(ModuleType.TYPE_MEMORY_CARD) ? 4 : 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule)
        {
            IModule imodule = (IModule)moduleStack.getItem();

            if (imodule.getModuleType(moduleStack).equals(ModuleType.TYPE_MEMORY_CARD))
            {
                int tier = imodule.getModuleTier(moduleStack);
                if (tier == ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_8B ||
                    tier == ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_10B ||
                    tier == ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B)
                {
                    return 4;
                }
            }
        }

        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        list.add(new ItemStack(this, 1, 0)); // Tier 1
        list.add(new ItemStack(this, 1, 1)); // Tier 2
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
        return 4;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".0");
        this.iconArray = new IIcon[7];

        this.iconArray[0] = iconRegister.registerIcon(this.getIconString() + ".0");
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".1");
        this.iconArray[2] = iconRegister.registerIcon(ReferenceTextures.getItemTextureName("empty"));

        // Overlay textures
        this.iconArray[3] = iconRegister.registerIcon(this.getIconString() + ".overlay.locked");
        this.iconArray[4] = iconRegister.registerIcon(this.getIconString() + ".overlay.pickup.matching");
        this.iconArray[5] = iconRegister.registerIcon(this.getIconString() + ".overlay.pickup.all");
        this.iconArray[6] = iconRegister.registerIcon(this.getIconString() + ".overlay.restock");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        switch (renderPass)
        {
            case 0: // Main texture
                int damage = stack.getItemDamage();
                return this.iconArray[damage <= 1 ? damage : 0];
            case 1: // Locked icon
                return bagIsOpenable(stack) ? this.iconArray[2] : this.iconArray[3];
            case 2: // Pickup mode; 0: None, 1: Matching, 2: All
                int index = this.getModeByName(stack, "PickupMode");
                if (index == 1 || index == 2)
                {
                    return this.iconArray[index - 1 + 4];
                }
                return this.iconArray[2]; // empty
            case 3:
                return this.getModeByName(stack, "RestockMode") != 0 ? this.iconArray[6] : this.iconArray[2];
        }

        return this.itemIcon;
    }
}
