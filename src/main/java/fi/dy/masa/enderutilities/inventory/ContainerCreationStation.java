package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerCreationStation extends ContainerLargeStacks
{
    protected final TileEntityCreationStation tecs;
    public int selectedModule;
    public int actionMode;
    public int fastModeMask;

    public final IInventory craftResultLeft;
    public final IInventory craftResultRight;
    protected InventoryItemCrafting craftMatrixLeft;
    protected InventoryItemCrafting craftMatrixRight;
    protected InventoryStackArray furnaceInventory;

    public ContainerCreationStation(EntityPlayer player, TileEntityCreationStation te)
    {
        super(player, te);
        this.tecs = te;
        te.openInventory();

        this.craftMatrixLeft = te.getCraftingInventoryLeft(this, player);
        this.craftMatrixRight = te.getCraftingInventoryRight(this, player);
        this.craftResultLeft = te.getCraftResultInventoryLeft();
        this.craftResultRight = te.getCraftResultInventoryRight();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(40, 174);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.furnaceInventory = this.tecs.getFurnaceInventory();

        int customInvStart = this.inventorySlots.size();
        int posX = 40;
        int posY = 102;

        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotGeneric(this.tecs.getItemInventory(), i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        this.customInventorySlots = new SlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        // Add the module slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posX = 216;
        posY = 102;

        int min = ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B;
        int max = ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_10B;

        // The Storage Module slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotModule(this.tecs, i, posX, posY + i * 18, ModuleType.TYPE_MEMORY_CARD).setMinAndMaxModuleTier(min, max));
        }

        // Crafting slots, left side
        posX = 40;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotGeneric(this.craftMatrixLeft, j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrixLeft, this.craftResultLeft, 0, 112, 33));

        // Crafting slots, right side
        posX = 148;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotGeneric(this.craftMatrixRight, j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrixRight, this.craftResultRight, 0, 112, 69));

        // Furnace slots, left side
        // Smeltable items
        this.addSlotToContainer(new SlotSmeltable(this.furnaceInventory, 0, 8, 8));
        // Fuel
        this.addSlotToContainer(new SlotFuel(this.furnaceInventory, 1, 8, 51));
        // Output
        this.addSlotToContainer(new SlotFurnace(this.player, this.furnaceInventory, 2, 40, 8));

        // Furnace slots, right side
        // Smeltable items
        this.addSlotToContainer(new SlotSmeltable(this.furnaceInventory, 3, 216, 8));
        // Fuel
        this.addSlotToContainer(new SlotFuel(this.furnaceInventory, 4, 216, 51));
        // Output
        this.addSlotToContainer(new SlotFurnace(this.player, this.furnaceInventory, 5, 184, 8));

        this.onCraftMatrixChanged(this.craftMatrixLeft);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv)
    {
        super.onCraftMatrixChanged(inv);

        this.craftResultLeft.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrixLeft, this.player.worldObj));
        this.craftResultRight.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrixRight, this.player.worldObj));
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        this.tecs.closeInventory();
    }

    @Override
    public boolean func_94530_a(ItemStack stack, Slot slot)
    {
        return slot.inventory != this.craftResultLeft && slot.inventory != this.craftResultRight && super.func_94530_a(stack, slot);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Player inventory, module slots, crafting slots or furnace slots
        if (slot.inventory != this.tecs.getItemInventory())
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
        if (slot != null && slot.isSlotInInventory(this.tecs.getItemInventory(), slotNum) == true)
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

        icrafting.sendProgressBarUpdate(this, 0, this.tecs.getSelectedModule());
        icrafting.sendProgressBarUpdate(this, 1, this.tecs.getQuickMode());
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.tecs.getWorldObj().isRemote == true)
        {
            return;
        }

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.selectedModule != this.tecs.getSelectedModule())
            {
                icrafting.sendProgressBarUpdate(this, 0, this.tecs.getSelectedModule());
            }

            if (this.actionMode != this.tecs.getQuickMode())
            {
                icrafting.sendProgressBarUpdate(this, 1, this.tecs.getQuickMode());
            }

            if (this.fastModeMask != this.tecs.getFastModeMask())
            {
                icrafting.sendProgressBarUpdate(this, 2, this.tecs.getFastModeMask());
            }
        }

        this.selectedModule = this.tecs.getSelectedModule();
        this.actionMode = this.tecs.getQuickMode();
        this.fastModeMask = this.tecs.getFastModeMask();
    }

    @Override
    public void updateProgressBar(int var, int val)
    {
        super.updateProgressBar(var, val);

        switch (var)
        {
            case 0:
                this.tecs.setSelectedModule(val);
                break;
            case 1:
                this.tecs.setQuickMode(val);
                break;
            case 2:
                this.fastModeMask = val;
                break;
            default:
        }
    }
}
