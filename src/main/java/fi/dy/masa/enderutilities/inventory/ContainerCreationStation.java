package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerCreationStation extends ContainerLargeStacks
{
    protected final TileEntityCreationStation tecs;
    public int selectionsLast; // action mode and module selection
    public int modeMask;
    public int fuelProgress;
    public int smeltProgress;

    public final IInventory[] craftResults;
    public final InventoryItemCrafting[] craftMatrices;
    public final InventoryStackArray furnaceInventory;
    private SlotRange craftingGridSlotsLeft;
    private SlotRange craftingGridSlotsRight;

    public ContainerCreationStation(EntityPlayer player, TileEntityCreationStation te)
    {
        super(player, te);
        this.tecs = te;
        te.openInventory(player);

        this.craftMatrices = new InventoryItemCrafting[] { te.getCraftingInventory(0, this, player), te.getCraftingInventory(1, this, player) };
        this.craftResults = new IInventory[] { te.getCraftResultInventory(0), te.getCraftResultInventory(1) };
        this.furnaceInventory = this.tecs.getFurnaceInventory();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(40, 174);
    }

    @Override
    protected void addCustomInventorySlots()
    {
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
        int max = ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;

        // The Storage Module slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotModule(this.tecs, i, posX, posY + i * 18, ModuleType.TYPE_MEMORY_CARD_ITEMS).setMinAndMaxModuleTier(min, max));
        }

        // Crafting slots, left side
        this.craftingGridSlotsLeft = new SlotRange(this.inventorySlots.size(), 9);
        posX = 40;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotGeneric(this.craftMatrices[0], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrices[0], this.craftResults[0], 0, 112, 33));

        // Crafting slots, right side
        this.craftingGridSlotsRight = new SlotRange(this.inventorySlots.size(), 9);
        posX = 148;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotGeneric(this.craftMatrices[1], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrices[1], this.craftResults[1], 0, 112, 69));

        // Add the furnace slots as priority merge slots
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 6);

        // Furnace slots, left side
        // Smeltable items
        this.addSlotToContainer(new SlotSmeltable(this.furnaceInventory, 0, 8, 8));
        // Fuel
        this.addSlotToContainer(new SlotFuel(this.furnaceInventory, 1, 8, 51));
        // Output
        this.addSlotToContainer(new SlotFurnaceOutput(this.player, this.furnaceInventory, 2, 40, 8));

        // Furnace slots, right side
        // Smeltable items
        this.addSlotToContainer(new SlotSmeltable(this.furnaceInventory, 3, 216, 8));
        // Fuel
        this.addSlotToContainer(new SlotFuel(this.furnaceInventory, 4, 216, 51));
        // Output
        this.addSlotToContainer(new SlotFurnaceOutput(this.player, this.furnaceInventory, 5, 184, 8));

        this.onCraftMatrixChanged(this.craftMatrices[0]);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv)
    {
        super.onCraftMatrixChanged(inv);

        this.craftResults[0].setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[0], this.player.worldObj));
        this.craftResults[1].setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[1], this.player.worldObj));
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        this.tecs.closeInventory(player);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot)
    {
        return slot.inventory != this.craftResults[0] && slot.inventory != this.craftResults[0] && super.canMergeSlot(stack, slot);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our main item inventory or the furnace inventory
        if (slot.inventory == this.tecs.getItemInventory() || slot.inventory == this.tecs.getFurnaceInventory())
        {
            return slot.getSlotStackLimit();
        }

        // Player inventory, module slots or crafting slots
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    public boolean transferStackFromSlot(EntityPlayer player, int slotNum)
    {
        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (slotNum == 40 || slotNum == 50)
        {
            int invId = slotNum == 50 ? 1 : 0;

            if (this.tecs.canCraftItems(invId) == false)
            {
                return false;
            }

            boolean ret = super.transferStackFromSlot(player, slotNum);
            this.tecs.restockCraftingGrid(invId);

            return ret;
        }
        // Crafting grid slots, try to merge to the main item inventory first
        else if (this.isSlotInRange(this.craftingGridSlotsLeft, slotNum) == true || this.isSlotInRange(this.craftingGridSlotsRight, slotNum) == true)
        {
            if (this.transferStackToSlotRange(player, slotNum, this.customInventorySlots.first, this.customInventorySlots.lastExc, false) == true)
            {
                return true;
            }
        }

        return super.transferStackFromSlot(player, slotNum);
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (slotNum == 40 || slotNum == 50)
        {
            int invId = slotNum == 50 ? 1 : 0;

            if (this.tecs.canCraftItems(invId) == false)
            {
                return null;
            }

            ItemStack stack = super.slotClick(slotNum, button, type, player);
            this.tecs.restockCraftingGrid(invId);

            return stack;
        }

        return super.slotClick(slotNum, button, type, player);
    }

    @Override
    public void middleClickSlot(int slotNum, EntityPlayer player)
    {
        Slot slot1 = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;

        // Only allow swapping in this inventory (which supports the large stacks)
        if (slot1 != null && slot1.isHere(this.tecs.getItemInventory(), slotNum) == true)
        {
            if (this.selectedSlot != -1)
            {
                // Don't swap with self
                if (this.selectedSlot != slotNum)
                {
                    Slot slot2 = this.getSlot(this.selectedSlot);
                    ItemStack stackTmp1 = slot1.getStack();
                    ItemStack stackTmp2 = slot2.getStack();
                    slot1.putStack(stackTmp2);
                    slot2.putStack(stackTmp1);

                    slot1.onPickupFromSlot(player, stackTmp1);
                    slot2.onPickupFromSlot(player, stackTmp2);
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
    public void onCraftGuiOpened(ICrafting icrafting)
    {
        super.onCraftGuiOpened(icrafting);

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModule();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        icrafting.sendProgressBarUpdate(this, 0, modeMask);
        icrafting.sendProgressBarUpdate(this, 1, selection);
        icrafting.sendProgressBarUpdate(this, 2, fuelProgress);
        icrafting.sendProgressBarUpdate(this, 3, smeltProgress);

        this.detectAndSendChanges();
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.tecs.getWorld().isRemote == true)
        {
            return;
        }

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModule();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.modeMask != modeMask)
            {
                icrafting.sendProgressBarUpdate(this, 0, modeMask);
            }
            if (this.selectionsLast != selection)
            {
                icrafting.sendProgressBarUpdate(this, 1, selection);
            }
            if (this.fuelProgress != fuelProgress)
            {
                icrafting.sendProgressBarUpdate(this, 2, fuelProgress);
            }
            if (this.smeltProgress != smeltProgress)
            {
                icrafting.sendProgressBarUpdate(this, 3, smeltProgress);
            }
        }

        this.modeMask = modeMask;
        this.selectionsLast = selection;
        this.fuelProgress = fuelProgress;
        this.smeltProgress = smeltProgress;
    }

    @Override
    public void updateProgressBar(int var, int val)
    {
        super.updateProgressBar(var, val);

        switch (var)
        {
            case 0:
                this.modeMask = val;
                break;
            case 1:
                this.tecs.setSelectedModule(val & 0x3); // 0..3
                this.tecs.setQuickMode((val >> 2) & 0x7); // 0..5
                this.tecs.inventoryChanged(TileEntityCreationStation.INV_ID_MODULES);
                break;
            case 2:
                this.fuelProgress = val; // value is 0..100, left furnace is in the lower bits 7..0
                break;
            case 3:
                this.smeltProgress = val; // value is 0..100, left furnace is in the lower bits 7..0
                break;
            default:
        }
    }
}
