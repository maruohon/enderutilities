package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.SlotRange;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCreationStation extends ContainerLargeStacks
{
    protected final TileEntityCreationStation tecs;
    public int selectionsLast; // action mode and module selection
    public int modeMask;
    public int fuelProgress;
    public int smeltProgress;
    private int lastInteractedCraftingGrid;

    private final InventoryItemCrafting[] craftMatrices;
    private final IItemHandler[] craftMatrixWrappers;
    private final ItemStackHandlerBasic[] craftResults;
    private final IItemHandler furnaceInventory;
    private SlotRange craftingGridSlotsLeft;
    private SlotRange craftingGridSlotsRight;

    public ContainerCreationStation(EntityPlayer player, TileEntityCreationStation te)
    {
        super(player, te.getItemInventory());
        this.tecs = te;
        this.tecs.openInventory(player);

        this.craftMatrices = new InventoryItemCrafting[] { te.getCraftingInventory(0, this, player), te.getCraftingInventory(1, this, player) };
        this.craftMatrixWrappers = new IItemHandler[] { te.getCraftingInventoryWrapper(0), te.getCraftingInventoryWrapper(1) };
        this.craftResults = new ItemStackHandlerBasic[] { te.getCraftResultInventory(0), te.getCraftResultInventory(1) };
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

        // Item inventory slots
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        // Add the module slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posX = 216;
        posY = 102;

        // The Storage Module slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerModule(this.tecs.getMemoryCardInventory(), i, posX, posY + i * 18, ModuleType.TYPE_MEMORY_CARD_ITEMS));
        }

        // Crafting slots, left side
        this.craftingGridSlotsLeft = new SlotRange(this.inventorySlots.size(), 9);
        posX = 40;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.craftMatrixWrappers[0], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotItemHandlerCraftresult(this.player, this.craftMatrices[0], this.craftResults[0], 0, 112, 33));

        // Crafting slots, right side
        this.craftingGridSlotsRight = new SlotRange(this.inventorySlots.size(), 9);
        posX = 148;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.craftMatrixWrappers[1], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }
        this.addSlotToContainer(new SlotItemHandlerCraftresult(this.player, this.craftMatrices[1], this.craftResults[1], 0, 112, 69));

        // Add the furnace slots as priority merge slots, but only to already existing stacks
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 6, true);

        // Furnace slots, left side
        // Smeltable items
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 0, 8, 8));
        // Fuel
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 1, 8, 51));
        // Output
        this.addSlotToContainer(new SlotItemHandlerFurnaceOutput(this.player, this.furnaceInventory, 2, 40, 8));

        // Furnace slots, right side
        // Smeltable items
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 3, 216, 8));
        // Fuel
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.furnaceInventory, 4, 216, 51));
        // Output
        this.addSlotToContainer(new SlotItemHandlerFurnaceOutput(this.player, this.furnaceInventory, 5, 184, 8));

        this.onCraftMatrixChanged(this.craftMatrices[0]);
    }

    /**
     * Get the SlotRange for the given crafting grid id.
     * 0 = Left, 1 = Right
     */
    public SlotRange getCraftingGridSlotRange(int id)
    {
        return id == 1 ? this.craftingGridSlotsRight : this.craftingGridSlotsLeft;
    }

    public int getLastInteractedCraftingGridId()
    {
        return this.tecs.lastInteractedCraftingGrid;
    }

    public IItemHandler getCraftMatrixWrapper(int id)
    {
        return this.craftMatrixWrappers[id];
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv)
    {
        this.craftResults[0].setStackInSlot(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[0], this.player.worldObj));
        this.craftResults[1].setStackInSlot(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[1], this.player.worldObj));

        this.detectAndSendChanges();
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        this.tecs.closeInventory(player);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our main item inventory or the furnace inventory
        if (slot instanceof SlotItemHandler)
        {
            SlotItemHandler slotItemHandler = (SlotItemHandler)slot;
            if (slotItemHandler.getItemHandler() == this.inventory || slotItemHandler.getItemHandler() == this.tecs.getFurnaceInventory())
            {
                return slotItemHandler.getItemStackLimit(stack);
            }
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
            if (this.transferStackToSlotRange(player, slotNum, this.customInventorySlots, false) == true)
            {
                return true;
            }
        }

        return super.transferStackFromSlot(player, slotNum);
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        // Update the "last interacted on" crafting grid id, used for JEI recipe filling
        if (this.isSlotInRange(this.craftingGridSlotsLeft, slotNum) == true || slotNum == 40)
        {
            this.tecs.lastInteractedCraftingGrid = 0;
        }
        else if (this.isSlotInRange(this.craftingGridSlotsRight, slotNum) == true || slotNum == 50)
        {
            this.tecs.lastInteractedCraftingGrid = 1;
        }

        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (slotNum == 40 || slotNum == 50)
        {
            int invId = slotNum == 50 ? 1 : 0;

            if (this.tecs.canCraftItems(invId) == false)
            {
                return null;
            }

            ItemStack stack = super.slotClick(slotNum, dragType, clickType, player);
            this.tecs.restockCraftingGrid(invId);

            return stack;
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }

    @Override
    public void onCraftGuiOpened(ICrafting icrafting)
    {
        super.onCraftGuiOpened(icrafting);

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModuleSlot();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        icrafting.sendProgressBarUpdate(this, 0, modeMask);
        icrafting.sendProgressBarUpdate(this, 1, selection);
        icrafting.sendProgressBarUpdate(this, 2, fuelProgress);
        icrafting.sendProgressBarUpdate(this, 3, smeltProgress);
        icrafting.sendProgressBarUpdate(this, 4, this.tecs.lastInteractedCraftingGrid);

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
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModuleSlot();
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
            if (this.lastInteractedCraftingGrid != this.tecs.lastInteractedCraftingGrid)
            {
                icrafting.sendProgressBarUpdate(this, 4, this.tecs.lastInteractedCraftingGrid);
            }
        }

        this.modeMask = modeMask;
        this.selectionsLast = selection;
        this.fuelProgress = fuelProgress;
        this.smeltProgress = smeltProgress;
        this.lastInteractedCraftingGrid = this.tecs.lastInteractedCraftingGrid;
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
                this.tecs.setSelectedModuleSlot(val & 0x3); // 0..3
                this.tecs.setQuickMode((val >> 2) & 0x7); // 0..5
                this.tecs.inventoryChanged(TileEntityCreationStation.INV_ID_MODULES, 0); // The slot is not used
                break;
            case 2:
                this.fuelProgress = val; // value is 0..100, left furnace is in the lower bits 7..0
                break;
            case 3:
                this.smeltProgress = val; // value is 0..100, left furnace is in the lower bits 7..0
                break;
            case 4:
                this.tecs.lastInteractedCraftingGrid = val;
                break;
            default:
        }
    }
}
