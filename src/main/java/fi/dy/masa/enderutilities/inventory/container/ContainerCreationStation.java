package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerTileLargeStacks;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.SlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerCraftResult;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerFurnaceOutput;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.inventory.wrapper.InvWrapperSyncable;
import fi.dy.masa.enderutilities.inventory.wrapper.InventoryCraftingPermissions;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerCraftResult;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperPermissions;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class ContainerCreationStation extends ContainerTileLargeStacks
{
    protected final TileEntityCreationStation tecs;
    public int selectionsLast; // action mode and module selection
    public int fuelProgress;
    public int smeltProgress;
    private int modeMaskLast;
    private int lastInteractedCraftingGrid;

    private final InventoryCraftingPermissions[] craftingInventories;
    private final IItemHandler[] wrappedCraftingInventories;
    private final ItemHandlerCraftResult[] craftResults;
    private final IItemHandler furnaceInventory;
    private SlotRange craftingGridSlotsLeft;
    private SlotRange craftingGridSlotsRight;
    private int craftingOutputSlotLeft = -1;
    private int craftingOutputSlotRight = -1;
    private final NonNullList<ItemStack> recipeStacks = NonNullList.withSize(18, ItemStack.EMPTY);

    public ContainerCreationStation(EntityPlayer player, TileEntityCreationStation te)
    {
        super(player, te.getItemInventory(player), te);
        this.tecs = te;

        this.craftingInventories = new InventoryCraftingPermissions[] {
            te.getCraftingInventory(0, player, this),
            te.getCraftingInventory(1, player, this)
        };

        this.wrappedCraftingInventories = new IItemHandler[] {
            new InvWrapperSyncable(this.craftingInventories[0]),
            new InvWrapperSyncable(this.craftingInventories[1])
        };

        this.craftResults = new ItemHandlerCraftResult[] {
            te.getCraftResultInventory(0),
            te.getCraftResultInventory(1)
        };

        this.furnaceInventory = this.tecs.getFurnaceInventory();
        this.inventoryNonWrapped = (ItemHandlerWrapperPermissions) this.inventory;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(40, 174);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 40;
        int posY = 102;

        // Item inventory slots
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 27);

        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        // Add the module slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posX = 216;
        posY = 102;

        // The Storage Module slots
        // NOTE: These were moved to after the crafting grid slots, so that the grid slots get synced first when a card is removed,
        // before the inventory becomes inaccessible on the client side due to that card removal.
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
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.wrappedCraftingInventories[0], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }

        // Crafting result slot, left
        this.craftingOutputSlotLeft = this.inventorySlots.size();
        this.addSlotToContainer(new SlotItemHandlerCraftResult(this.craftingInventories[0], this.craftResults[0], 0, 112, 33, this.player));

        // Crafting slots, right side
        this.craftingGridSlotsRight = new SlotRange(this.inventorySlots.size(), 9);
        posX = 148;
        posY = 33;
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.wrappedCraftingInventories[1], j + i * 3, posX + j * 18, posY + i * 18));
            }
        }

        // Crafting result slot, right
        this.craftingOutputSlotRight = this.inventorySlots.size();
        this.addSlotToContainer(new SlotItemHandlerCraftResult(this.craftingInventories[1], this.craftResults[1], 0, 112, 69, this.player));

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

        this.craftingInventories[0].markDirty();
        this.craftingInventories[1].markDirty();
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

    public InventoryCraftingPermissions getCraftingInventory(int gridId)
    {
        return this.craftingInventories[gridId];
    }

    public ItemStack getStackOnCraftingGrid(int gridId, int slot)
    {
        return this.wrappedCraftingInventories[gridId].getStackInSlot(slot);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our main item inventory or the furnace inventory
        if (slot instanceof SlotItemHandler)
        {
            SlotItemHandler slotItemHandler = (SlotItemHandler) slot;

            if (slotItemHandler.getItemHandler() == this.inventory ||
                slotItemHandler.getItemHandler() == this.tecs.getFurnaceInventory())
            {
                return slotItemHandler.getItemStackLimit(stack);
            }
        }

        // Player inventory, module slots or crafting slots
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    protected boolean transferStackFromSlot(EntityPlayer player, int slotNum)
    {
        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (this.craftingOutputSlotLeft == slotNum || this.craftingOutputSlotRight == slotNum)
        {
            int invId = this.craftingOutputSlotRight == slotNum ? 1 : 0;

            if (this.tecs.canCraftItems(this.wrappedCraftingInventories[invId], invId) == false)
            {
                return false;
            }

            boolean ret = false;

            IItemHandler inv = this.tecs.getItemInventory(player);
            MergeSlotRange range = new MergeSlotRange(inv);
            ItemStack stackSlot = this.getSlot(slotNum).getStack();

            if (stackSlot.isEmpty() == false && InventoryUtils.matchingStackFoundInSlotRange(inv, range, stackSlot, false, false))
            {
                ret = super.transferStackToSlotRange(player, slotNum, range, false);
            }
            else
            {
                ret = super.transferStackFromSlot(player, slotNum);
            }

            this.tecs.restockCraftingGrid(this.wrappedCraftingInventories[invId], invId);

            return ret;
        }
        // Crafting grid slots, try to merge to the main item inventory first
        else if (this.craftingGridSlotsLeft.contains(slotNum) || this.craftingGridSlotsRight.contains(slotNum))
        {
            if (this.transferStackToSlotRange(player, slotNum, this.customInventorySlots, false))
            {
                return true;
            }
        }

        return super.transferStackFromSlot(player, slotNum);
    }

    @Override
    protected void shiftClickSlot(int slotNum, EntityPlayer player)
    {
        // Not a crafting output slot
        if (this.craftingOutputSlotLeft != slotNum && this.craftingOutputSlotRight != slotNum)
        {
            super.shiftClickSlot(slotNum, player);
            return;
        }

        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stackOrig = slot.getStack().copy();
            int num = 64;

            while (num-- > 0)
            {
                // Could not transfer the items, or ran out of some of the items, so the crafting result changed, bail out now
                if (this.transferStackFromSlot(player, slotNum) == false ||
                    InventoryUtils.areItemStacksEqual(stackOrig, slot.getStack()) == false)
                {
                    break;
                }
            }
        }
    }

    @Override
    protected void rightClickSlot(int slotNum, EntityPlayer player)
    {
        // Not a crafting output slot
        if (this.craftingOutputSlotLeft != slotNum && this.craftingOutputSlotRight != slotNum)
        {
            super.rightClickSlot(slotNum, player);
            return;
        }

        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stackOrig = slot.getStack().copy();
            int num = stackOrig.getMaxStackSize() / stackOrig.getCount();

            while (num-- > 0)
            {
                super.rightClickSlot(slotNum, player);

                // Ran out of some of the ingredients, so the crafting result changed, stop here
                if (InventoryUtils.areItemStacksEqual(stackOrig, slot.getStack()) == false)
                {
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        if (this.tecs.isInventoryAccessible(player) == false)
        {
            if (this.craftingGridSlotsLeft.contains(slotNum) || this.craftingGridSlotsRight.contains(slotNum))
            {
                return ItemStack.EMPTY;
            }
        }

        // Update the "last interacted on" crafting grid id, used for JEI recipe filling
        if (this.craftingGridSlotsLeft.contains(slotNum) || this.craftingOutputSlotLeft == slotNum)
        {
            this.tecs.lastInteractedCraftingGrid = 0;
        }
        else if (this.craftingGridSlotsRight.contains(slotNum) || this.craftingOutputSlotRight == slotNum)
        {
            this.tecs.lastInteractedCraftingGrid = 1;
        }

        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (this.craftingOutputSlotLeft == slotNum || this.craftingOutputSlotRight == slotNum)
        {
            int invId = this.craftingOutputSlotRight == slotNum ? 1 : 0;

            if (this.tecs.canCraftItems(this.wrappedCraftingInventories[invId], invId) == false)
            {
                return ItemStack.EMPTY;
            }

            ItemStack stack = super.slotClick(slotNum, dragType, clickType, player);

            if (this.isClient == false)
            {
                this.tecs.restockCraftingGrid(this.wrappedCraftingInventories[invId], invId);
                this.syncSlotToClient(slotNum);
                this.syncCursorStackToClient();
            }

            return stack;
        }

        super.slotClick(slotNum, dragType, clickType, player);

        // Memory Card slot, update the crafting output slots to prevent item dupes
        if (this.mergeSlotRangesPlayerToExt.get(0).contains(slotNum))
        {
            this.craftingInventories[0].markDirty();
            this.craftingInventories[1].markDirty();
        }

        return ItemStack.EMPTY;
    }

    public boolean isInventoryAccessible()
    {
        return this.tecs.isInventoryAccessible(this.player);
    }

    private void syncRecipeStacks()
    {
        int start = this.inventorySlots.size();

        for (int slot = 0; slot < this.recipeStacks.size(); slot++)
        {
            ItemStack currentStack = this.tecs.getRecipeItems(slot / 9).get(slot % 9);
            ItemStack prevStack = this.recipeStacks.get(slot);

            if (ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
            {
                prevStack = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                this.recipeStacks.set(slot, prevStack);

                for (int i = 0; i < this.listeners.size(); i++)
                {
                    IContainerListener listener = this.listeners.get(i);

                    if (listener instanceof EntityPlayerMP)
                    {
                        //System.out.printf("syncing recipe stack %d in inv %d\n", slot % 9, slot / 9);
                        PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, start + slot, prevStack), (EntityPlayerMP) listener);
                    }
                }
            }
        }
    }

    @Override
    public void syncStackInSlot(int slotId, ItemStack stack)
    {
        int size = this.inventorySlots.size();

        // Syncing the recipe items
        if (slotId >= size)
        {
            this.recipeStacks.set(slotId - size, stack);
        }
        else
        {
            super.syncStackInSlot(slotId, stack);
        }
    }

    public ItemStack getRecipeItem(int invId, int slot)
    {
        return this.recipeStacks.get(invId * 9 + slot);
    }

    public int getCraftingResultSlotNum(int gridId)
    {
        return gridId == 1 ? this.craftingOutputSlotRight : this.craftingOutputSlotLeft;
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModuleSlot();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        listener.sendWindowProperty(this, 0, modeMask);
        listener.sendWindowProperty(this, 1, selection);
        listener.sendWindowProperty(this, 2, fuelProgress);
        listener.sendWindowProperty(this, 3, smeltProgress);
        listener.sendWindowProperty(this, 4, this.tecs.lastInteractedCraftingGrid);

        this.detectAndSendChanges();
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.tecs.getWorld().isRemote)
        {
            return;
        }

        super.detectAndSendChanges();
        this.syncRecipeStacks();

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModuleSlot();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener listener = this.listeners.get(i);

            if (this.modeMaskLast != modeMask)
            {
                listener.sendWindowProperty(this, 0, modeMask);
            }
            if (this.selectionsLast != selection)
            {
                listener.sendWindowProperty(this, 1, selection);
            }
            if (this.fuelProgress != fuelProgress)
            {
                listener.sendWindowProperty(this, 2, fuelProgress);
            }
            if (this.smeltProgress != smeltProgress)
            {
                listener.sendWindowProperty(this, 3, smeltProgress);
            }
            if (this.lastInteractedCraftingGrid != this.tecs.lastInteractedCraftingGrid)
            {
                listener.sendWindowProperty(this, 4, this.tecs.lastInteractedCraftingGrid);
            }
        }

        this.modeMaskLast = modeMask;
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
                this.tecs.setModeMask(val);
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
