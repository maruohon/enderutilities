package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerHandyChest extends ContainerLargeStacks
{
    protected TileEntityHandyChest tehc;
    public int selectedModule;
    public int actionMode;

    public ContainerHandyChest(EntityPlayer player, TileEntityHandyChest te)
    {
        super(player, te);
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

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotGeneric(this.tehc.getItemInventory(), i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        this.customInventorySlots = new SlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        // Add the module slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posX = 98;
        posY = 8;

        int min = ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B;
        int max = ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;

        // The Storage Module slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotModule(this.tehc, i, posX + i * 18, posY, ModuleType.TYPE_MEMORY_CARD).setMinAndMaxModuleTier(min, max));
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
        // Player inventory or module slots
        if (slot.inventory != this.tehc.getItemInventory())
        {
            return super.getMaxStackSizeFromSlotAndStack(slot, stack);
        }

        // Our main item inventory
        return slot.getSlotStackLimit();
    }

    @Override
    public void middleClickSlot(int slotNum, EntityPlayer player)
    {
        Slot slot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;

        // Only allow swapping in this inventory (which supports the large stacks)
        if (slot != null && slot.isSlotInInventory(this.tehc.getItemInventory(), slotNum) == true)
        {
            if (this.selectedSlot != -1)
            {
                // Don't swap with self
                if (this.selectedSlot != slotNum)
                {
                    ItemStack stackTmp = slot.getStack();
                    slot.putStack(this.getSlot(this.selectedSlot).getStack());
                    this.getSlot(this.selectedSlot).putStack(stackTmp);

                    slot.onPickupFromSlot(player, stackTmp);
                    this.getSlot(this.selectedSlot).onPickupFromSlot(player, stackTmp);
                }
                this.selectedSlot = -1;
            }
            else
            {
                this.selectedSlot = slotNum;
            }
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting icrafting)
    {
        super.addCraftingToCrafters(icrafting);

        icrafting.sendProgressBarUpdate(this, 0, this.tehc.getSelectedModule());
        icrafting.sendProgressBarUpdate(this, 1, this.tehc.getQuickMode());
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.tehc.getWorldObj().isRemote == true)
        {
            return;
        }

        super.detectAndSendChanges();

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
