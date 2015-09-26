package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
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
    public static final int INV_SIZE_TIER_2 = 55;

    public static final int MAX_STACKSIZE_TIER_1 =   256;
    public static final int MAX_STACKSIZE_TIER_2 =  4096;

    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_MOVE_ITEMS    = 1;

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
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
        super.onUpdate(stack, world, entity, slot, isCurrent);

        // If Restock mode is enabled, then we will fill the stacks in the player's inventory from the bag
        if (world.isRemote == false && entity instanceof EntityPlayer && this.getModeByName(stack, "RestockMode") == 1)
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
                    inv = new InventoryItemModular(stack, (EntityPlayer)entity);
                }

                InventoryUtils.fillStacksOfMatchingItems(inv, player.inventory);
                inv.saveInventory();

                //if (player.openContainer instanceof ContainerHandyBag)
                {
                    player.openContainer.detectAndSendChanges();
                    player.inventory.markDirty();
                }
            }
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        // If the bag is sneak + right clicked on an inventory, then we try to dump all the contents to that inventory
        if (player.isSneaking() == true)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (world.isRemote == false && te != null && te instanceof IInventory)
            {
                InventoryItemModular inv = new InventoryItemModular(stack, player);
                InventoryUtils.tryMoveAllItems(inv, (IInventory)te, 0, side);
                inv.saveInventory();
            }

            return true;
        }

        return super.onItemUse(stack, player,world, x, y, z, side, hitX, hitY, hitZ);
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
        else if (mode == 1)
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

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(super.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    public static boolean onItemPickupEvent(EntityItemPickupEvent event)
    {
        int origStackSize = event.item.getEntityItem().stackSize;
        EntityPlayer player = event.entityPlayer;

        // If all the items fit into existing stacks in the player's inventory, then we do nothing more here
        if (InventoryUtils.tryInsertItemStackToExistingStacksInInventory(player.inventory, event.item.getEntityItem(), 0, false) == 0)
        {
            return false;
        }

        // Not all the items could fit into existing stacks in the player's inventory, move them directly to the bag
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.handyBag);
        for (int slot : slots)
        {
            ItemStack bagStack = player.inventory.getStackInSlot(slot);
            // Bag is not locked
            if (bagStack != null && bagStack.getItem() == EnderUtilitiesItems.handyBag && ItemHandyBag.bagIsOpenable(bagStack) == true)
            {
                InventoryItemModular inv = new InventoryItemModular(bagStack, player);
                int pickupMode = NBTUtils.getOrCreateCompoundTag(bagStack, "HandyBag").getByte("PickupMode");

                // Pickup mode is All, or Matching and the bag already contains the same item type
                if (pickupMode == 2 || (pickupMode == 1 && InventoryUtils.getSlotOfFirstMatchingItemStack(inv, event.item.getEntityItem()) != -1))
                {
                    // All items successfully inserted
                    if (InventoryUtils.tryInsertItemStackToInventory(inv, event.item.getEntityItem(), 0, true) == true)
                    {
                        event.item.getEntityItem().stackSize = 0;
                        event.item.setDead();
                        event.setCanceled(true);
                        break;
                    }
                    inv.saveInventory();
                }
            }
        }

        // At least some items were picked up
        if (event.item.getEntityItem().stackSize != origStackSize)
        {
            FMLCommonHandler.instance().firePlayerItemPickupEvent(player, event.item);
            player.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.onItemPickup(event.item, origStackSize);
            return true;
        }

        return false;
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
        int slotNum = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD);
        ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(containerStack, slotNum, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null && moduleStack.getItem() instanceof IModule)
        {
            int tier = ((IModule) moduleStack.getItem()).getModuleTier(moduleStack);
            if (tier >= 6 && tier <= 12)
            {
                return (int)Math.pow(2, tier);
            }
        }

        //return containerStack.getItemDamage() == DAMAGE_TIER_2 ? MAX_STACKSIZE_TIER_2 : MAX_STACKSIZE_TIER_1;
        return 0;
    }

    public static void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (player.openContainer instanceof ContainerHandyBag)
        {
            ContainerHandyBag container = (ContainerHandyBag)player.openContainer;
            ItemStack containerStack = container.inventoryItemModular.getContainerItemStack();
            if (containerStack != null && containerStack.getItem() == EnderUtilitiesItems.handyBag)
            {
                int max = ((ItemHandyBag)containerStack.getItem()).getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD);
                // Changing the selected module via the GUI buttons
                if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < max)
                {
                    UtilItemModular.setModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD, element);
                    container.inventoryItemModular.updateContainerItems();
                }
                else if (action == GUI_ACTION_MOVE_ITEMS && element >= 0 && element <= 5)
                {
                    int bagMaxSlot = container.inventoryItemModular.getSizeInventory() - 1;
                    int playerMaxSlot = player.inventory.getSizeInventory() - 5;
                    switch(element)
                    {
                        case 0: // Move all items to Bag
                            InventoryUtils.tryMoveAllItemsWithinSlotRange(player.inventory, container.inventoryItemModular, 0, 0, 0, playerMaxSlot, 0, bagMaxSlot, true);
                            break;
                        case 1: // Move matching items to Bag
                            InventoryUtils.tryMoveMatchingItemsWithinSlotRange(player.inventory, container.inventoryItemModular, 0, 0, 0, playerMaxSlot, 0, bagMaxSlot, true);
                            break;
                        case 2: // Leave one stack of each item type and fill that stack
                            InventoryUtils.leaveOneFullStackOfEveryItem(player.inventory, container.inventoryItemModular, false, false, true);
                            break;
                        case 3: // Fill stacks in player inventory from bag
                            InventoryUtils.fillStacksOfMatchingItemsWithinSlotRange(container.inventoryItemModular, player.inventory, 0, 0, 0, bagMaxSlot, 0, playerMaxSlot, false);
                            break;
                        case 4: // Move matching items to player inventory
                            InventoryUtils.tryMoveMatchingItemsWithinSlotRange(container.inventoryItemModular, player.inventory, 0, 0, 0, bagMaxSlot, 0, playerMaxSlot, false);
                            break;
                        case 5: // Move all items to player inventory
                            InventoryUtils.tryMoveAllItemsWithinSlotRange(container.inventoryItemModular, player.inventory, 0, 0, 0, bagMaxSlot, 0, playerMaxSlot, false);
                            break;
                    }
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
                if (tier >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B &&
                    tier <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B)
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
