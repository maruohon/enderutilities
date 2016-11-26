package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.enderutilities.inventory.InventoryCraftingWrapper;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperPermissions;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotCraftingWrapper;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerCraftresult;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerFurnaceOutput;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerCreationStation extends ContainerLargeStacks
{
    protected final TileEntityCreationStation tecs;
    public int selectionsLast; // action mode and module selection
    public int modeMask;
    public int fuelProgress;
    public int smeltProgress;
    private int lastInteractedCraftingGrid;

    private final ItemHandlerWrapperPermissions[] craftMatrixWrappers;
    private final InventoryCraftingWrapper[] craftMatrices;
    private final ItemStackHandlerBasic[] craftResults;
    private final IItemHandler furnaceInventory;
    private SlotRange craftingGridSlotsLeft;
    private SlotRange craftingGridSlotsRight;
    private final ItemStack[] recipeStacks;

    public ContainerCreationStation(EntityPlayer player, TileEntityCreationStation te)
    {
        super(player, te.getItemInventory(player));
        this.tecs = te;

        this.craftMatrixWrappers = new ItemHandlerWrapperPermissions[2];
        this.craftMatrixWrappers[0] = te.getCraftingInventoryWrapper(0, player);
        this.craftMatrixWrappers[1] = te.getCraftingInventoryWrapper(1, player);

        this.craftMatrices = new InventoryCraftingWrapper[2];
        this.craftMatrices[0] = new InventoryCraftingWrapper(this, 3, 3, this.craftMatrixWrappers[0]);
        this.craftMatrices[1] = new InventoryCraftingWrapper(this, 3, 3, this.craftMatrixWrappers[1]);

        this.craftResults = new ItemStackHandlerBasic[] { te.getCraftResultInventory(0), te.getCraftResultInventory(1) };
        this.recipeStacks = new ItemStack[18];

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
                this.addSlotToContainer(new SlotCraftingWrapper(this.craftMatrices[0], j + i * 3, posX + j * 18, posY + i * 18));
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
                this.addSlotToContainer(new SlotCraftingWrapper(this.craftMatrices[1], j + i * 3, posX + j * 18, posY + i * 18));
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
        this.craftResults[0].setStackInSlot(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[0], this.player.getEntityWorld()));
        this.craftResults[1].setStackInSlot(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrices[1], this.player.getEntityWorld()));
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
    protected boolean transferStackFromSlot(EntityPlayer player, int slotNum)
    {
        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (slotNum == 40 || slotNum == 50)
        {
            int invId = slotNum == 50 ? 1 : 0;

            if (this.tecs.canCraftItems(this.craftMatrixWrappers[invId], invId) == false)
            {
                return false;
            }

            boolean ret = false;

            IItemHandler inv = this.tecs.getItemInventory(player);
            MergeSlotRange range = new MergeSlotRange(inv);
            ItemStack stackSlot = this.getSlot(slotNum).getStack();

            if (stackSlot != null && InventoryUtils.matchingStackFoundInSlotRange(inv, range, stackSlot, false, false))
            {
                ret = super.transferStackToSlotRange(player, slotNum, range, false);
            }
            else
            {
                ret = super.transferStackFromSlot(player, slotNum);
            }

            this.tecs.restockCraftingGrid(this.craftMatrixWrappers[invId], invId);
            this.onCraftMatrixChanged(this.craftMatrices[invId]);

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
        if (slotNum != 40 && slotNum != 50)
        {
            super.shiftClickSlot(slotNum, player);
            return;
        }

        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackSlot = slot != null ? slot.getStack() : null;
        if (stackSlot == null)
        {
            return;
        }

        ItemStack stackOrig = stackSlot.copy();
        int num = 64;

        while (num-- > 0)
        {
            // Could not transfer the items, or ran out of some of the items, so the crafting result changed, bail out now
            if (this.transferStackFromSlot(player, slotNum) == false || InventoryUtils.areItemStacksEqual(stackOrig, slot.getStack()) == false)
            {
                break;
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
                return null;
            }
        }

        // Update the "last interacted on" crafting grid id, used for JEI recipe filling
        if (this.craftingGridSlotsLeft.contains(slotNum) || slotNum == 40)
        {
            this.tecs.lastInteractedCraftingGrid = 0;
        }
        else if (this.craftingGridSlotsRight.contains(slotNum) || slotNum == 50)
        {
            this.tecs.lastInteractedCraftingGrid = 1;
        }

        // Crafting output slots; if "keep one item" is enabled and the minimum remaining
        // stack size is 1 and the auto-use feature is not enabled, then we bail out
        if (slotNum == 40 || slotNum == 50)
        {
            int invId = slotNum == 50 ? 1 : 0;

            if (this.tecs.canCraftItems(this.craftMatrixWrappers[invId], invId) == false)
            {
                return null;
            }

            ItemStack stack = super.slotClick(slotNum, dragType, clickType, player);
            this.tecs.restockCraftingGrid(this.craftMatrixWrappers[invId], invId);
            this.onCraftMatrixChanged(this.craftMatrices[invId]);

            return stack;
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);

        int modeMask = this.tecs.getModeMask();
        int selection = this.tecs.getQuickMode() << 2 | this.tecs.getSelectedModuleSlot();
        int smeltProgress = this.tecs.getSmeltProgressScaled(1, 100) << 8 | this.tecs.getSmeltProgressScaled(0, 100);
        int fuelProgress = this.tecs.getBurnTimeRemainingScaled(1, 100) << 8 | this.tecs.getBurnTimeRemainingScaled(0, 100);

        listener.sendProgressBarUpdate(this, 0, modeMask);
        listener.sendProgressBarUpdate(this, 1, selection);
        listener.sendProgressBarUpdate(this, 2, fuelProgress);
        listener.sendProgressBarUpdate(this, 3, smeltProgress);
        listener.sendProgressBarUpdate(this, 4, this.tecs.lastInteractedCraftingGrid);

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

            if (this.modeMask != modeMask)
            {
                listener.sendProgressBarUpdate(this, 0, modeMask);
            }
            if (this.selectionsLast != selection)
            {
                listener.sendProgressBarUpdate(this, 1, selection);
            }
            if (this.fuelProgress != fuelProgress)
            {
                listener.sendProgressBarUpdate(this, 2, fuelProgress);
            }
            if (this.smeltProgress != smeltProgress)
            {
                listener.sendProgressBarUpdate(this, 3, smeltProgress);
            }
            if (this.lastInteractedCraftingGrid != this.tecs.lastInteractedCraftingGrid)
            {
                listener.sendProgressBarUpdate(this, 4, this.tecs.lastInteractedCraftingGrid);
            }
        }

        this.modeMask = modeMask;
        this.selectionsLast = selection;
        this.fuelProgress = fuelProgress;
        this.smeltProgress = smeltProgress;
        this.lastInteractedCraftingGrid = this.tecs.lastInteractedCraftingGrid;
    }

    private void syncRecipeStacks()
    {
        int start = this.inventorySlots.size();

        for (int slot = 0; slot < this.recipeStacks.length; slot++)
        {
            ItemStack currentStack = this.tecs.getRecipeItems(slot / 9)[slot % 9];
            ItemStack prevStack = this.recipeStacks[slot];

            if (ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
            {
                prevStack = ItemStack.copyItemStack(currentStack);
                this.recipeStacks[slot] = prevStack;

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
            this.recipeStacks[slotId - size] = stack;
        }
        else
        {
            super.syncStackInSlot(slotId, stack);
        }
    }

    public ItemStack getRecipeItem(int invId, int slot)
    {
        return this.recipeStacks[invId * 9 + slot];
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
