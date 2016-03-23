package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.SlotItemHandler;

import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerToolWorkstation extends ContainerTileEntityInventory implements IContainerModularItem
{
    public static final int NUM_MODULE_SLOTS = 10;
    public static final int NUM_STORAGE_SLOTS = 9;
    public static final int SLOT_MODULAR_ITEM = 0;
    public final InventoryItemModules inventoryItem;

    public ContainerToolWorkstation(EntityPlayer player, TileEntityToolWorkstation te)
    {
        super(player, te);
        ItemStack tool = this.inventory.getStackInSlot(TileEntityToolWorkstation.SLOT_TOOL);
        this.inventoryItem = new InventoryItemModules(tool, NUM_MODULE_SLOTS, te.getWorld().isRemote, player);
        this.inventoryItem.readFromContainerItemStack();
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 94);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        // The modular item's slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, TileEntityToolWorkstation.SLOT_TOOL, 8, 19));

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

        // Module storage inventory slots
        x = 8; y = 66;
        for (int i = 0; i < NUM_STORAGE_SLOTS; x += 18, ++i)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, TileEntityToolWorkstation.SLOT_MODULES_START + i, x, y));
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

    /**
     * Note: If this implementation (how the module types are assigned to slots)
     * is ever changed, it will then affect UtilItemModular.getFirstIndexOfModuleType too!
     */
    private void setUpgradeSlotTypes()
    {
        int slotNum = SLOT_MODULAR_ITEM + 1;
        int slots = 0;
        Slot slot = this.getSlot(SLOT_MODULAR_ITEM);
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

        for ( ; slotNum < (NUM_MODULE_SLOTS + 1); slotNum++)
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

        Slot slot = this.getSlot(slotNum);
        ItemStack stack = super.slotClick(slotNum, i1, i2, player);

        // The clicked on slot is inside the modular item's inventory
        if (slot != null && slot instanceof SlotItemHandler && ((SlotItemHandler)slot).itemHandler == this.inventoryItem)
        {
            this.inventoryItem.onContentsChanged(slot.getSlotIndex());
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
