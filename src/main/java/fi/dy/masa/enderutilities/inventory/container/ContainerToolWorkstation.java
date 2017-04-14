package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerTileEntityInventory;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotModuleModularItem;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IStringInput;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class ContainerToolWorkstation extends ContainerTileEntityInventory implements IContainerItem, IStringInput
{
    public static final int CONT_SLOT_MODULES_START = 1;
    public static final int NUM_MODULE_SLOTS = 10;
    public static final int NUM_STORAGE_SLOTS = 9;

    private final TileEntityToolWorkstation tetw;
    private int slotTool;
    private int slotRename;
    private ItemStack toolStackLast;

    public ContainerToolWorkstation(EntityPlayer player, TileEntityToolWorkstation te)
    {
        super(player, te);

        this.tetw = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 135);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        // The modular item's slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.tetw.getToolSlotInventory(), 0, 8, 19));
        this.slotTool = this.inventorySlots.size() - 1;

        // Item's module slots
        int x = 80, y = 19;
        for (int i = 0; i < NUM_MODULE_SLOTS; x += 18, i++)
        {
            // We initially add all the slots as invalid. When the player inserts a tool into the tool slot,
            // we will then re-assign the slot types based on the tool.
            this.addSlotToContainer(new SlotModuleModularItem(this.tetw.getInstalledModulesInventory(), i, x, y, ModuleType.TYPE_INVALID, this));

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
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i, x, y));
        }

        // The slot for renaming items
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.tetw.getRenameSlotInventory(), 0, 8, 97));
        this.slotRename = this.inventorySlots.size() - 1;

        this.customInventorySlots = new MergeSlotRange(0, this.inventorySlots.size());

        this.setModuleSlotTypes();
        // This fixes the slot module types not updating the first time if there is a tool in the tool slot when opening the GUI
        this.toolStackLast = this.getContainerItem();
    }

    @Override
    public ItemStack getContainerItem()
    {
        return this.getSlot(this.slotTool).getStack();
    }

    public int getSlotTool()
    {
        return this.slotTool;
    }

    public int getSlotRename()
    {
        return this.slotRename;
    }

    /**
     * Note: If this implementation (how the module types are assigned to slots)
     * is ever changed, it will then affect UtilItemModular.getFirstIndexOfModuleType too!
     */
    private void setModuleSlotTypes()
    {
        int slotNum = CONT_SLOT_MODULES_START;
        int slots = 0;

        Slot slot = this.getSlot(this.slotTool);

        if (slot != null && slot.getHasStack() && slot.getStack().getItem() instanceof IModular)
        {
            ItemStack toolStack = slot.getStack();
            IModular imodular = (IModular) toolStack.getItem();

            // Set the upgrade slot types according to how many of each type of upgrade the current tool supports.
            for (ModuleType moduleType : ModuleType.values())
            {
                // Don't add the invalid type, doh
                if (moduleType.equals(ModuleType.TYPE_INVALID))
                {
                    continue;
                }

                int maxOfType = imodular.getMaxModules(toolStack, moduleType);

                for (int i = 0; i < maxOfType && slots < NUM_MODULE_SLOTS; i++, slotNum++, slots++)
                {
                    slot = this.getSlot(slotNum);

                    if (slot instanceof SlotModuleModularItem)
                    {
                        ((SlotModuleModularItem) slot).setModuleType(moduleType);
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
                ((SlotModuleModularItem) slot).setModuleType(ModuleType.TYPE_INVALID);
            }
        }
    }

    @Override
    public void syncStackInSlot(int slotId, ItemStack stack)
    {
        super.syncStackInSlot(slotId, stack);

        if (slotId == this.slotTool && this.getContainerItem() != this.toolStackLast)
        {
            // This just fixes a minor de-sync with the module slot types,
            // if another player changes the item in the tool slot
            this.setModuleSlotTypes();
            this.toolStackLast = this.getContainerItem();
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        // We are just using this detectAndSendChanges() method to constantly check
        // when the tool stack changes, and then update the slot types
        if (this.getContainerItem() != this.toolStackLast)
        {
            this.setModuleSlotTypes();
        }

        this.toolStackLast = this.getContainerItem();
    }

    @Override
    public void handleString(EntityPlayer player, ItemStack stack, String text)
    {
        this.tetw.renameItem(text);
    }
}
