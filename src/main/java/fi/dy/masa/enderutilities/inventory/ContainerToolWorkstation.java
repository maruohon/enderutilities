package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerToolWorkstation extends ContainerTileEntityInventory implements IContainerModularItem
{
    public static final int NUM_MODULE_SLOTS = 10;
    public static final int NUM_STORAGE_SLOTS = 9;
    public static final int SLOT_MODULAR_ITEM = NUM_MODULE_SLOTS;
    public InventoryItemModules inventoryItem;
    protected boolean isRemote;

    public ContainerToolWorkstation(EntityPlayer player, TileEntityToolWorkstation te)
    {
        super(player, te);
        this.inventoryItem = new InventoryItemModules(this.inventory.getStackInSlot(TileEntityToolWorkstation.SLOT_TOOL),
                NUM_MODULE_SLOTS, this.te.getWorld().isRemote, inventoryPlayer.player);
        this.inventoryItem.readFromContainerItemStack();
        this.isRemote = this.te.getWorld().isRemote;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 94);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        // Item's module slots
        int x = 80, y = 19;
        for (int i = 0; i < NUM_MODULE_SLOTS; x += 18, i++)
        {
            // We initially add all the slots as generic. When the player inserts a tool into the tool slot,
            // we will then re-assign the slot types based on the tool.
            this.addSlotToContainer(new SlotModuleModularItem(this.inventoryItem, i, x, y, ModuleType.TYPE_ANY, this));

            // First row done
            if (i == 4)
            {
                y += 18;
                x -= 5 * 18;
            }
        }

        // NOTE: The following slots are in the TileEntity's inventory and not in the modular item's inventory,
        // thus the slot numbering starts from 0 here again.

        // The modular item's slot
        this.addSlotToContainer(new SlotGeneric(this.inventory, TileEntityToolWorkstation.SLOT_TOOL, 8, 19));

        // Module storage inventory slots
        x = 8; y = 66;
        for (int i = 0; i < NUM_STORAGE_SLOTS; x += 18, ++i)
        {
            this.addSlotToContainer(new SlotGeneric(this.inventory, TileEntityToolWorkstation.SLOT_MODULES_START + i, x, y));
        }

        this.customInventorySlots = new SlotRange(0, this.inventorySlots.size());
        this.setUpgradeSlotTypes();
    }

    @Override
    public void putStackInSlot(int slotNum, ItemStack stack)
    {
        super.putStackInSlot(slotNum, stack);

        if (slotNum == SLOT_MODULAR_ITEM)
        {
            // This is to get rid of the minor annoyance of the slot types/backgrounds not updating when you
            // open the workstation for the first time after loading a world, if there is a tool in there.
            // This updates the slot types when the server syncs the stacks to the client.
            this.setUpgradeSlotTypes();
        }
    }

    @Override
    public ItemStack getModularItem()
    {
        return this.getSlot(SLOT_MODULAR_ITEM).getStack();
    }

    private void setUpgradeSlotTypes()
    {
        int slotNum = 0;
        Slot slot = this.getSlot(SLOT_MODULAR_ITEM);
        if (slot != null && slot.getHasStack() == true && slot.getStack().getItem() instanceof IModular)
        {
            ItemStack toolStack = slot.getStack();
            IModular imodular = (IModular)toolStack.getItem();
            int numSlots = this.inventorySlots.size();

            // Set the upgrade slot types according to how many of each type of upgrade the current tool supports.
            for (ModuleType moduleType : ModuleType.values())
            {
                // Don't add the invalid type, doh
                if (moduleType.equals(ModuleType.TYPE_INVALID) == true)
                {
                    continue;
                }

                int maxOfType = imodular.getMaxModules(toolStack, moduleType);

                for (int i = 0; i < maxOfType && slotNum < NUM_MODULE_SLOTS && slotNum < numSlots; i++, slotNum++)
                {
                    slot = this.getSlot(slotNum);
                    if (slot instanceof SlotModuleModularItem)
                    {
                        ((SlotModuleModularItem)slot).setModuleType(moduleType);
                    }
                }

                if (slotNum >= NUM_MODULE_SLOTS || slotNum >= numSlots)
                {
                    break;
                }
            }
        }

        for ( ; slotNum < NUM_MODULE_SLOTS; slotNum++)
        {
            slot = this.getSlot(slotNum);
            if (slot instanceof SlotModuleModularItem)
            {
                ((SlotModuleModularItem)slot).setModuleType(ModuleType.TYPE_INVALID);
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotNum, int i1, int i2, EntityPlayer player)
    {
        //System.out.println("slotClick(" + slotNum + ", " + i1 + ", " + i2 + ", ); isRemote: " + this.te.getWorldObj().isRemote);

        Slot slot = slotNum >= 0 && slotNum <= this.inventorySlots.size() ? this.getSlot(slotNum) : null;
        ItemStack stack = super.slotClick(slotNum, i1, i2, player);

        // The clicked on slot is inside the modular item's inventory
        if (slot != null && slot.inventory == this.inventoryItem)
        {
            this.inventoryItem.markDirty();
        }
        // Changing the item in the tool slot, update the InventoryItem
        else if (this.inventoryItem.getContainerItemStack() != this.inventory.getStackInSlot(TileEntityToolWorkstation.SLOT_TOOL))
        {
            this.inventoryItem.setContainerItemStack(this.inventory.getStackInSlot(TileEntityToolWorkstation.SLOT_TOOL));
            this.setUpgradeSlotTypes();
        }

        this.detectAndSendChanges();

        return stack;
    }
}
