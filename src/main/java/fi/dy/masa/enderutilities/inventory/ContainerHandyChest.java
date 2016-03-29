package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerHandyChest extends ContainerLargeStacks
{
    protected TileEntityHandyChest tehc;
    public int selectedModule;
    public int actionMode;

    public ContainerHandyChest(EntityPlayer player, TileEntityHandyChest te)
    {
        super(player, te.getWrappedInventoryForContainer());
        this.tehc = te;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 95);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 8;
        int posY = 41;

        int tier = this.tehc.getStorageTier();
        int rows = tier >= 0 && tier <= 2 ? (tier + 1) * 2 : 2;

        // Item inventory slots
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        // Add the module slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posX = 98;
        posY = 8;

        // The Storage Module slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerModule(this.tehc.getModuleInventory(), i, posX + i * 18, posY, ModuleType.TYPE_MEMORY_CARD_ITEMS));
        }
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        int tier = this.tehc.getStorageTier();
        posY = tier >= 0 && tier <= 2 ? posY + tier * 36 : posY;

        super.addPlayerInventorySlots(posX, posY);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our main item inventory
        if (slot instanceof SlotItemHandler && ((SlotItemHandler)slot).getItemHandler() == this.inventory)
        {
            return slot.getItemStackLimit(stack);
        }

        // Player inventory or module slots
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    public void onCraftGuiOpened(ICrafting icrafting)
    {
        super.onCraftGuiOpened(icrafting);

        icrafting.sendProgressBarUpdate(this, 0, this.tehc.getSelectedModule());
        icrafting.sendProgressBarUpdate(this, 1, this.tehc.getQuickMode());
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.tehc.getWorld().isRemote == true)
        {
            return;
        }

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.selectedModule != this.tehc.getSelectedModule())
            {
                icrafting.sendProgressBarUpdate(this, 0, this.tehc.getSelectedModule());
            }

            if (this.actionMode != this.tehc.getQuickMode())
            {
                icrafting.sendProgressBarUpdate(this, 1, this.tehc.getQuickMode());
            }
        }

        this.selectedModule = this.tehc.getSelectedModule();
        this.actionMode = this.tehc.getQuickMode();

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int var, int val)
    {
        super.updateProgressBar(var, val);

        switch (var)
        {
            case 0:
                this.tehc.setSelectedModule(val);
                break;
            case 1:
                this.tehc.setQuickMode(val);
                break;
            default:
        }
    }
}
