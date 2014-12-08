package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ContainerToolWorkstation extends ContainerEnderUtilitiesInventory
{
    public static final int NUM_MODULE_SLOTS = 10;
    public static final int NUM_STORAGE_SLOTS = 9;

    public ContainerToolWorkstation(TileEntityToolWorkstation te, InventoryPlayer inventory)
    {
        super(te, inventory);
    }

    @Override
    protected void addSlots()
    {
        // Item slot
        this.addSlotToContainer(new SlotItemModular(this.te, 0, 8, 19));

        // Module slots
        int x = 80, y = 19;
        for (int i = 0; i < NUM_MODULE_SLOTS; x += 18)
        {
            // We initially add all the slots as generic. When the player inserts a tool into the tool slot,
            // we will then re-assign the slot types based on the tool.
            this.addSlotToContainer(new SlotUpgradeModule(this.te, i + 1, x, y, UtilItemModular.ModuleType.TYPE_ANY));
            if (++i == 5)
            {
                y += 18;
                x -= 5 * 18;
            }
        }

        // Module storage inventory slots
        x = 8; y = 66;
        for (int i = 0; i < NUM_STORAGE_SLOTS; x += 18, ++i)
        {
            this.addSlotToContainer(new SlotUpgradeModuleStorage(this.te, i + 11, x, y));
        }

        this.setUpgradeSlotTypes();
    }

    @Override
    protected int getPlayerInventoryVerticalOffset()
    {
        return 94;
    }

    @Override
    public void putStackInSlot(int slotNum, ItemStack stack)
    {
        super.putStackInSlot(slotNum, stack);

        if (slotNum == 0)
        {
            // This is to get rid of the minor annoyance of the slot types/backgrounds not updating when you
            // open the workstation for the first time after loading a world, if there is a tool in there.
            // So it updates the slot types when the server syncs the stacks to the client.
            this.setUpgradeSlotTypes();
        }
    }

    private void setUpgradeSlotTypes()
    {
        Slot slot = (Slot)this.inventorySlots.get(0);
        if (slot != null && slot.getHasStack() == true && slot.getStack().getItem() instanceof IModular)
        {
            ItemStack toolStack = slot.getStack();
            int i = 1, j = 0, max = 0;
            int slots = this.inventorySlots.size();

            // Set the upgrade slot types according to how many of each type of upgrade the current tool supports.
            for (UtilItemModular.ModuleType mt : UtilItemModular.ModuleType.values())
            {
                max = ((IModular)toolStack.getItem()).getMaxModules(toolStack, mt);
                // 10: The Tool Workstation supports a maximum of 10 upgrade modules for any tools atm
                for (j = 0; j < max && i < NUM_MODULE_SLOTS && i < slots; ++j, ++i)
                {
                    if (i >= NUM_MODULE_SLOTS || mt.getOrdinal() < -1) // < -1: Don't add TYPE_INVALID slots...
                    {
                        return;
                    }

                    slot = (Slot)this.inventorySlots.get(i);
                    if (slot instanceof SlotUpgradeModule)
                    {
                        ((SlotUpgradeModule)slot).setModuleType(mt);
                    }
                }
            }
        }
        // No tool, reset all slot types
        /*else
        {
            int slots = this.inventorySlots.size();
            for (int i = 0; i < 10 && i < slots; ++i)
            {
                slot = (Slot)this.inventorySlots.get(i + 1);
                if (slot instanceof SlotUpgradeModule)
                {
                    ((SlotUpgradeModule)slot).setModuleType(UtilItemModular.ModuleType.TYPE_ANY);
                }
            }
        }*/
    }

    @Override
    public ItemStack slotClick(int slotNum, int i1, int i2, EntityPlayer player)
    {
        //System.out.println("slotClick(" + slotNum + ", " + i1 + ", " + i2 + ", ); isRemote: " + this.te.getWorldObj().isRemote);

        if (this.te instanceof TileEntityToolWorkstation)
        {
            // This is to force the modules to be written to the tool before the transferStackInSlot/mergeItemStack
            // methods get a hold of the tool stack.
            ((TileEntityToolWorkstation)this.te).writeModulesToItem();
        }

        ItemStack stack = super.slotClick(slotNum, i1, i2, player);

        if (this.te instanceof TileEntityToolWorkstation)
        {
            // This is to write the changes to the tool if the player manually clicks an item into the module slots
            // (the above write call in that case happened nefore the item was added to the slot).
            ((TileEntityToolWorkstation)this.te).writeModulesToItem();
        }

        this.setUpgradeSlotTypes();

        return stack;
    }
}
