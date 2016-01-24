package fi.dy.masa.enderutilities.tileentity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.gui.client.GuiCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerCreationStation;
import fi.dy.masa.enderutilities.inventory.IModularInventoryCallback;
import fi.dy.masa.enderutilities.inventory.InventoryItemCallback;
import fi.dy.masa.enderutilities.inventory.InventoryItemCrafting;
import fi.dy.masa.enderutilities.inventory.InventoryStackArray;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityCreationStation extends TileEntityEnderUtilitiesSided implements IModularInventoryCallback
{
    public static final int GUI_ACTION_SELECT_MODULE       = 0;
    public static final int GUI_ACTION_MOVE_ITEMS          = 1;
    public static final int GUI_ACTION_SET_QUICK_ACTION    = 2;
    public static final int GUI_ACTION_CLEAR_CRAFTING_GRID = 3;
    public static final int GUI_ACTION_SET_CRAFTING_MODE   = 4;
    public static final int GUI_ACTION_RECALL_RECIPE       = 5;
    public static final int GUI_ACTION_SET_RECIPE          = 6;
    public static final int GUI_ACTION_TOGGLE_FURNACE_MODE = 7;

    public static final int INV_SIZE_ITEMS = 27;

    public static final int COOKTIME_INC_SLOW = 12; // Slow/eco mode: 5 seconds per item
    public static final int COOKTIME_INC_FAST = 30; // Fast mode: 2 second per item (2.5x as fast)
    public static final int COOKTIME_DEFAULT = 1200; // Base cooktime per item: 5 seconds on slow

    public static final int BURNTIME_USAGE_SLOW = 20; // Slow/eco mode base usage
    public static final int BURNTIME_USAGE_FAST = 120; // Fast mode: use fuel 6x faster over time

    protected InventoryItemCallback itemInventory;
    protected InventoryItemCrafting craftingInventoryLeft;
    protected InventoryItemCrafting craftingInventoryRight;
    protected final IInventory craftResultLeft = new InventoryCraftResult();
    protected final IInventory craftResultRight = new InventoryCraftResult();
    protected final InventoryStackArray furnaceInventory;
    protected ItemStack[] furnaceItems;
    protected int selectedModule;
    protected int actionMode;
    protected Map<UUID, Long> clickTimes;
    protected int numPlayersUsing;

    protected ItemStack[] smeltingResultCache;
    public int[] burnTimeRemaining;   // Remaining burn time from the currently burning fuel
    public int[] burnTimeFresh;       // The time the currently burning fuel will burn in total
    public int[] cookTime;            // The time the currently cooking item has been cooking for
    protected boolean[] inputDirty;
    protected boolean[] fastMode;

    public TileEntityCreationStation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION, 4);

        this.itemInventory = new InventoryItemCallback(null, INV_SIZE_ITEMS, false, null, this);

        this.furnaceItems = new ItemStack[6];
        this.furnaceInventory = new InventoryStackArray(this.furnaceItems, 576, 6, true, null);

        this.clickTimes = new HashMap<UUID, Long>();
        this.numPlayersUsing = 0;

        this.smeltingResultCache = new ItemStack[2];
        this.burnTimeRemaining = new int[2];
        this.burnTimeFresh = new int[2];
        this.cookTime = new int[2];
        this.inputDirty = new boolean[] { true, true };
        this.fastMode = new boolean[2];
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.setSelectedModule(nbt.getByte("SelModule"));
        this.actionMode = nbt.getByte("QuickMode");

        for (int i = 0; i < 2; i++)
        {
            this.burnTimeRemaining[i]  = nbt.getInteger("BurnTimeRemaining" + i);
            this.burnTimeFresh[i]      = nbt.getInteger("BurnTimeFresh" + i);
            this.cookTime[i]           = nbt.getInteger("CookTime" + i);
            this.fastMode[i]           = nbt.getBoolean("FastMode" + i);
        }

        super.readFromNBTCustom(nbt);

        this.furnaceItems = this.readItemsFromNBT(nbt, 6, "FurnaceItems");
        this.furnaceInventory.setStackArray(this.furnaceItems);

        this.itemInventory.setContainerItemStack(this.itemStacks[this.selectedModule]);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("QuickMode", (byte)this.actionMode);
        nbt.setByte("SelModule", (byte)this.selectedModule);

        for (int i = 0; i < 2; i++)
        {
            nbt.setInteger("BurnTimeRemaining" + i, this.burnTimeRemaining[i]);
            nbt.setInteger("BurnTimeFresh" + i, this.burnTimeFresh[i]);
            nbt.setInteger("CookTime" + i, this.cookTime[i]);
            nbt.setBoolean("FastMode",  this.fastMode[i]);
        }

        this.writeItemsToNBT(nbt, this.furnaceItems, "FurnaceItems");

        super.writeToNBT(nbt);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("msel", (byte)this.selectedModule);

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();

        this.selectedModule = nbt.getByte("msel");

        this.itemInventory = new InventoryItemCallback(this.itemStacks[this.selectedModule], INV_SIZE_ITEMS, true, null, this);

        super.onDataPacket(net, packet);
    }

    public IInventory getItemInventory()
    {
        return this.itemInventory;
    }

    public InventoryItemCrafting getCraftingInventoryLeft(Container container, EntityPlayer player)
    {
        if (this.craftingInventoryLeft == null)
        {
            this.craftingInventoryLeft = new InventoryItemCrafting(container, 3, 3, this.getContainerStack(),
                    this.worldObj.isRemote, player, this, "CraftingItemsLeft");
            this.craftingInventoryLeft.readFromContainerItemStack();
        }

        return this.craftingInventoryLeft;
    }

    public InventoryItemCrafting getCraftingInventoryRight(Container container, EntityPlayer player)
    {
        if (this.craftingInventoryRight == null)
        {
            this.craftingInventoryRight = new InventoryItemCrafting(container, 3, 3, this.getContainerStack(),
                    this.worldObj.isRemote, player, this, "CraftingItemsRight");
            this.craftingInventoryRight.readFromContainerItemStack();
        }

        return this.craftingInventoryRight;
    }

    public IInventory getCraftResultInventoryLeft()
    {
        return this.craftResultLeft;
    }

    public IInventory getCraftResultInventoryRight()
    {
        return this.craftResultRight;
    }

    public InventoryStackArray getFurnaceInventory()
    {
        return this.furnaceInventory;
    }

    public int getQuickMode()
    {
        return this.actionMode;
    }

    public void setQuickMode(int mode)
    {
        this.actionMode = mode;
    }

    public boolean isInventoryAccessible(EntityPlayer player)
    {
        return this.itemInventory.isUseableByPlayer(player);
    }

    public int getSelectedModule()
    {
        return this.selectedModule;
    }

    public void setSelectedModule(int index)
    {
        this.selectedModule = MathHelper.clamp_int(index, 0, this.invSize - 1);
    }

    public int getFastModeMask()
    {
        int mode = 0, bit = 0x1;

        for (int i = 0; i < 2; i++, bit <<= 1)
        {
            if (this.fastMode[i] == true)
            {
                mode |= bit;
            }
        }

        return mode;
    }

    @Override
    public ItemStack getContainerStack()
    {
        return this.itemStacks[this.selectedModule];
    }

    @Override
    public void modularInventoryChanged()
    {
        this.itemInventory.setContainerItemStack(this.itemStacks[this.selectedModule]);

        if (this.craftingInventoryLeft != null)
        {
            this.craftingInventoryLeft.setContainerItemStack(this.itemStacks[this.selectedModule]);
        }

        if (this.craftingInventoryRight != null)
        {
            this.craftingInventoryRight.setContainerItemStack(this.itemStacks[this.selectedModule]);
        }

        // This gets called from the furnace inventory's markDirty
        this.inputDirty[0] = this.inputDirty[1] = true;
    }

    @Override
    public int getSizeInventory()
    {
        return this.invSize;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (stack == null)
        {
            return true;
        }

        if ((stack.getItem() instanceof IModule) == false)
        {
            return false;
        }

        IModule module = (IModule)stack.getItem();
        ModuleType type = module.getModuleType(stack);

        if (type.equals(ModuleType.TYPE_INVALID) == false)
        {
            // Matching basic module type, check for the sub-type/tier
            if (type.equals(ModuleType.TYPE_MEMORY_CARD) == true)
            {
                return module.getModuleTier(stack) >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B &&
                       module.getModuleTier(stack) <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;
            }
        }

        return false;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        this.modularInventoryChanged();
    }

    @Override
    public void openInventory()
    {
        this.numPlayersUsing++;
    }

    @Override
    public void closeInventory()
    {
        if (--this.numPlayersUsing <= 0)
        {
            this.numPlayersUsing = 0;
            this.craftingInventoryLeft = null;
            this.craftingInventoryRight = null;
        }
    }

    public void onLeftClickBlock(EntityPlayer player)
    {
        if (this.worldObj.isRemote == true)
        {
            return;
        }

        Long last = this.clickTimes.get(player.getUniqueID());
        if (last != null && this.worldObj.getTotalWorldTime() - last < 5)
        {
            // Double left clicked fast enough (< 5 ticks) - do the selected item moving action
            this.performGuiAction(player, GUI_ACTION_MOVE_ITEMS, this.actionMode);
            player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.2f, 1.8f);
            this.clickTimes.remove(player.getUniqueID());
        }
        else
        {
            this.clickTimes.put(player.getUniqueID(), this.worldObj.getTotalWorldTime());
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < 4)
        {
            this.itemInventory.markDirty();
            this.setSelectedModule(element);
            this.modularInventoryChanged();
        }
        else if (action == GUI_ACTION_MOVE_ITEMS && element >= 0 && element < 6)
        {
            IInventory inv = this.itemInventory;
            if (inv.isUseableByPlayer(player) == false)
            {
                return;
            }

            int playerMaxSlot = player.inventory.getSizeInventory() - 5;
            int chestMaxSlot = this.itemInventory.getSizeInventory() - 1;

            switch (element)
            {
                case 0: // Move all items to Chest
                    InventoryUtils.tryMoveAllItemsWithinSlotRange(player.inventory, inv, 0, 0, 0, playerMaxSlot, 0, chestMaxSlot, true);
                    break;
                case 1: // Move matching items to Chest
                    InventoryUtils.tryMoveMatchingItemsWithinSlotRange(player.inventory, inv, 0, 0, 0, playerMaxSlot, 0, chestMaxSlot, true);
                    break;
                case 2: // Leave one stack of each item type and fill that stack
                    InventoryUtils.leaveOneFullStackOfEveryItem(player.inventory, inv, false, false, true);
                    break;
                case 3: // Fill stacks in player inventory from Chest
                    InventoryUtils.fillStacksOfMatchingItemsWithinSlotRange(inv, player.inventory, 0, 0, 0, chestMaxSlot, 0, playerMaxSlot, false);
                    break;
                case 4: // Move matching items to player inventory
                    InventoryUtils.tryMoveMatchingItemsWithinSlotRange(inv, player.inventory, 0, 0, 0, chestMaxSlot, 0, playerMaxSlot, false);
                    break;
                case 5: // Move all items to player inventory
                    InventoryUtils.tryMoveAllItemsWithinSlotRange(inv, player.inventory, 0, 0, 0, chestMaxSlot, 0, playerMaxSlot, false);
                    break;
            }
        }
        else if (action == GUI_ACTION_SET_QUICK_ACTION && element >= 0 && element < 6)
        {
            this.actionMode = element;
        }
        else if (action == GUI_ACTION_CLEAR_CRAFTING_GRID && element >= 0 && element < 2)
        {
            IInventory inv = element == 0 ? this.craftingInventoryLeft : this.craftingInventoryRight;

            if (InventoryUtils.tryMoveAllItems(inv, this.itemInventory, 0, 0, true) == false)
            {
                InventoryUtils.tryMoveAllItems(inv, player.inventory, 0, 0, false);
            }
        }
        else if (action == GUI_ACTION_SET_CRAFTING_MODE && element >= 0 && element < 2)
        {
            
        }
        else if (action == GUI_ACTION_RECALL_RECIPE && element >= 0 && element < 2)
        {
            IInventory inv = element == 0 ? this.craftingInventoryLeft : this.craftingInventoryRight;

            if (InventoryUtils.tryMoveAllItems(inv, this.itemInventory, 0, 0, true) == true ||
                InventoryUtils.tryMoveAllItems(inv, player.inventory, 0, 0, false) == true)
            {
                ;
            }
        }
        else if (action == GUI_ACTION_SET_RECIPE && element >= 0 && element < 2)
        {
        }
        else if (action == GUI_ACTION_TOGGLE_FURNACE_MODE && element >= 0 && element < 2)
        {
            this.fastMode[element] = ! this.fastMode[element];
        }
    }

    public boolean isBurning(int id)
    {
        // This returns if the furnace is actually burning fuel at the moment
        return this.burnTimeRemaining[id] > 0;
    }

    /**
     * Updates the cached smelting result for the current input item, if the input has changed since last caching the result.
     */
    private void updateSmeltingResult(int id)
    {
        if (this.inputDirty[id] == true)
        {
            ItemStack inputStack = this.furnaceInventory.getStackInSlot(id * 3);
            if (inputStack != null)
            {
                this.smeltingResultCache[id] = FurnaceRecipes.smelting().getSmeltingResult(inputStack);
            }
            else
            {
                this.smeltingResultCache[id] = null;
            }

            this.inputDirty[id] = false;
        }
    }

    /**
     * Checks if there is a valid fuel item in the fuel slot.
     * @return true if the fuel slot has an item that can be used as fuel
     */
    public boolean hasFuelAvailable(int id)
    {
        ItemStack fuelStack = this.furnaceInventory.getStackInSlot(id * 3 + 1);
        if (fuelStack == null)
        {
            return false;
        }

        return TileEntityEnderFurnace.itemContainsFluidFuel(fuelStack) == true ||
               TileEntityEnderFurnace.getItemBurnTime(fuelStack) > 0;
    }

    /**
     * Consumes one fuel item or one dose of fluid fuel. Sets the burnTimeFresh field to the amount of burn time gained.
     * @return returns the amount of furnace burn time that was gained from the fuel
     */
    public int consumeFuelItem(int id)
    {
        ItemStack fuelStack = this.furnaceInventory.getStackInSlot(id * 3 + 1);

        if (fuelStack == null)
        {
            return 0;
        }

        int burnTime = TileEntityEnderFurnace.consumeFluidFuelDosage(fuelStack);

        // IFluidContainerItem items with lava
        if (burnTime > 0)
        {
            this.burnTimeFresh[id] = burnTime;
        }
        // Regular solid fuels
        else
        {
            burnTime = TileEntityEnderFurnace.getItemBurnTime(fuelStack);

            if (burnTime > 0)
            {
                if (--fuelStack.stackSize <= 0)
                {
                    fuelStack = fuelStack.getItem().getContainerItem(fuelStack);
                }

                this.burnTimeFresh[id] = burnTime;
                this.furnaceInventory.setInventorySlotContents(id * 3 + 1, fuelStack.stackSize > 0 ? fuelStack : null);
            }
        }

        return burnTime;
    }

    /**
     * Returns true if the furnace can smelt an item. Checks the input slot for valid smeltable items and the output buffer
     * for an equal item and free space or empty buffer. Does not check the fuel.
     * @return true if input and output item stacks allow the current item to be smelted
     */
    public boolean canSmelt(int id)
    {
        ItemStack inputStack = this.furnaceInventory.getStackInSlot(id * 3);

        if (inputStack == null || this.smeltingResultCache[id] == null)
        {
            return false;
        }

        int amount = 0;
        ItemStack outputStack = this.furnaceInventory.getStackInSlot(id * 3 + 2);
        if (outputStack != null)
        {
            if (InventoryUtils.areItemStacksEqual(this.smeltingResultCache[id], outputStack) == false)
            {
                return false;
            }

            amount = outputStack.stackSize;
        }

        if ((this.furnaceInventory.getInventoryStackLimit() - amount) < this.smeltingResultCache[id].stackSize)
        {
            return false;
        }

        return true;
    }

    /**
     * Turn one item from the furnace input slot into a smelted item in the furnace output buffer.
     */
    public void smeltItem(int id)
    {
        if (this.canSmelt(id) == true)
        {
            // Output stack
            ItemStack stack = this.furnaceInventory.getStackInSlot(id * 3 + 2);

            if (stack == null)
            {
                stack = this.smeltingResultCache[id].copy();
            }
            else
            {
                stack.stackSize += this.smeltingResultCache[id].stackSize;
            }

            this.furnaceInventory.setInventorySlotContents(id * 3 + 2, stack);

            // Input stack
            stack = this.furnaceInventory.getStackInSlot(id * 3);

            if (--stack.stackSize <= 0)
            {
                stack = null;
                this.inputDirty[id] = true;
            }

            this.furnaceInventory.setInventorySlotContents(id * 3, stack);
        }
    }

    protected void smeltingLogic(int id)
    {
        this.updateSmeltingResult(id);

        boolean dirty = false;
        boolean hasFuel = this.hasFuelAvailable(id);

        int cookTimeIncrement = COOKTIME_INC_SLOW;
        if (this.burnTimeRemaining[id] == 0 && hasFuel == false)
        {
            return;
        }
        else if (this.fastMode[id] == true)
        {
            cookTimeIncrement = COOKTIME_INC_FAST;
        }

        boolean canSmelt = this.canSmelt(id);
        // The furnace is currently burning fuel
        if (this.burnTimeRemaining[id] > 0)
        {
            int btUse = (this.fastMode[id] == true ? BURNTIME_USAGE_FAST : BURNTIME_USAGE_SLOW);

            // Not enough fuel burn time remaining for the elapsed tick
            if (btUse > this.burnTimeRemaining[id])
            {
                if (hasFuel == true && canSmelt == true)
                {
                    this.burnTimeRemaining[id] += this.consumeFuelItem(id);
                    hasFuel = this.hasFuelAvailable(id);
                }
                // Running out of fuel, scale the cook progress according to the elapsed burn time
                else
                {
                    cookTimeIncrement = (this.burnTimeRemaining[id] * cookTimeIncrement) / btUse;
                    btUse = this.burnTimeRemaining[id];
                }
            }

            this.burnTimeRemaining[id] -= btUse;
            dirty = true;
        }
        // Furnace wasn't burning, but it now has fuel and smeltable items, start burning/smelting
        else if (canSmelt == true && hasFuel == true)
        {
            this.burnTimeRemaining[id] += this.consumeFuelItem(id);
            hasFuel = this.hasFuelAvailable(id);
            dirty = true;
        }

        // Valid items to smelt, room in output
        if (canSmelt == true)
        {
            this.cookTime[id] += cookTimeIncrement;

            // One item done smelting
            if (this.cookTime[id] >= COOKTIME_DEFAULT)
            {
                this.smeltItem(id);
                canSmelt = this.canSmelt(id);

                // We can smelt the next item and we "overcooked" the last one, carry over the extra progress
                if (canSmelt == true && this.cookTime[id] > COOKTIME_DEFAULT)
                {
                    this.cookTime[id] -= COOKTIME_DEFAULT;
                }
                else // No more items to smelt or didn't overcook
                {
                    this.cookTime[id] = 0;
                }
            }

            // If the current fuel ran out and we still have items to cook, consume the next fuel item
            if (this.burnTimeRemaining[id] == 0 && hasFuel == true && canSmelt == true)
            {
                this.burnTimeRemaining[id] += this.consumeFuelItem(id);
            }

            dirty = true;
        }
        // Can't smelt anything at the moment, rewind the cooking progress at half the speed of normal cooking
        else if (this.cookTime[id] > 0)
        {
            this.cookTime[id] -= Math.min(this.cookTime[id], COOKTIME_INC_SLOW / 2);
            dirty = true;
        }

        if (dirty == true)
        {
            this.markDirty();
        }
    }

    @Override
    public void updateEntity()
    {
        if (this.worldObj.isRemote == true)
        {
            return;
        }

        this.smeltingLogic(0);
        this.smeltingLogic(1);
    }

    /**
     * Returns an integer between 0 and the passed value representing how close the current item is to being completely cooked
     */
    public int getCookProgressScaled(int id, int i)
    {
        return this.cookTime[id] * i / COOKTIME_DEFAULT;
    }

    /**
     * Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
     * item, where 0 means that the item is exhausted and the passed value means that the item is fresh
     */
    public int getBurnTimeRemainingScaled(int id, int i)
    {
        if (this.burnTimeFresh[id] == 0)
        {
            return 0;
        }

        return this.burnTimeRemaining[id] * i / this.burnTimeFresh[id];
    }

    @Override
    public ContainerCreationStation getContainer(EntityPlayer player)
    {
        return new ContainerCreationStation(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiCreationStation(this.getContainer(player), this);
    }
}
