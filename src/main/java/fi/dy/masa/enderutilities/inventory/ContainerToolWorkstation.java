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
        for (int i = 0; i < 10; x += 18)
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
        for (int i = 0; i < 9; x += 18, ++i)
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
                for (j = 0; j < max && i < 10 && i < slots; ++j, ++i)
                {
                    if (i >= 10 || mt.getOrdinal() < -1) // < -1: Don't add TYPE_INVALID slots...
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
    public ItemStack slotClick(int slotNum, int p_75144_2_, int p_75144_3_, EntityPlayer player)
    {
        ItemStack stack = super.slotClick(slotNum, p_75144_2_, p_75144_3_, player);
        this.setUpgradeSlotTypes();
        return stack;
    }
}
