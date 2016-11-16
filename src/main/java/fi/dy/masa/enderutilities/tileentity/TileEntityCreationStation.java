package fi.dy.masa.enderutilities.tileentity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import fi.dy.masa.enderutilities.gui.client.GuiCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperPermissions;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerCreationStation;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemCallback;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils.InvResult;
import fi.dy.masa.enderutilities.util.ItemType;
import fi.dy.masa.enderutilities.util.SlotRange;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TileEntityCreationStation extends TileEntityEnderUtilitiesInventory implements IModularInventoryHolder, ITickable
{
    public static final int GUI_ACTION_SELECT_MODULE       = 0;
    public static final int GUI_ACTION_MOVE_ITEMS          = 1;
    public static final int GUI_ACTION_SET_QUICK_ACTION    = 2;
    public static final int GUI_ACTION_CLEAR_CRAFTING_GRID = 3;
    public static final int GUI_ACTION_RECIPE_LOAD         = 4;
    public static final int GUI_ACTION_RECIPE_STORE        = 5;
    public static final int GUI_ACTION_RECIPE_CLEAR        = 6;
    public static final int GUI_ACTION_TOGGLE_MODE         = 7;
    public static final int GUI_ACTION_SORT_ITEMS          = 8;

    public static final int INV_SIZE_ITEMS = 27;

    public static final int INV_ID_MODULES        = 1;
    public static final int INV_ID_CRAFTING_LEFT  = 2;
    public static final int INV_ID_CRAFTING_RIGHT = 3;
    public static final int INV_ID_FURNACE        = 4;

    public static final int COOKTIME_INC_SLOW = 12; // Slow/eco mode: 5 seconds per item
    public static final int COOKTIME_INC_FAST = 30; // Fast mode: 2 second per item (2.5x as fast)
    public static final int COOKTIME_DEFAULT = 1200; // Base cooktime per item: 5 seconds on slow

    public static final int BURNTIME_USAGE_SLOW = 20; // Slow/eco mode base usage
    public static final int BURNTIME_USAGE_FAST = 120; // Fast mode: use fuel 6x faster over time

    // The crafting mode button bits are in continuous order for easier checking in the gui
    public static final int MODE_BIT_LEFT_CRAFTING_OREDICT  = 0x0001;
    public static final int MODE_BIT_LEFT_CRAFTING_KEEPONE  = 0x0002;
    public static final int MODE_BIT_LEFT_CRAFTING_AUTOUSE  = 0x0004;
    public static final int MODE_BIT_RIGHT_CRAFTING_OREDICT = 0x0008;
    public static final int MODE_BIT_RIGHT_CRAFTING_KEEPONE = 0x0010;
    public static final int MODE_BIT_RIGHT_CRAFTING_AUTOUSE = 0x0020;
    public static final int MODE_BIT_LEFT_FAST              = 0x0040;
    public static final int MODE_BIT_RIGHT_FAST             = 0x0080;
    // Note: The selected recipe index is stored in bits 0x3F00 for right and left side (3 bits for each)
    public static final int MODE_BIT_SHOW_RECIPE_LEFT       = 0x4000;
    public static final int MODE_BIT_SHOW_RECIPE_RIGHT      = 0x8000;

    private InventoryItemCallback itemInventory;
    private ItemHandlerWrapperPermissions wrappedInventory;
    private final IItemHandler itemHandlerMemoryCards;
    private final ItemStackHandlerTileEntity furnaceInventory;
    private final IItemHandler furnaceInventoryWrapper;

    private final InventoryItemCallback[] craftingInventories;
    private final ItemStack[][] craftingGridTemplates;
    protected final ItemStackHandlerBasic[] craftResults;
    protected final ItemStack[][] recipeItems;
    protected int selectedModule;
    protected int actionMode;
    protected Map<UUID, Long> clickTimes;
    protected int numPlayersUsing;

    protected ItemStack[] smeltingResultCache;
    public int[] burnTimeRemaining;   // Remaining burn time from the currently burning fuel
    public int[] burnTimeFresh;       // The time the currently burning fuel will burn in total
    public int[] cookTime;            // The time the currently cooking item has been cooking for
    protected boolean[] inputDirty;
    protected int modeMask;
    protected byte furnaceMode;
    protected int recipeLoadClickCount;
    public int lastInteractedCraftingGrid;

    public TileEntityCreationStation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(INV_ID_MODULES, 4, 1, false, "Items", this);
        this.itemHandlerMemoryCards = new TileEntityHandyChest.ItemHandlerWrapperMemoryCards(this.getBaseItemHandler());

        this.craftingInventories = new InventoryItemCallback[2];
        this.craftingGridTemplates = new ItemStack[][] { null, null };
        this.craftResults = new ItemStackHandlerBasic[] { new ItemStackHandlerBasic(1), new ItemStackHandlerBasic(1) };
        this.recipeItems = new ItemStack[][] { new ItemStack[10], new ItemStack[10] };

        this.furnaceInventory = new ItemStackHandlerTileEntity(INV_ID_FURNACE, 6, 1024, true, "FurnaceItems", this);
        this.furnaceInventoryWrapper = new ItemHandlerWrapperFurnace(this.furnaceInventory);

        this.clickTimes = new HashMap<UUID, Long>();
        this.numPlayersUsing = 0;

        this.smeltingResultCache = new ItemStack[2];
        this.burnTimeRemaining = new int[2];
        this.burnTimeFresh = new int[2];
        this.cookTime = new int[2];
        this.inputDirty = new boolean[] { true, true };
        this.modeMask = 0;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();

        this.initStorage(this.getWorld().isRemote);
    }

    private void initStorage(boolean isRemote)
    {
        this.itemInventory = new InventoryItemCallback(null, INV_SIZE_ITEMS, true, isRemote, null, this);
        this.wrappedInventory = new ItemHandlerWrapperPermissions(this.itemInventory, null);
        this.itemHandlerExternal = this.wrappedInventory;

        this.craftingInventories[0] = new InventoryItemCallback(null, 9, 64, false, isRemote, null, this, "CraftItems_0");
        this.craftingInventories[1] = new InventoryItemCallback(null, 9, 64, false, isRemote, null, this, "CraftItems_1");

        ItemStack containerStack = this.getContainerStack();
        this.itemInventory.setContainerItemStack(containerStack);
        this.craftingInventories[0].setContainerItemStack(containerStack);
        this.craftingInventories[1].setContainerItemStack(containerStack);

        this.readModeMaskFromModule();
        this.loadRecipe(0, this.getRecipeId(0));
        this.loadRecipe(1, this.getRecipeId(1));
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.setSelectedModuleSlot(nbt.getByte("SelModule"));
        this.actionMode = nbt.getByte("QuickMode");
        this.modeMask = nbt.getByte("FurnaceMode");

        for (int i = 0; i < 2; i++)
        {
            this.burnTimeRemaining[i]  = nbt.getInteger("BurnTimeRemaining" + i);
            this.burnTimeFresh[i]      = nbt.getInteger("BurnTimeFresh" + i);
            this.cookTime[i]           = nbt.getInteger("CookTime" + i);
        }

        super.readFromNBTCustom(nbt);
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        // This will read the Memory Cards themselves into the Memory Card inventory
        super.readItemsFromNBT(nbt);

        this.furnaceInventory.deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("QuickMode", (byte)this.actionMode);
        nbt.setByte("SelModule", (byte)this.selectedModule);
        nbt.setByte("FurnaceMode", (byte)(this.modeMask & (MODE_BIT_LEFT_FAST | MODE_BIT_RIGHT_FAST)));

        for (int i = 0; i < 2; i++)
        {
            nbt.setInteger("BurnTimeRemaining" + i, this.burnTimeRemaining[i]);
            nbt.setInteger("BurnTimeFresh" + i, this.burnTimeFresh[i]);
            nbt.setInteger("CookTime" + i, this.cookTime[i]);
        }

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        super.writeItemsToNBT(nbt);

        nbt.merge(this.furnaceInventory.serializeNBT());
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("msel", (byte)this.selectedModule);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.selectedModule = tag.getByte("msel");

        super.handleUpdateTag(tag);
    }

    public IItemHandler getItemInventory(EntityPlayer player)
    {
        return new ItemHandlerWrapperPermissions(this.itemInventory, player);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return this.getItemInventory(player);
    }

    public ItemHandlerWrapperPermissions getCraftingInventoryWrapper(int invId, EntityPlayer player)
    {
        return new ItemHandlerWrapperPermissions(this.craftingInventories[invId], player);
    }

    public ItemStackHandlerBasic getCraftResultInventory(int invId)
    {
        return this.craftResults[invId];
    }

    public IItemHandler getMemoryCardInventory()
    {
        return this.itemHandlerMemoryCards;
    }

    public IItemHandler getFurnaceInventory()
    {
        return this.furnaceInventoryWrapper;
    }

    public int getQuickMode()
    {
        return this.actionMode;
    }

    public void setQuickMode(int mode)
    {
        this.actionMode = mode;
    }

    public int getSelectedModuleSlot()
    {
        return this.selectedModule;
    }

    public void setSelectedModuleSlot(int index)
    {
        this.selectedModule = MathHelper.clamp(index, 0, this.itemHandlerMemoryCards.getSlots() - 1);
    }

    public int getModeMask()
    {
        return this.modeMask;
    }

    protected int readModeMaskFromModule()
    {
        this.modeMask &= (MODE_BIT_LEFT_FAST | MODE_BIT_RIGHT_FAST);

        ItemStack containerStack = this.getContainerStack();

        // Furnace modes are stored in the TileEntity itself, other modes are on the modules
        if (containerStack != null)
        {
            NBTTagCompound tag = NBTUtils.getCompoundTag(containerStack, null, "CreationStation", false);
            if (tag != null)
            {
                this.modeMask |= tag.getShort("ConfigMask");
            }
        }

        return this.modeMask;
    }

    protected void writeModeMaskToModule()
    {
        // Cache the value locally, because taking out the memory card will trigger an update that will overwrite the
        // value stored in the field in the TE.
        int modeMask = this.modeMask;
        ItemStack stack = this.itemHandlerMemoryCards.extractItem(this.selectedModule, 1, false);
        //ItemStack stack = this.getContainerStack();
        if (stack != null)
        {
            // Furnace modes are stored in the TileEntity itself, other modes are on the modules
            NBTTagCompound tag = NBTUtils.getCompoundTag(stack, null, "CreationStation", true);
            tag.setShort("ConfigMask", (short)(modeMask & ~(MODE_BIT_LEFT_FAST | MODE_BIT_RIGHT_FAST)));
            this.itemHandlerMemoryCards.insertItem(this.selectedModule, stack, false);
            //this.itemHandlerMemoryCards.setStackInSlot(this.selectedModule, stack);
        }
    }

    protected int getRecipeId(int invId)
    {
        int s = (invId == 1) ? 11 : 8;
        return (this.modeMask >> s) & 0x7;
    }

    protected void setRecipeId(int invId, int recipeId)
    {
        int shift = (invId == 1) ? 11 : 8;
        int mask = (invId == 1) ? 0x3800 : 0x0700;
        this.modeMask = (this.modeMask & ~mask) | ((recipeId & 0x7) << shift);
    }

    public boolean getShowRecipe(int invId)
    {
        return invId == 1 ? (this.modeMask & MODE_BIT_SHOW_RECIPE_RIGHT) != 0 : (this.modeMask & MODE_BIT_SHOW_RECIPE_LEFT) != 0;
    }

    protected void setShowRecipe(int invId, boolean show)
    {
        int mask = (invId == 1) ? MODE_BIT_SHOW_RECIPE_RIGHT : MODE_BIT_SHOW_RECIPE_LEFT;

        if (show)
        {
            this.modeMask |= mask;
        }
        else
        {
            this.modeMask &= ~mask;
        }
    }

    /**
     * Gets the recipeItems array of ItemStacks for the currently selected recipe
     * @param invId
     */
    public ItemStack[] getRecipeItems(int invId)
    {
        return this.recipeItems[MathHelper.clamp(invId, 0, 1)];
    }

    protected NBTTagCompound getRecipeTag(int invId, int recipeId, boolean create)
    {
        ItemStack containerStack = this.getContainerStack();

        if (containerStack != null)
        {
            return NBTUtils.getCompoundTag(containerStack, "CreationStation", "Recipes_" + invId, create);
        }

        return null;
    }

    protected void loadRecipe(int invId, int recipeId)
    {
        NBTTagCompound tag = this.getRecipeTag(invId, recipeId, false);
        if (tag != null)
        {
            this.clearLoadedRecipe(invId);
            ItemStack items[] = this.getRecipeItems(invId);
            NBTUtils.readStoredItemsFromTag(tag, items, "Recipe_" + recipeId);
        }
        else
        {
            this.removeRecipe(invId, recipeId);
        }
    }

    protected void storeRecipe(IItemHandler invCrafting, int invId, int recipeId)
    {
        invId = MathHelper.clamp(invId, 0, 1);
        NBTTagCompound tag = this.getRecipeTag(invId, recipeId, true);
        if (tag != null)
        {
            int invSize = invCrafting.getSlots();
            ItemStack items[] = this.getRecipeItems(invId);

            for (int i = 0; i < invSize; i++)
            {
                ItemStack stack = invCrafting.getStackInSlot(i);
                if (stack != null)
                {
                    stack = stack.copy();
                    stack.stackSize = 1;
                }

                items[i] = stack;
            }

            // Store the recipe output item in the last slot, it will be used for GUI stuff
            items[items.length - 1] = ItemStack.copyItemStack(this.craftResults[invId].getStackInSlot(0));

            NBTUtils.writeItemsToTag(tag, items, "Recipe_" + recipeId, true);
        }
    }

    protected void clearLoadedRecipe(int invId)
    {
        ItemStack items[] = this.getRecipeItems(invId);
        for (int i = 0; i < items.length; i++)
        {
            items[i] = null;
        }
    }

    protected void removeRecipe(int invId, int recipeId)
    {
        NBTTagCompound tag = this.getRecipeTag(invId, recipeId, false);
        if (tag != null)
        {
            tag.removeTag("Recipe_" + recipeId);
        }

        this.clearLoadedRecipe(invId);
    }

    /**
     * Adds one more of each item in the recipe into the crafting grid, if possible
     * @param invId
     * @param recipeId
     */
    protected boolean addOneSetOfRecipeItemsIntoGrid(IItemHandler invCrafting, int invId, int recipeId, EntityPlayer player)
    {
        invId = MathHelper.clamp(invId, 0, 1);
        int maskOreDict = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_OREDICT : MODE_BIT_LEFT_CRAFTING_OREDICT;
        boolean useOreDict = (this.modeMask & maskOreDict) != 0;

        ItemStack[] template = this.getRecipeItems(invId);
        IItemHandlerModifiable playerInv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        IItemHandler invWrapper = new CombinedInvWrapper(this.itemInventory, playerInv);
        InventoryUtils.clearInventoryToMatchTemplate(invCrafting, invWrapper, template);

        return InventoryUtils.restockInventoryBasedOnTemplate(invCrafting, invWrapper, template, 1, true, useOreDict);
    }

    protected void fillCraftingGrid(IItemHandler invCrafting, int invId, int recipeId, EntityPlayer player)
    {
        invId = MathHelper.clamp(invId, 0, 1);
        int largestStack = InventoryUtils.getLargestExistingStackSize(invCrafting);
        // If all stacks only have one item, then try to fill them all the way to maxStackSize
        if (largestStack == 1)
        {
            largestStack = 64;
        }

        ItemStack[] template = InventoryUtils.createInventorySnapshot(invCrafting);
        int maskOreDict = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_OREDICT : MODE_BIT_LEFT_CRAFTING_OREDICT;
        boolean useOreDict = (this.modeMask & maskOreDict) != 0;

        Map<ItemType, Integer> slotCounts = InventoryUtils.getSlotCountPerItem(invCrafting);

        // Clear old contents and then fill all the slots back up
        if (InventoryUtils.tryMoveAllItems(invCrafting, this.itemInventory) == InvResult.MOVED_ALL)
        {
            IItemHandlerModifiable playerInv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
            IItemHandler invWrapper = new CombinedInvWrapper(this.itemInventory, playerInv);

            // Next we find out how many items we have available for each item type on the crafting grid
            // and we cap the max stack size to that value, so the stacks will be balanced
            Iterator<Entry<ItemType, Integer>> iter = slotCounts.entrySet().iterator();
            while (iter.hasNext())
            {
                Entry<ItemType, Integer> entry = iter.next();
                ItemType item = entry.getKey();

                if (item.getStack().getMaxStackSize() == 1)
                {
                    continue;
                }

                int numFound = InventoryUtils.getNumberOfMatchingItemsInInventory(invWrapper, item.getStack(), useOreDict);
                int numSlots = entry.getValue();
                int maxSize = numFound / numSlots;

                if (maxSize < largestStack)
                {
                    largestStack = maxSize;
                }
            }

            InventoryUtils.restockInventoryBasedOnTemplate(invCrafting, invWrapper, template, largestStack, false, useOreDict);
        }
    }

    protected InvResult clearCraftingGrid(IItemHandler invCrafting, int invId, EntityPlayer player)
    {
        if (InventoryUtils.tryMoveAllItems(invCrafting, this.itemInventory) != InvResult.MOVED_ALL)
        {
            return InventoryUtils.tryMoveAllItems(invCrafting, player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP));
        }

        return InvResult.MOVED_ALL;
    }

    /**
     * Check if there are enough items on the crafting grid to craft once, and try to add more items
     * if necessary and the auto-use feature is enabled.
     * @param invId
     * @return
     */
    public boolean canCraftItems(IItemHandler invCrafting, int invId)
    {
        invId = MathHelper.clamp(invId, 0, 1);
        int maskKeepOne = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_KEEPONE : MODE_BIT_LEFT_CRAFTING_KEEPONE;
        int maskAutoUse = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_AUTOUSE : MODE_BIT_LEFT_CRAFTING_AUTOUSE;

        this.craftingGridTemplates[invId] = null;

        if (invCrafting == null)
        {
            return false;
        }

        // Auto-use-items feature enabled, create a snapshot of the current state of the crafting grid
        if ((this.modeMask & maskAutoUse) != 0)
        {
            //if (invCrafting != null && InventoryUtils.checkInventoryHasAllItems(this.itemInventory, invCrafting, 1))
            this.craftingGridTemplates[invId] = InventoryUtils.createInventorySnapshot(invCrafting);
        }

        // No requirement to keep one item on the grid
        if ((this.modeMask & maskKeepOne) == 0)
        {
            return true;
        }
        // Need to keep one item on the grid; if auto-use is disabled and there is only one item left, then we can't craft anymore
        else if ((this.modeMask & maskAutoUse) == 0 && InventoryUtils.getMinNonEmptyStackSize(invCrafting) <= 1)
        {
            return false;
        }

        // We are required to keep one item on the grid after crafting.
        // So we must check that either there are more than one item left in each slot,
        // or that the auto-use feature is enabled and the inventory has all the required items
        // to re-stock the crafting grid afterwards.

        int maskOreDict = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_OREDICT : MODE_BIT_LEFT_CRAFTING_OREDICT;
        boolean useOreDict = (this.modeMask & maskOreDict) != 0;

        // More than one item left in each slot
        //if (InventoryUtils.getMinNonEmptyStackSize(invCrafting) > 1 || this.craftingGridTemplates[invId] != null)
        if (InventoryUtils.getMinNonEmptyStackSize(invCrafting) > 1 ||
            InventoryUtils.checkInventoryHasAllItems(this.itemInventory, invCrafting, 1, useOreDict))
        {
            return true;
        }

        return false;
    }

    public void restockCraftingGrid(IItemHandler invCrafting, int invId)
    {
        if (invCrafting == null || this.craftingGridTemplates[invId] == null)
        {
            return;
        }

        this.recipeLoadClickCount = 0;
        invId = MathHelper.clamp(invId, 0, 1);
        int maskAutoUse = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_AUTOUSE : MODE_BIT_LEFT_CRAFTING_AUTOUSE;

        // Auto-use feature not enabled
        if ((this.modeMask & maskAutoUse) == 0)
        {
            return;
        }

        int maskOreDict = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_OREDICT : MODE_BIT_LEFT_CRAFTING_OREDICT;
        boolean useOreDict = (this.modeMask & maskOreDict) != 0;

        InventoryUtils.clearInventoryToMatchTemplate(invCrafting, this.itemInventory, this.craftingGridTemplates[invId]);

        InventoryUtils.restockInventoryBasedOnTemplate(invCrafting, this.itemInventory,
                this.craftingGridTemplates[invId], 1, true, useOreDict);

        this.craftingGridTemplates[invId] = null;
    }

    @Override
    public ItemStack getContainerStack()
    {
        return this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        if (inventoryId == INV_ID_FURNACE)
        {
            // This gets called from the furnace inventory's markDirty
            this.inputDirty[0] = this.inputDirty[1] = true;
            return;
        }

        ItemStack containerStack = this.getContainerStack();

        this.itemInventory.setContainerItemStack(containerStack);
        this.craftingInventories[0].setContainerItemStack(containerStack);
        this.craftingInventories[1].setContainerItemStack(containerStack);

        this.readModeMaskFromModule();
        this.loadRecipe(0, this.getRecipeId(0));
        this.loadRecipe(1, this.getRecipeId(1));
    }

    public boolean isInventoryAccessible(EntityPlayer player)
    {
        return this.wrappedInventory.isAccessibleByPlayer(player);
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        if (this.getWorld().isRemote)
        {
            return;
        }

        Long last = this.clickTimes.get(player.getUniqueID());
        if (last != null && this.getWorld().getTotalWorldTime() - last < 5)
        {
            // Double left clicked fast enough (< 5 ticks) - do the selected item moving action
            this.performGuiAction(player, GUI_ACTION_MOVE_ITEMS, this.actionMode);
            this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.2f, 1.8f);
            this.clickTimes.remove(player.getUniqueID());
        }
        else
        {
            this.clickTimes.put(player.getUniqueID(), this.getWorld().getTotalWorldTime());
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < 4)
        {
            this.setSelectedModuleSlot(element);
            this.inventoryChanged(INV_ID_MODULES, element);
            this.markDirty();
        }
        else if (action == GUI_ACTION_MOVE_ITEMS && element >= 0 && element < 6)
        {
            ItemHandlerWrapperPermissions inventory = new ItemHandlerWrapperPermissions(this.itemInventory, player);

            if (inventory.isAccessibleByPlayer(player) == false)
            {
                return;
            }

            IItemHandlerModifiable playerMainInv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
            IItemHandlerModifiable offhandInv = new PlayerOffhandInvWrapper(player.inventory);
            IItemHandler playerInv = new CombinedInvWrapper(playerMainInv, offhandInv);

            switch (element)
            {
                case 0: // Move all items to Chest
                    InventoryUtils.tryMoveAllItemsWithinSlotRange(playerInv, inventory, new SlotRange(9, 27), new SlotRange(inventory));
                    break;
                case 1: // Move matching items to Chest
                    InventoryUtils.tryMoveMatchingItemsWithinSlotRange(playerInv, inventory, new SlotRange(9, 27), new SlotRange(inventory));
                    break;
                case 2: // Leave one stack of each item type and fill that stack
                    InventoryUtils.leaveOneFullStackOfEveryItem(playerInv, inventory, true);
                    break;
                case 3: // Fill stacks in player inventory from Chest
                    InventoryUtils.fillStacksOfMatchingItems(inventory, playerInv);
                    break;
                case 4: // Move matching items to player inventory
                    InventoryUtils.tryMoveMatchingItems(inventory, playerInv);
                    break;
                case 5: // Move all items to player inventory
                    InventoryUtils.tryMoveAllItems(inventory, playerInv);
                    break;
            }
        }
        else if (action == GUI_ACTION_SET_QUICK_ACTION && element >= 0 && element < 6)
        {
            this.actionMode = element;
        }
        else if (action == GUI_ACTION_CLEAR_CRAFTING_GRID && element >= 0 && element <= 1)
        {
            int invId = element;
            ItemHandlerWrapperPermissions inv = this.getCraftingInventoryWrapper(invId, player);

            if (inv.isAccessibleByPlayer(player) == false)
            {
                return;
            }

            // Already empty crafting grid, set the "show recipe" mode to disabled
            if (InventoryUtils.isInventoryEmpty(inv))
            {
                this.setShowRecipe(invId, false);
                this.clearLoadedRecipe(invId);
                this.writeModeMaskToModule();
            }
            // Items in grid, clear the grid
            else
            {
                this.clearCraftingGrid(inv, invId, player);
            }

            this.recipeLoadClickCount = 0;
            this.lastInteractedCraftingGrid = invId;
        }
        else if (action == GUI_ACTION_RECIPE_LOAD && element >= 0 && element < 10)
        {
            int invId = element / 5;
            int recipeId = element % 5;

            ItemHandlerWrapperPermissions inv = this.getCraftingInventoryWrapper(invId, player);

            if (inv.isAccessibleByPlayer(player) == false)
            {
                return;
            }

            // Clicked again on a recipe button that is already currently selected => load items into crafting grid
            if (this.getRecipeId(invId) == recipeId && this.getShowRecipe(invId))
            {
                // First click after loading the recipe itself: load one item to each slot
                if (this.recipeLoadClickCount == 0)
                {
                    // First clear away the old contents
                    //if (this.clearCraftingGrid(inv, invId, player) == InvResult.MOVED_ALL)
                    {
                        if (this.addOneSetOfRecipeItemsIntoGrid(inv, invId, recipeId, player))
                        {
                            this.recipeLoadClickCount += 1;
                        }
                    }
                }
                // Subsequent click will load the crafting grid with items up to either the largest stack size,
                // or the max stack size if the largest existing stack size is 1
                else
                {
                    this.fillCraftingGrid(inv, invId, recipeId, player);
                }
            }
            // Clicked on a different recipe button, or the recipe was hidden => load the recipe
            // and show it, but don't load the items into the grid
            else
            {
                this.loadRecipe(invId, recipeId);
                this.setRecipeId(invId, recipeId);
                this.recipeLoadClickCount = 0;
            }

            this.setShowRecipe(invId, true);
            this.writeModeMaskToModule();
            this.lastInteractedCraftingGrid = invId;
        }
        else if (action == GUI_ACTION_RECIPE_STORE && element >= 0 && element < 10)
        {
            int invId = element / 5;
            int recipeId = element % 5;

            ItemHandlerWrapperPermissions inv = this.getCraftingInventoryWrapper(invId, player);

            if (inv.isAccessibleByPlayer(player) == false)
            {
                return;
            }

            /*IItemHandler inv = this.craftingInventories[invId];
            if (InventoryUtils.isInventoryEmpty(inv))
            {
                this.setShowRecipe(invId, false);
            }
            else
            {
                this.storeRecipe(invId, recipeId);
                this.setShowRecipe(invId, true);
            }*/

            this.storeRecipe(inv, invId, recipeId);
            this.setShowRecipe(invId, true);
            this.setRecipeId(invId, recipeId);
            this.writeModeMaskToModule();
            this.lastInteractedCraftingGrid = invId;
        }
        else if (action == GUI_ACTION_RECIPE_CLEAR && element >= 0 && element < 10)
        {
            int invId = element / 5;
            int recipeId = element % 5;

            ItemHandlerWrapperPermissions inv = this.getCraftingInventoryWrapper(invId, player);

            if (inv.isAccessibleByPlayer(player) == false)
            {
                return;
            }

            //if (this.getRecipeId(invId) == recipeId)
            {
                this.removeRecipe(invId, recipeId);
                this.setShowRecipe(invId, false);
                //this.setRecipeId(invId, recipeId);
                this.writeModeMaskToModule();
            }

            this.recipeLoadClickCount = 0;
            this.lastInteractedCraftingGrid = invId;
        }
        else if (action == GUI_ACTION_TOGGLE_MODE && element >= 0 && element < 8)
        {
            switch (element)
            {
                case 0: this.modeMask ^= MODE_BIT_LEFT_CRAFTING_OREDICT; break;
                case 1: this.modeMask ^= MODE_BIT_LEFT_CRAFTING_KEEPONE; break;
                case 2: this.modeMask ^= MODE_BIT_LEFT_CRAFTING_AUTOUSE; break;
                case 3: this.modeMask ^= MODE_BIT_RIGHT_CRAFTING_AUTOUSE; break;
                case 4: this.modeMask ^= MODE_BIT_RIGHT_CRAFTING_KEEPONE; break;
                case 5: this.modeMask ^= MODE_BIT_RIGHT_CRAFTING_OREDICT; break;
                case 6: this.modeMask ^= MODE_BIT_LEFT_FAST; break;
                case 7: this.modeMask ^= MODE_BIT_RIGHT_FAST; break;
                default:
            }

            if (element <= 5)
            {
                this.lastInteractedCraftingGrid = element / 3;
            }

            this.writeModeMaskToModule();
        }
        else if (action == GUI_ACTION_SORT_ITEMS && element >= 0 && element <= 1)
        {
            // Station's item inventory
            if (element == 0)
            {
                ItemHandlerWrapperPermissions inventory = new ItemHandlerWrapperPermissions(this.itemInventory, player);

                if (inventory.isAccessibleByPlayer(player) == false)
                {
                    return;
                }

                InventoryUtils.sortInventoryWithinRange(this.itemInventory, new SlotRange(this.itemInventory));
            }
            // Player inventory (don't sort the hotbar)
            else
            {
                IItemHandlerModifiable inv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                InventoryUtils.sortInventoryWithinRange(inv, new SlotRange(9, 27));
            }
        }
    }

    /**
     * Updates the cached smelting result for the current input item, if the input has changed since last caching the result.
     */
    private void updateSmeltingResult(int id)
    {
        if (this.inputDirty[id])
        {
            ItemStack inputStack = this.furnaceInventory.getStackInSlot(id * 3);
            if (inputStack != null)
            {
                this.smeltingResultCache[id] = FurnaceRecipes.instance().getSmeltingResult(inputStack);
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
    protected boolean hasFuelAvailable(int id)
    {
        ItemStack fuelStack = this.furnaceInventory.getStackInSlot(id * 3 + 1);
        if (fuelStack == null)
        {
            return false;
        }

        return TileEntityEnderFurnace.itemContainsFluidFuel(fuelStack) ||
               TileEntityEnderFurnace.getItemBurnTime(fuelStack) > 0;
    }

    /**
     * Consumes one fuel item or one dose of fluid fuel. Sets the burnTimeFresh field to the amount of burn time gained.
     * @return returns the amount of furnace burn time that was gained from the fuel
     */
    protected int consumeFuelItem(int id)
    {
        if (this.furnaceInventory.getStackInSlot(id * 3 + 1) == null)
        {
            return 0;
        }

        ItemStack fuelStack = this.furnaceInventory.extractItem(id * 3 + 1, 1, false);
        int burnTime = TileEntityEnderFurnace.consumeFluidFuelDosage(fuelStack);

        // IFluidContainerItem items with lava
        if (burnTime > 0)
        {
            // Put the fuel/fluid container item back
            this.furnaceInventory.insertItem(id * 3 + 1, fuelStack, false);
            this.burnTimeFresh[id] = burnTime;
        }
        // Regular solid fuels
        else
        {
            burnTime = TileEntityEnderFurnace.getItemBurnTime(fuelStack);

            if (burnTime > 0)
            {
                this.burnTimeFresh[id] = burnTime;
                ItemStack containerStack = fuelStack.getItem().getContainerItem(fuelStack);

                if (this.furnaceInventory.getStackInSlot(id * 3 + 1) == null && containerStack != null)
                {
                    this.furnaceInventory.insertItem(id * 3 + 1, containerStack, false);
                }
            }
        }

        return burnTime;
    }

    /**
     * Returns true if the furnace can smelt an item. Checks the input slot for valid smeltable items and the output buffer
     * for an equal item and free space or empty buffer. Does not check the fuel.
     * @return true if input and output item stacks allow the current item to be smelted
     */
    protected boolean canSmelt(int id)
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
    protected void smeltItem(int id)
    {
        if (this.canSmelt(id))
        {
            this.furnaceInventory.insertItem(id * 3 + 2, this.smeltingResultCache[id], false);
            this.furnaceInventory.extractItem(id * 3, 1, false);

            if (this.furnaceInventory.getStackInSlot(id * 3) == null)
            {
                this.inputDirty[id] = true;
            }
        }
    }

    protected void smeltingLogic(int id)
    {
        this.updateSmeltingResult(id);

        boolean dirty = false;
        boolean hasFuel = this.hasFuelAvailable(id);
        boolean isFastMode = id == 0 ? (this.modeMask & MODE_BIT_LEFT_FAST) != 0 : (this.modeMask & MODE_BIT_RIGHT_FAST) != 0;

        int cookTimeIncrement = COOKTIME_INC_SLOW;
        if (this.burnTimeRemaining[id] == 0 && hasFuel == false)
        {
            return;
        }
        else if (isFastMode)
        {
            cookTimeIncrement = COOKTIME_INC_FAST;
        }

        boolean canSmelt = this.canSmelt(id);
        // The furnace is currently burning fuel
        if (this.burnTimeRemaining[id] > 0)
        {
            int btUse = (isFastMode ? BURNTIME_USAGE_FAST : BURNTIME_USAGE_SLOW);

            // Not enough fuel burn time remaining for the elapsed tick
            if (btUse > this.burnTimeRemaining[id])
            {
                if (hasFuel && canSmelt)
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
        else if (canSmelt && hasFuel)
        {
            this.burnTimeRemaining[id] += this.consumeFuelItem(id);
            hasFuel = this.hasFuelAvailable(id);
            dirty = true;
        }

        // Valid items to smelt, room in output
        if (canSmelt)
        {
            this.cookTime[id] += cookTimeIncrement;

            // One item done smelting
            if (this.cookTime[id] >= COOKTIME_DEFAULT)
            {
                this.smeltItem(id);
                canSmelt = this.canSmelt(id);

                // We can smelt the next item and we "overcooked" the last one, carry over the extra progress
                if (canSmelt && this.cookTime[id] > COOKTIME_DEFAULT)
                {
                    this.cookTime[id] -= COOKTIME_DEFAULT;
                }
                else // No more items to smelt or didn't overcook
                {
                    this.cookTime[id] = 0;
                }
            }

            // If the current fuel ran out and we still have items to cook, consume the next fuel item
            if (this.burnTimeRemaining[id] == 0 && hasFuel && canSmelt)
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

        if (dirty)
        {
            this.markDirty();
        }
    }

    @Override
    public void update()
    {
        if (this.getWorld().isRemote)
        {
            return;
        }

        this.smeltingLogic(0);
        this.smeltingLogic(1);
    }

    /**
     * Returns an integer between 0 and the passed value representing how close the current item is to being completely cooked
     */
    public int getSmeltProgressScaled(int id, int i)
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

    private class ItemHandlerWrapperFurnace extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapperFurnace(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return true;
            }

            if (slot == 0 || slot == 3)
            {
                return FurnaceRecipes.instance().getSmeltingResult(stack) != null;
            }

            return (slot == 1 || slot == 4) && TileEntityEnderFurnace.isItemFuel(stack);
        }

        /*
        @Override
        protected boolean canExtractFromSlot(int slot)
        {
            // 2 & 5: output slots; 1 & 4: fuel slots => allow pulling out from output slots, and non-fuel items (like empty buckets) from fuel slots
            if (  slot == 2 || slot == 5 ||
                ((slot == 1 || slot == 4) && TileEntityEnderFurnace.isItemFuel(this.getStackInSlot(slot)) == false))
            {
                return true;
            }

            return false;
        }
        */
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
