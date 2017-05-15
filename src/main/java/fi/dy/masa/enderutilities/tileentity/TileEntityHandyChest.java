package fi.dy.masa.enderutilities.tileentity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import fi.dy.masa.enderutilities.gui.client.GuiHandyChest;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyChest;
import fi.dy.masa.enderutilities.inventory.container.base.SlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemCallback;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperPermissions;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class TileEntityHandyChest extends TileEntityEnderUtilitiesInventory implements IModularInventoryHolder
{
    public static final int GUI_ACTION_SELECT_MODULE    = 0;
    public static final int GUI_ACTION_LOCK_MODULE      = 1;
    public static final int GUI_ACTION_MOVE_ITEMS       = 2;
    public static final int GUI_ACTION_SET_QUICK_ACTION = 3;
    public static final int GUI_ACTION_SORT_ITEMS       = 4;

    public static final int INV_ID_MEMORY_CARDS         = 0;
    public static final int INV_ID_ITEMS                = 1;
    public static final int MAX_TIER                    = 3;
    public static final int[] INV_SIZES = new int[] { 18, 36, 54, 104 };

    private final IItemHandler itemHandlerMemoryCards;
    protected InventoryItemCallback itemInventory;
    protected ItemHandlerWrapperPermissions wrappedInventory;
    protected int selectedModule;
    protected int chestTier;
    protected int actionMode;
    protected int lockedModules;
    protected int invSize;
    protected final Map<UUID, Long> clickTimes = new HashMap<UUID, Long>();

    public TileEntityHandyChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(INV_ID_MEMORY_CARDS, 4, 1, false, "Items", this);
        this.itemHandlerMemoryCards = new ItemHandlerWrapperMemoryCards(this.getBaseItemHandler());
    }

    private void initStorage(int invSize, boolean isRemote)
    {
        this.itemInventory = new InventoryItemCallback(null, invSize, true, isRemote, this);
        this.itemInventory.setContainerItemStack(this.getContainerStack());
        this.wrappedInventory = new ItemHandlerWrapperPermissions(this.itemInventory, null);
        this.itemHandlerExternal = this.wrappedInventory;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();

        this.initStorage(this.invSize, this.getWorld().isRemote);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.chestTier = MathHelper.clamp(nbt.getByte("ChestTier"), 0, MAX_TIER);
        this.actionMode = nbt.getByte("QuickMode");
        this.setSelectedModule(nbt.getByte("SelModule"));
        this.setLockMask(nbt.getByte("LockMask"));
        this.invSize = INV_SIZES[this.chestTier];

        // This needs to happen after setting the invSize and tier etc.
        super.readFromNBTCustom(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("ChestTier", (byte)this.chestTier);
        nbt.setByte("QuickMode", (byte)this.actionMode);
        nbt.setByte("SelModule", (byte)this.selectedModule);
        nbt.setByte("LockMask", (byte)this.lockedModules);

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte)this.chestTier);
        nbt.setByte("msel", (byte)this.selectedModule);
        nbt.setByte("lck", (byte)this.lockedModules);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.chestTier = tag.getByte("tier");
        this.selectedModule = tag.getByte("msel");
        this.setLockMask(tag.getByte("lck"));
        this.invSize = INV_SIZES[this.chestTier];

        // Needs re-initialization after the invSize has been set
        this.initStorage(this.invSize, this.getWorld().isRemote);

        super.handleUpdateTag(tag);
    }

    @Override
    public ItemHandlerWrapperPermissions getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperPermissions(this.itemInventory, player);
    }

    public boolean isInventoryAccessible(EntityPlayer player)
    {
        return this.wrappedInventory.isAccessibleByPlayer(player);
    }

    public IItemHandler getModuleInventory()
    {
        return this.itemHandlerMemoryCards;
    }

    public int getQuickMode()
    {
        return this.actionMode;
    }

    public void setQuickMode(int mode)
    {
        this.actionMode = mode;
    }

    public int getLockMask()
    {
        return this.lockedModules;
    }

    public void setLockMask(int mask)
    {
        this.lockedModules = mask;
    }

    public int getSelectedModule()
    {
        return this.selectedModule;
    }

    public void setSelectedModule(int index)
    {
        this.selectedModule = MathHelper.clamp(index, 0, this.itemHandlerMemoryCards.getSlots() - 1);
    }

    @Override
    public ItemStack getContainerStack()
    {
        return this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        this.itemInventory.setContainerItemStack(this.getContainerStack());
    }

    public int getStorageTier()
    {
        return this.chestTier;
    }

    public void setStorageTier(int tier)
    {
        this.chestTier = MathHelper.clamp(tier, 0, MAX_TIER);
        this.invSize = INV_SIZES[this.chestTier];

        this.initStorage(this.invSize, this.getWorld().isRemote);
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

    public static class ItemHandlerWrapperMemoryCards extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapperMemoryCards(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack.isEmpty())
            {
                return false;
            }

            if ((stack.getItem() instanceof IModule) == false)
            {
                return false;
            }

            IModule module = (IModule)stack.getItem();
            ModuleType type = module.getModuleType(stack);

            // Check for a valid item-type Memory Card
            return  type.equals(ModuleType.TYPE_MEMORY_CARD_ITEMS) &&
                    module.getModuleTier(stack) >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B &&
                    module.getModuleTier(stack) <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;
        }
    }

    private void toggleCardLocked(EntityPlayer player, int cardIndex)
    {
        ItemStack stack = this.itemHandlerMemoryCards.getStackInSlot(cardIndex);

        if (stack.isEmpty() == false)
        {
            OwnerData ownerData = OwnerData.getOwnerDataFromItem(stack);

            if (ownerData != null && ownerData.isOwner(player))
            {
                this.lockedModules ^= (1 << cardIndex);
                this.markDirty();
            }
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < 4)
        {
            this.setSelectedModule(element);
            this.inventoryChanged(0, element);
            this.markDirty();
        }
        else if (action == GUI_ACTION_LOCK_MODULE && element >= 0 && element < 4)
        {
            this.toggleCardLocked(player, element);
        }
        else if (action == GUI_ACTION_MOVE_ITEMS && element >= 0 && element < 6)
        {
            ItemHandlerWrapperPermissions inventory = this.getWrappedInventoryForContainer(player);

            if (inventory.isAccessibleByPlayer(player) == false)
            {
                return;
            }

            IItemHandlerModifiable playerMainInv = new PlayerMainInvWrapper(player.inventory);
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

            this.markDirty();
        }
        else if (action == GUI_ACTION_SET_QUICK_ACTION && element >= 0 && element < 6)
        {
            this.actionMode = element;
        }
        else if (action == GUI_ACTION_SORT_ITEMS && element >= 0 && element <= 1)
        {
            // Chest inventory
            if (element == 0)
            {
                ItemHandlerWrapperPermissions inventory = this.getWrappedInventoryForContainer(player);

                if (inventory.isAccessibleByPlayer(player) == false)
                {
                    return;
                }

                InventoryUtils.sortInventoryWithinRange(inventory, new SlotRange(this.itemInventory));
            }
            // Player inventory (don't sort the hotbar)
            else
            {
                IItemHandlerModifiable inv = (IItemHandlerModifiable) player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                InventoryUtils.sortInventoryWithinRange(inv, new SlotRange(9, 27));
            }
        }
    }

    @Override
    public ContainerHandyChest getContainer(EntityPlayer player)
    {
        return new ContainerHandyChest(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiHandyChest(this.getContainer(player), this);
    }
}
