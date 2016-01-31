package fi.dy.masa.enderutilities.tileentity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiHandyChest;
import fi.dy.masa.enderutilities.inventory.ContainerHandyChest;
import fi.dy.masa.enderutilities.inventory.IModularInventoryCallback;
import fi.dy.masa.enderutilities.inventory.InventoryItemCallback;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityHandyChest extends TileEntityEnderUtilitiesSided implements ITieredStorage, IModularInventoryCallback
{
    public static final int GUI_ACTION_SELECT_MODULE    = 0;
    public static final int GUI_ACTION_MOVE_ITEMS       = 1;
    public static final int GUI_ACTION_SET_QUICK_ACTION = 2;

    public static final int[] INV_SIZES = new int[] { 18, 36, 54 };

    protected InventoryItemCallback itemInventory;
    protected int selectedModule;
    protected int chestTier;
    protected int actionMode;
    protected int invSizeItems;
    protected Map<UUID, Long> clickTimes;

    public TileEntityHandyChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST, 4);

        this.itemInventory = new InventoryItemCallback(null, 18, false, null, this);
        this.clickTimes = new HashMap<UUID, Long>();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.chestTier = MathHelper.clamp_int(nbt.getByte("ChestTier"), 0, 2);
        this.invSizeItems = INV_SIZES[this.chestTier];
        this.setSelectedModule(nbt.getByte("SelModule"));
        this.actionMode = nbt.getByte("QuickMode");

        super.readFromNBTCustom(nbt);

        //this.itemInventory = new InventoryItemCallback(this.itemStacks[this.selectedModule], this.invSizeItems, false, null, this);
        this.itemInventory.setInventorySize(this.invSizeItems);
        this.itemInventory.setContainerItemStack(this.itemStacks[this.selectedModule]);
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
        this.invSizeItems = INV_SIZES[this.chestTier];

        this.itemInventory = new InventoryItemCallback(this.itemStacks[this.selectedModule], this.invSizeItems, true, null, this);

        super.onDataPacket(net, packet);
    }

    public IInventory getItemInventory()
    {
        return this.itemInventory;
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

    @Override
    public ItemStack getContainerStack()
    {
        return this.itemStacks[this.selectedModule];
    }

    @Override
    public void inventoryChanged(int invId)
    {
        this.itemInventory.setContainerItemStack(this.itemStacks[this.selectedModule]);
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
        this.invSizeItems = INV_SIZES[this.chestTier];
        this.itemInventory = new InventoryItemCallback(null, this.invSizeItems, this.worldObj.isRemote, null, this);
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
                return module.getModuleTier(stack) >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B && module.getModuleTier(stack) <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;
            }
        }

        return false;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        this.inventoryChanged(0);
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
            this.inventoryChanged(0);
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
