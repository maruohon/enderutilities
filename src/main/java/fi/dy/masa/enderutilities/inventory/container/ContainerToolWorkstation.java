package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModules;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotModuleModularItem;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IStringInput;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class ContainerToolWorkstation extends ContainerTileEntityInventory implements IContainerItem, IStringInput
{
    private final TileEntityToolWorkstation tetw;
    public static final int CONT_SLOT_TOOL = 0;
    public static final int CONT_SLOT_MODULES_START = 1;
    public static final int CONT_SLOT_RENAME = 20;
    public static final int NUM_MODULE_SLOTS = 10;
    public static final int NUM_STORAGE_SLOTS = 9;
    public final InventoryItemModules inventoryItem;

    public ContainerToolWorkstation(EntityPlayer player, TileEntityToolWorkstation te)
    {
        super(player, te);
        this.tetw = te;
        ItemStack tool = this.inventory.getStackInSlot(TileEntityToolWorkstation.INV_SLOT_TOOL);
        this.inventoryItem = new InventoryItemModules(tool, NUM_MODULE_SLOTS, te.getWorld().isRemote, player);
        this.inventoryItem.readFromContainerItemStack();
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 135);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        // The modular item's slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, TileEntityToolWorkstation.INV_SLOT_TOOL, 8, 19));

        // Item's module slots
        int x = 80, y = 19;
        for (int i = 0; i < NUM_MODULE_SLOTS; x += 18, i++)
        {
            // We initially add all the slots as invalid. When the player inserts a tool into the tool slot,
            // we will then re-assign the slot types based on the tool.
            this.addSlotToContainer(new SlotModuleModularItem(this.inventoryItem, i, x, y, ModuleType.TYPE_INVALID, this));

            // First row done
            if (i == 4)
            {
                y += 18;
                x -= 5 * 18;
            }
        }

        // Module storage inventory slots
        x = 8; y = 66;
        for (int i = 0; i < NUM_STORAGE_SLOTS; x += 18, ++i)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, TileEntityToolWorkstation.INV_SLOT_MODULES_START + i, x, y));
        }

        // The slot for renaming items
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, TileEntityToolWorkstation.INV_SLOT_RENAME, 8, 97));

        this.customInventorySlots = new MergeSlotRange(0, this.inventorySlots.size());
        this.setUpgradeSlotTypes();
    }

    @Override
    public void putStackInSlot(int slotNum, ItemStack stack)
    {
        super.putStackInSlot(slotNum, stack);

        if (slotNum == CONT_SLOT_TOOL)
        {
            // This is to get rid of the minor annoyance of the slot types/backgrounds not updating when you
            // open the workstation for the first time after loading a world, if there is a tool in there.
            // This updates the slot types when the server syncs the stacks to the client.
            this.setUpgradeSlotTypes();
        }
    }

    @Override
    public ItemStack getContainerItem()
    {
        return this.getSlot(CONT_SLOT_TOOL).getStack();
    }

    /**
     * Note: If this implementation (how the module types are assigned to slots)
     * is ever changed, it will then affect UtilItemModular.getFirstIndexOfModuleType too!
     */
    private void setUpgradeSlotTypes()
    {
        int slotNum = CONT_SLOT_MODULES_START;
        int slots = 0;

        Slot slot = this.getSlot(CONT_SLOT_TOOL);

        if (slot != null && slot.getHasStack() == true && slot.getStack().getItem() instanceof IModular)
        {
            ItemStack toolStack = slot.getStack();
            IModular imodular = (IModular)toolStack.getItem();

            // Set the upgrade slot types according to how many of each type of upgrade the current tool supports.
            for (ModuleType moduleType : ModuleType.values())
            {
                // Don't add the invalid type, doh
                if (moduleType.equals(ModuleType.TYPE_INVALID) == true)
                {
                    continue;
                }

                int maxOfType = imodular.getMaxModules(toolStack, moduleType);

                for (int i = 0; i < maxOfType && slots < NUM_MODULE_SLOTS; i++, slotNum++, slots++)
                {
                    slot = this.getSlot(slotNum);
                    if (slot instanceof SlotModuleModularItem)
                    {
                        ((SlotModuleModularItem)slot).setModuleType(moduleType);
                    }
                }

                if (slots >= NUM_MODULE_SLOTS)
                {
                    break;
                }
            }
        }

        for ( ; slotNum < (CONT_SLOT_MODULES_START + NUM_MODULE_SLOTS); slotNum++)
        {
            slot = this.getSlot(slotNum);
            if (slot instanceof SlotModuleModularItem)
            {
                ((SlotModuleModularItem)slot).setModuleType(ModuleType.TYPE_INVALID);
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        //System.out.println("slotClick(" + slotNum + ", " + i1 + ", " + i2 + ", ); isRemote: " + this.te.getWorldObj().isRemote);

        Slot slot = this.getSlot(slotNum);
        ItemStack stack = super.slotClick(slotNum, dragType, clickType, player);

        // The clicked on slot is inside the modular item's inventory
        if (slot != null && slot instanceof SlotItemHandler && ((SlotItemHandler)slot).getItemHandler() == this.inventoryItem)
        {
            this.inventoryItem.onContentsChanged(slot.getSlotIndex());
        }
        // Changing the item in the tool slot, update the InventoryItem
        else if (this.inventoryItem.getContainerItemStack() != this.inventory.getStackInSlot(TileEntityToolWorkstation.INV_SLOT_TOOL))
        {
            this.inventoryItem.setContainerItemStack(this.inventory.getStackInSlot(TileEntityToolWorkstation.INV_SLOT_TOOL));
            this.setUpgradeSlotTypes();
        }

        this.detectAndSendChanges();

        return stack;
    }

    @Override
    public void handleString(EntityPlayer player, ItemStack stack, String text)
    {
        this.tetw.renameItem(text);
    }
}
