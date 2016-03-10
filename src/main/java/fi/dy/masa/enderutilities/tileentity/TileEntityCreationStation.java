package fi.dy.masa.enderutilities.tileentity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import fi.dy.masa.enderutilities.gui.client.GuiCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerCreationStation;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;
import fi.dy.masa.enderutilities.inventory.InventoryItemCallback;
import fi.dy.masa.enderutilities.inventory.InventoryItemCrafting;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.ItemType;
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

    private final IItemHandlerModifiable itemHandlerMemoryCards;
    private final InventoryItemCallback itemInventory;
    private final ItemStackHandlerTileEntity furnaceInventory;
    private final IItemHandlerModifiable furnaceInventoryWrapper;

    protected InventoryItemCrafting[] craftingInventories;
    private final ItemStack[][] craftingGridTemplates;
    protected final IInventory[] craftResults;
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

    public TileEntityCreationStation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(INV_ID_MODULES, 4, 1, false, "Items", this);
        this.itemHandlerMemoryCards = new ItemHandlerWrapperMemoryCards(this.itemHandlerBase);

        this.itemInventory = new InventoryItemCallback(null, INV_SIZE_ITEMS, false, null, this);

        this.craftingInventories = new InventoryItemCrafting[2];
        this.craftingGridTemplates = new ItemStack[][] { null, null };
        this.craftResults = new InventoryCraftResult[] { new InventoryCraftResult(), new InventoryCraftResult() };
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
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.setSelectedModule(nbt.getByte("SelModule"));
        this.actionMode = nbt.getByte("QuickMode");
        this.modeMask = nbt.getByte("FurnaceMode");

        for (int i = 0; i < 2; i++)
        {
            this.burnTimeRemaining[i]  = nbt.getInteger("BurnTimeRemaining" + i);
            this.burnTimeFresh[i]      = nbt.getInteger("BurnTimeFresh" + i);
            this.cookTime[i]           = nbt.getInteger("CookTime" + i);
        }

        super.readFromNBTCustom(nbt);

        this.readModeMaskFromModule();
        this.loadRecipe(0, this.getRecipeId(0));
        this.loadRecipe(1, this.getRecipeId(1));
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        super.readItemsFromNBT(nbt);

        this.furnaceInventory.deserializeNBT(nbt);

        this.itemInventory.setContainerItemStack(this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
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
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        super.writeItemsToNBT(nbt);

        nbt.merge(this.furnaceInventory.serializeNBT());
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
        NBTTagCompound nbt = packet.getNbtCompound();

        this.selectedModule = nbt.getByte("msel");

        this.itemInventory.setIsRemote(true);
        this.itemInventory.setContainerItemStack(this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule));

        super.onDataPacket(net, packet);
    }

    public IItemHandler getItemInventory()
    {
        return this.itemInventory;
    }

    public InventoryItemCrafting getCraftingInventory(int id, Container container, EntityPlayer player)
    {
        if (this.craftingInventories[id] == null)
        {
            this.craftingInventories[id] = new InventoryItemCrafting(container, 3, 3, this.getContainerStack(),
                    this.worldObj.isRemote, player, this, "CraftItems_" + id);
            this.craftingInventories[id].readFromContainerItemStack();
        }

        return this.craftingInventories[id];
    }

    public IInventory getCraftResultInventory(int id)
    {
        return this.craftResults[id];
    }

    public IItemHandlerModifiable getMemoryCardInventory()
    {
        return this.itemHandlerMemoryCards;
    }

    public IItemHandlerModifiable getFurnaceInventory()
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
        this.selectedModule = MathHelper.clamp_int(index, 0, this.itemHandlerMemoryCards.getSlots() - 1);
    }

    public int getModeMask()
    {
        return this.modeMask;
    }

    protected int readModeMaskFromModule()
    {
        this.modeMask &= (MODE_BIT_LEFT_FAST | MODE_BIT_RIGHT_FAST);

        // Furnace modes are stored in the TileEntity itself, other modes are on the modules
        if (this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule) != null)
        {
            NBTTagCompound tag = NBTUtils.getCompoundTag(this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule), null, "CreationStation", false);
            if (tag != null)
            {
                this.modeMask |= tag.getShort("ConfigMask");
            }
        }

        return this.modeMask;
    }

    protected void writeModeMaskToModule()
    {
        //ItemStack stack = this.itemHandlerMemoryCards.extractItem(this.selectedModule, 1, false);
        ItemStack stack = this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule);
        if (stack != null)
        {
            // Furnace modes are stored in the TileEntity itself, other modes are on the modules
            NBTTagCompound tag = NBTUtils.getCompoundTag(stack, null, "CreationStation", true);
            tag.setShort("ConfigMask", (short)(this.modeMask & ~(MODE_BIT_LEFT_FAST | MODE_BIT_RIGHT_FAST)));
            //this.itemHandlerMemoryCards.insertItem(this.selectedModule, stack, false);
            this.itemHandlerMemoryCards.setStackInSlot(this.selectedModule, stack);
        }
    }

    public int getRecipeId(int invId)
    {
        int s = (invId == 1) ? 11 : 8;
        return (this.modeMask >> s) & 0x7;
    }

    public void setRecipeId(int invId, int recipeId)
    {
        int shift = (invId == 1) ? 11 : 8;
        int mask = (invId == 1) ? 0x3800 : 0x0700;
        this.modeMask = (this.modeMask & ~mask) | ((recipeId & 0x7) << shift);
    }

    public boolean getShowRecipe(int invId)
    {
        return invId == 1 ? (this.modeMask & MODE_BIT_SHOW_RECIPE_RIGHT) != 0 : (this.modeMask & MODE_BIT_SHOW_RECIPE_LEFT) != 0;
    }

    public void setShowRecipe(int invId, boolean show)
    {
        int mask = (invId == 1) ? MODE_BIT_SHOW_RECIPE_RIGHT : MODE_BIT_SHOW_RECIPE_LEFT;

        if (show == true)
        {
            this.modeMask |= mask;
        }
        else
        {
            this.modeMask &= ~mask;
        }
    }

    /**
     * Gets the result ItemStack from a stored recipe.
     * The recipe items will be in the recipeItems array
     * @param invId
     * @param recipeId
     */
    public void getRecipeOutput(int invId, int recipeId)
    {
        
    }

    /**
     * Gets the recipeItems array of ItemStacks for the currently selected recipe
     * @param invId
     */
    public ItemStack[] getRecipeItems(int invId)
    {
        invId = MathHelper.clamp_int(invId, 0, 1);
        return this.recipeItems[invId];
    }

    public NBTTagCompound getRecipeTag(int invId, int recipeId, boolean create)
    {
        ItemStack stack = this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule);
        if (stack == null)
        {
            return null;
        }

        return NBTUtils.getCompoundTag(stack, "CreationStation", "Recipes_" + invId, create);
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

    protected void storeRecipe(int invId, int recipeId)
    {
        invId = MathHelper.clamp_int(invId, 0, 1);
        IInventory invCrafting = this.craftingInventories[invId];
        if (invCrafting == null)
        {
            return;
        }

        NBTTagCompound tag = this.getRecipeTag(invId, recipeId, true);
        if (tag != null)
        {
            int invSize = invCrafting.getSizeInventory();
            //ItemStack items[] = new ItemStack[invCrafting.getSizeInventory() + 1];
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
    protected boolean addOneSetOfRecipeItemsIntoGrid(int invId, int recipeId)
    {
        invId = MathHelper.clamp_int(invId, 0, 1);
        IInventory invCrafting = this.craftingInventories[invId];
        if (invCrafting == null)
        {
            return false;
        }

        int maskOreDict = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_OREDICT : MODE_BIT_LEFT_CRAFTING_OREDICT;
        boolean useOreDict = (this.modeMask & maskOreDict) != 0;

        return InventoryUtils.restockInventoryBasedOnTemplate(invCrafting, this.itemInventory, this.getRecipeItems(invId), 1, true, useOreDict);
    }

    protected void fillCraftingGrid(int invId, int recipeId)
    {
        invId = MathHelper.clamp_int(invId, 0, 1);
        IInventory invCrafting = this.craftingInventories[invId];
        if (invCrafting == null)
        {
            return;
        }

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
        if (InventoryUtils.tryMoveAllItems(invCrafting, this.itemInventory, EnumFacing.UP, EnumFacing.UP, true) == true)
        {
            // Next we find out how many items we have available for each item type on the crafting grid
            // and we cap the max stack size to that value, so the stacks will be balanced
            Iterator<Entry<ItemType, Integer>> iter = slotCounts.entrySet().iterator();
            while (iter.hasNext() == true)
            {
                Entry<ItemType, Integer> entry = iter.next();
                ItemType item = entry.getKey();

                if (item.getStack().getMaxStackSize() == 1)
                {
                    continue;
                }

                int numFound = InventoryUtils.getNumberOfMatchingItemsInInventory(this.itemInventory, item.getStack(), useOreDict);
                int numSlots = entry.getValue();
                int maxSize = numFound / numSlots;

                if (maxSize < largestStack)
                {
                    largestStack = maxSize;
                }
            }

            InventoryUtils.restockInventoryBasedOnTemplate(invCrafting, this.itemInventory, template, largestStack, false, useOreDict);
        }
    }

    protected boolean clearCraftingGrid(int invId, EntityPlayer player)
    {
        IInventory inv = this.craftingInventories[MathHelper.clamp_int(invId, 0, 1)];
        if (inv == null)
        {
            return false;
        }

        if (InventoryUtils.tryMoveAllItems(inv, this.itemInventory, EnumFacing.UP, EnumFacing.UP, true) == false)
        {
            return InventoryUtils.tryMoveAllItems(inv, player.inventory, EnumFacing.UP, EnumFacing.UP, false);
        }

        return true;
    }

    /**
     * Check if there are enough items on the crafting grid to craft once, and try to add more items
     * if necessary and the auto-use feature is enabled.
     * @param invId
     * @return
     */
    public boolean canCraftItems(int invId)
    {
        invId = MathHelper.clamp_int(invId, 0, 1);
        int maskKeepOne = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_KEEPONE : MODE_BIT_LEFT_CRAFTING_KEEPONE;
        int maskAutoUse = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_AUTOUSE : MODE_BIT_LEFT_CRAFTING_AUTOUSE;

        this.craftingGridTemplates[invId] = null;

        IInventory invCrafting = this.craftingInventories[invId];
        if (invCrafting == null)
        {
            return false;
        }

        // Auto-use-items feature enabled, create a snapshot of the current state of the crafting grid
        if ((this.modeMask & maskAutoUse) != 0)
        {
            //if (invCrafting != null && InventoryUtils.checkInventoryHasAllItems(this.itemInventory, invCrafting, 1) == true)
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
            InventoryUtils.checkInventoryHasAllItems(this.itemInventory, invCrafting, 1, useOreDict) == true)
        {
            return true;
        }

        return false;
    }

    public void restockCraftingGrid(int invId)
    {
        this.recipeLoadClickCount = 0;
        invId = MathHelper.clamp_int(invId, 0, 1);
        int maskAutoUse = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_AUTOUSE : MODE_BIT_LEFT_CRAFTING_AUTOUSE;

        // Auto-use feature not enabled
        if ((this.modeMask & maskAutoUse) == 0)
        {
            return;
        }

        if (this.craftingInventories[invId] == null || this.craftingGridTemplates[invId] == null)
        {
            return;
        }

        int maskOreDict = invId == 1 ? MODE_BIT_RIGHT_CRAFTING_OREDICT : MODE_BIT_LEFT_CRAFTING_OREDICT;
        boolean useOreDict = (this.modeMask & maskOreDict) != 0;

        InventoryUtils.restockInventoryBasedOnTemplate(this.craftingInventories[invId], this.itemInventory,
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
        /*if (this.worldObj != null && this.worldObj.isRemote == true)
        {
            return;
        }*/

        //System.out.printf("%s - inventoryChanged\n", (this.worldObj.isRemote ? "client" : "server"));
        if (inventoryId == INV_ID_FURNACE)
        {
            // This gets called from the furnace inventory's markDirty
            this.inputDirty[0] = this.inputDirty[1] = true;
            return;
        }

        this.itemInventory.setContainerItemStack(this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule));
        this.readModeMaskFromModule();

        if (this.craftingInventories[0] != null)
        {
            this.craftingInventories[0].setContainerItemStack(this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule));
        }

        if (this.craftingInventories[1] != null)
        {
            this.craftingInventories[1].setContainerItemStack(this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule));
        }

        //if (this.worldObj.isRemote == false)
        {
            this.loadRecipe(0, this.getRecipeId(0));
            this.loadRecipe(1, this.getRecipeId(1));
        }
    }

    public void openInventory(EntityPlayer player)
    {
        this.numPlayersUsing++;
    }

    public void closeInventory(EntityPlayer player)
    {
        if (--this.numPlayersUsing <= 0)
        {
            this.numPlayersUsing = 0;
            this.craftingInventories[0] = null;
            this.craftingInventories[1] = null;
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
            this.itemInventory.onContentsChanged(element);
            this.setSelectedModule(element);
            this.inventoryChanged(INV_ID_MODULES, element);
        }
        else if (action == GUI_ACTION_MOVE_ITEMS && element >= 0 && element < 6)
        {
            if (this.itemInventory.isUseableByPlayer(player) == false)
            {
                return;
            }

            int playerMaxSlot = player.inventory.getSizeInventory() - 5;
            int chestMaxSlot = this.itemInventory.getSlots() - 1;
            EnumFacing up = EnumFacing.UP;

            switch (element)
            {
                case 0: // Move all items to Chest
                    InventoryUtils.tryMoveAllItemsWithinSlotRange(player.inventory, this.itemInventory, up, up, 0, playerMaxSlot, 0, chestMaxSlot, true);
                    break;
                case 1: // Move matching items to Chest
                    InventoryUtils.tryMoveMatchingItemsWithinSlotRange(player.inventory, this.itemInventory, up, up, 0, playerMaxSlot, 0, chestMaxSlot, true);
                    break;
                case 2: // Leave one stack of each item type and fill that stack
                    InventoryUtils.leaveOneFullStackOfEveryItem(player.inventory, this.itemInventory, false, false, true);
                    break;
                case 3: // Fill stacks in player inventory from Chest
                    InventoryUtils.fillStacksOfMatchingItemsWithinSlotRange(this.itemInventory, player.inventory, up, up, 0, chestMaxSlot, 0, playerMaxSlot, false);
                    break;
                case 4: // Move matching items to player inventory
                    InventoryUtils.tryMoveMatchingItemsWithinSlotRange(this.itemInventory, player.inventory, up, up, 0, chestMaxSlot, 0, playerMaxSlot, false);
                    break;
                case 5: // Move all items to player inventory
                    InventoryUtils.tryMoveAllItemsWithinSlotRange(this.itemInventory, player.inventory, up, up, 0, chestMaxSlot, 0, playerMaxSlot, false);
                    break;
            }
        }
        else if (action == GUI_ACTION_SET_QUICK_ACTION && element >= 0 && element < 6)
        {
            this.actionMode = element;
        }
        else if (action == GUI_ACTION_CLEAR_CRAFTING_GRID && element >= 0 && element < 2)
        {
            IInventory inv = this.craftingInventories[element];

            // Already empty crafting grid, set the "show recipe" mode to disabled
            if (InventoryUtils.isInventoryEmpty(inv) == true)
            {
                this.setShowRecipe(element, false);
                this.clearLoadedRecipe(element);
                this.writeModeMaskToModule();
            }
            // Items in grid, clear the grid
            else
            {
                this.clearCraftingGrid(element, player);
            }

            this.recipeLoadClickCount = 0;
        }
        else if (action == GUI_ACTION_RECIPE_LOAD && element >= 0 && element < 10)
        {
            int invId = element / 5;
            int recipeId = element % 5;

            // Clicked again on a recipe button that is already currently selected => load items into crafting grid
            if (this.getRecipeId(invId) == recipeId && this.getShowRecipe(invId) == true)
            {
                // First click after loading the recipe itself: load one item to each slot
                if (this.recipeLoadClickCount == 0)
                {
                    // First clear away the old contents
                    //if (this.clearCraftingGrid(invId, player) == true)
                    {
                        if (this.addOneSetOfRecipeItemsIntoGrid(invId, recipeId) == true)
                        {
                            this.recipeLoadClickCount += 1;
                        }
                    }
                }
                // Subsequent click will load the crafting grid with items up to either the largest stack size,
                // or the max stack size if the largest existing stack size is 1
                else
                {
                    this.fillCraftingGrid(invId, recipeId);
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
        }
        else if (action == GUI_ACTION_RECIPE_STORE && element >= 0 && element < 10)
        {
            int invId = element / 5;
            int recipeId = element % 5;

            /*IInventory inv = this.craftingInventories[invId];
            if (InventoryUtils.isInventoryEmpty(inv) == true)
            {
                this.setShowRecipe(invId, false);
            }
            else
            {
                this.storeRecipe(invId, recipeId);
                this.setShowRecipe(invId, true);
            }*/

            this.storeRecipe(invId, recipeId);
            this.setShowRecipe(invId, true);
            this.setRecipeId(invId, recipeId);
            this.writeModeMaskToModule();
        }
        else if (action == GUI_ACTION_RECIPE_CLEAR && element >= 0 && element < 10)
        {
            int invId = element / 5;
            int recipeId = element % 5;

            //if (this.getRecipeId(invId) == recipeId)
            {
                this.removeRecipe(invId, recipeId);
                this.setShowRecipe(invId, false);
                //this.setRecipeId(invId, recipeId);
                this.writeModeMaskToModule();
            }

            this.recipeLoadClickCount = 0;
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

            this.writeModeMaskToModule();
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
        else if (isFastMode == true)
        {
            cookTimeIncrement = COOKTIME_INC_FAST;
        }

        boolean canSmelt = this.canSmelt(id);
        // The furnace is currently burning fuel
        if (this.burnTimeRemaining[id] > 0)
        {
            int btUse = (isFastMode == true ? BURNTIME_USAGE_FAST : BURNTIME_USAGE_SLOW);

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
    public void update()
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

    private class ItemHandlerWrapperMemoryCards extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperMemoryCards(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        protected boolean isItemValidForSlot(int slot, ItemStack stack)
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

            // Check for a valid item-type Memory Card
            return  type.equals(ModuleType.TYPE_MEMORY_CARD_ITEMS) == true &&
                    module.getModuleTier(stack) >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B &&
                    module.getModuleTier(stack) <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;
        }

    }

    private class ItemHandlerWrapperFurnace extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperFurnace(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        protected boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return true;
            }

            if (slot == 0 || slot == 3)
            {
                return FurnaceRecipes.instance().getSmeltingResult(stack) != null;
            }

            return (slot == 1 || slot == 4) && TileEntityEnderFurnace.isItemFuel(stack) == true;
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
