package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiTemplatedChest;
import fi.dy.masa.enderutilities.inventory.ContainerTemplatedChest;
import fi.dy.masa.enderutilities.inventory.IModularInventoryCallback;
import fi.dy.masa.enderutilities.inventory.InventoryItemCallback;
import fi.dy.masa.enderutilities.inventory.InventoryStackArray;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityTemplatedChest extends TileEntityEnderUtilitiesInventory implements ITieredStorage, IModularInventoryCallback
{
    public static final int GUI_ACTION_CHANGE_SELECTED_MODULE = 0;
    protected InventoryItemCallback itemInventory;
    protected InventoryStackArray moduleInventory;
    protected ItemStack[] templateStacks;
    protected ItemStack[] moduleStacks;
    protected int chestTier;
    protected int selectedModule;
    protected int invSizeItems;
    protected int templateMask;
    public static final int[] INV_SIZES = new int[] { 9, 27, 4 };

    public TileEntityTemplatedChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TEMPLATED_CHEST, 9);

        this.templateStacks = new ItemStack[27];

        this.itemInventory = new InventoryItemCallback(null, 27, true, null, this);

        this.moduleStacks = new ItemStack[4];
        this.moduleInventory = new InventoryStackArray(this.moduleStacks, 1, 4, false);
        this.moduleInventory.setInventoryCallback(this);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.chestTier = MathHelper.clamp_int(nbt.getByte("ChestTier"), 0, 2);
        this.templateMask = nbt.getInteger("TemplateMask");
        this.invSize = INV_SIZES[this.chestTier];
        this.invSizeItems = this.chestTier == 0 ? 9 : 27;

        super.readFromNBTCustom(nbt);

        this.templateStacks = this.readItemsFromNBT(nbt, this.invSizeItems, "TemplateItems");

        if (this.chestTier == 2)
        {
            this.selectedModule = nbt.getByte("SelModule");
            this.moduleStacks = this.readItemsFromNBT(nbt, 4, "ModuleItems");

            this.itemInventory = new InventoryItemCallback(this.moduleStacks[this.selectedModule], 27, false, null, this);

            this.moduleInventory = new InventoryStackArray(this.moduleStacks, 1, 4, false);
            this.moduleInventory.setInventoryCallback(this);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("ChestTier", (byte)this.chestTier);
        nbt.setInteger("TemplateMask", this.templateMask);

        this.writeItemsToNBT(nbt, this.templateStacks, "TemplateItems");

        if (this.chestTier == 2)
        {
            nbt.setByte("SelModule", (byte)this.selectedModule);
            this.writeItemsToNBT(nbt, this.moduleStacks, "ModuleItems");
        }

        super.writeToNBT(nbt);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("tier", (byte)this.chestTier);
        nbt.setInteger("tmpl", this.templateMask);

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();
        this.chestTier = nbt.getByte("tier");
        this.templateMask = nbt.getInteger("tmpl");
        this.invSize = INV_SIZES[this.chestTier];
        this.invSizeItems = this.chestTier == 0 ? 9 : 27;
        this.itemStacks = new ItemStack[this.invSize];

        if (this.chestTier == 2)
        {
            this.moduleInventory = new InventoryStackArray(this.moduleStacks, 1, 4, false);
            this.moduleInventory.setInventoryCallback(this);
        }

        super.onDataPacket(net, packet);
    }

    public IInventory getItemInventory()
    {
        return this.itemInventory;
    }

    public boolean isInventoryAccessible(EntityPlayer player)
    {
        return this.itemInventory.isUseableByPlayer(player);
    }

    public InventoryStackArray getModuleInventory()
    {
        return this.moduleInventory;
    }

    @Override
    public int getSizeInventory()
    {
        if (this.chestTier == 2)
        {
            return this.itemInventory.getSizeInventory();
        }

        return this.invSizeItems;
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (this.chestTier == 2)
        {
            ItemStack moduleStack = this.getContainerStack();
            if (moduleStack != null && moduleStack.getItem() instanceof IModule)
            {
                int tier = ((IModule) moduleStack.getItem()).getModuleTier(moduleStack);
                if (tier >= 6 && tier <= 12)
                {
                    return (int)Math.pow(2, tier);
                }
            }
        }

        return super.getInventoryStackLimit();
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        if (this.chestTier == 2)
        {
            this.itemInventory.markDirty();
            return this.itemInventory.getStackInSlot(slotNum);
        }

        return super.getStackInSlot(slotNum);
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        if (this.chestTier == 2)
        {
            return this.itemInventory.decrStackSize(slotNum, maxAmount);
        }

        return super.decrStackSize(slotNum, maxAmount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        if (this.chestTier == 2)
        {
            return this.itemInventory.getStackInSlotOnClosing(slotNum);
        }

        return super.getStackInSlotOnClosing(slotNum);
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack stack)
    {
        if (this.chestTier == 2)
        {
            this.itemInventory.setInventorySlotContents(slotNum, stack);
            return;
        }

        super.setInventorySlotContents(slotNum, stack);
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if ((this.templateMask & (1 << slotNum)) != 0 && InventoryUtils.areItemStacksEqual(stack, this.templateStacks[slotNum]) == false)
        {
            return false;
        }

        if (this.chestTier == 2)
        {
            return this.itemInventory.isItemValidForSlot(slotNum, stack);
        }

        return true;
    }

    @Override
    public ItemStack getContainerStack()
    {
        return this.moduleStacks[this.selectedModule];
    }

    @Override
    public void modularInventoryChanged()
    {
        this.itemInventory.setContainerItemStack(this.moduleStacks[this.selectedModule]);
    }

    @Override
    public int getStorageTier()
    {
        return this.chestTier;
    }

    @Override
    public void setStorageTier(int tier)
    {
        this.chestTier = tier;
    }

    public int getSelectedModule()
    {
        return this.selectedModule;
    }

    public void setSelectedModule(int index)
    {
        this.selectedModule = index;
    }

    public int getTemplateMask()
    {
        return this.templateMask;
    }

    public ItemStack getTemplateStack(int slotNum)
    {
        if (this.templateStacks != null && slotNum < this.templateStacks.length)
        {
            return this.templateStacks[slotNum];
        }

        return null;
    }

    public void setTemplateStack(int slotNum, ItemStack stack)
    {
        if (this.templateStacks != null && slotNum < this.templateStacks.length)
        {
            this.templateStacks[slotNum] = stack;
            this.templateMask ^= (1 << slotNum);
        }
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public void performGuiAction(int action, int element)
    {
        super.performGuiAction(action, element);

        if (action == GUI_ACTION_CHANGE_SELECTED_MODULE && element >= 0 && element < 4)
        {
            this.itemInventory.markDirty();
            this.selectedModule = element;
            this.modularInventoryChanged();
        }
    }

    @Override
    public ContainerTemplatedChest getContainer(InventoryPlayer inventoryPlayer)
    {
        return new ContainerTemplatedChest(inventoryPlayer, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiTemplatedChest(this.getContainer(inventoryPlayer), this);
    }
}
