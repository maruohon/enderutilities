package fi.dy.masa.enderutilities.tileentity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiHandyChest;
import fi.dy.masa.enderutilities.inventory.ContainerHandyChest;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;
import fi.dy.masa.enderutilities.inventory.InventoryItemCallback;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityHandyChest extends TileEntityEnderUtilitiesInventory implements ITieredStorage, IModularInventoryHolder
{
    public static final int GUI_ACTION_SELECT_MODULE    = 0;
    public static final int GUI_ACTION_MOVE_ITEMS       = 1;
    public static final int GUI_ACTION_SET_QUICK_ACTION = 2;

    public static final int INV_ID_MEMORY_CARDS         = 0;
    public static final int INV_ID_ITEMS                = 1;
    public static final int[] INV_SIZES = new int[] { 18, 36, 54 };

    private final IItemHandler itemHandlerMemoryCards;
    protected InventoryItemCallback itemInventory;
    protected int selectedModule;
    protected int chestTier;
    protected int actionMode;
    protected int invSize;
    protected final Map<UUID, Long> clickTimes;

    public TileEntityHandyChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(INV_ID_MEMORY_CARDS, 4, 1, false, "Items", this);
        this.itemHandlerMemoryCards = new ItemHandlerWrapperMemoryCards(this.itemHandlerBase);
        this.itemInventory = new InventoryItemCallback(null, 54, false, null, this);
        this.itemHandlerExternal = this.itemInventory;
        this.clickTimes = new HashMap<UUID, Long>();
    }

    private void initStorage(int invSize, boolean isRemote)
    {
        this.itemInventory = new InventoryItemCallback(null, invSize, isRemote, null, this);
        this.itemInventory.setContainerItemStack(this.getContainerStack());
        this.itemHandlerExternal = this.itemInventory;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.chestTier = MathHelper.clamp_int(nbt.getByte("ChestTier"), 0, 2);
        this.invSize = INV_SIZES[this.chestTier];
        this.setSelectedModule(nbt.getByte("SelModule"));
        this.actionMode = nbt.getByte("QuickMode");

        super.readFromNBTCustom(nbt);
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        // This will read the Memory Cards themselves into the Memory Card inventory
        super.readItemsFromNBT(nbt);

        // ... and this will read the item inventory from the selected Memory Card
        this.initStorage(this.invSize, false);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("ChestTier", (byte)this.chestTier);
        nbt.setByte("QuickMode", (byte)this.actionMode);
        nbt.setByte("SelModule", (byte)this.selectedModule);

        super.writeToNBT(nbt);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("tier", (byte)this.chestTier);
        nbt.setByte("msel", (byte)this.selectedModule);

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        this.chestTier = nbt.getByte("tier");
        this.selectedModule = nbt.getByte("msel");
        this.invSize = INV_SIZES[this.chestTier];

        this.initStorage(this.invSize, true);

        super.onDataPacket(net, packet);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
    {
        return this.itemHandlerExternal;
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

    @Override
    public ItemStack getContainerStack()
    {
        return this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        this.itemInventory.setContainerItemStack(this.itemHandlerMemoryCards.getStackInSlot(this.selectedModule));
    }

    @Override
    public int getStorageTier()
    {
        return this.chestTier;
    }

    @Override
    public void setStorageTier(int tier)
    {
        tier = MathHelper.clamp_int(tier, 0, 2);
        this.chestTier = tier;
        this.invSize = INV_SIZES[this.chestTier];

        this.initStorage(this.invSize, this.worldObj.isRemote);
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

    public static class ItemHandlerWrapperMemoryCards extends ItemHandlerWrapperSelectiveModifiable
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

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_SELECT_MODULE && element >= 0 && element < 4)
        {
            this.itemInventory.onContentsChanged(element);
            this.setSelectedModule(element);
            this.inventoryChanged(0, element);
        }
        else if (action == GUI_ACTION_MOVE_ITEMS && element >= 0 && element < 6)
        {
            if (this.itemInventory.isUseableByPlayer(player) == false)
            {
                return;
            }

            IItemHandler playerInv = new PlayerMainInvWrapper(player.inventory);

            switch (element)
            {
                case 0: // Move all items to Chest
                    InventoryUtils.tryMoveAllItems(playerInv, this.itemInventory);
                    break;
                case 1: // Move matching items to Chest
                    InventoryUtils.tryMoveMatchingItems(playerInv, this.itemInventory);
                    break;
                case 2: // Leave one stack of each item type and fill that stack
                    InventoryUtils.leaveOneFullStackOfEveryItem(playerInv, this.itemInventory, true);
                    break;
                case 3: // Fill stacks in player inventory from Chest
                    InventoryUtils.fillStacksOfMatchingItems(this.itemInventory, playerInv);
                    break;
                case 4: // Move matching items to player inventory
                    InventoryUtils.tryMoveMatchingItems(this.itemInventory, playerInv);
                    break;
                case 5: // Move all items to player inventory
                    InventoryUtils.tryMoveAllItems(this.itemInventory, playerInv);
                    break;
            }

            this.markDirty();
        }
        else if (action == GUI_ACTION_SET_QUICK_ACTION && element >= 0 && element < 6)
        {
            this.actionMode = element;
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
