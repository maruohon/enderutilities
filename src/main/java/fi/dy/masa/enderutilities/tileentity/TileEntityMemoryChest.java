package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiMemoryChest;
import fi.dy.masa.enderutilities.inventory.ContainerMemoryChest;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TileEntityMemoryChest extends TileEntityEnderUtilitiesInventory implements ITieredStorage
{
    public static final int[] INV_SIZES = new int[] { 9, 27, 54 };

    protected ItemStack[] templateStacks;
    protected List<Integer> enabledTemplateSlots;
    protected int chestTier;
    protected long templateMask;
    protected int invSize;

    public TileEntityMemoryChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST);
        this.templateStacks = new ItemStack[54];
        this.enabledTemplateSlots = new ArrayList<Integer>();
    }

    private void initStorage()
    {
        this.itemHandler = new ItemStackHandlerTileEntity(this.invSize, this);
        this.itemHandlerExternal = new ItemHandlerWrapperMemoryChest(this.itemHandler, this);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.chestTier = MathHelper.clamp_int(nbt.getByte("ChestTier"), 0, 2);
        this.invSize = INV_SIZES[this.chestTier];
        this.setTemplateMask(nbt.getLong("TemplateMask"));

        super.readFromNBTCustom(nbt);
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        this.initStorage();

        this.templateStacks = new ItemStack[this.invSize];
        NBTUtils.readStoredItemsFromTag(nbt, this.templateStacks, "TemplateItems");

        super.readItemsFromNBT(nbt);
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        super.writeItemsToNBT(nbt);

        NBTUtils.writeItemsToTag(nbt, this.templateStacks, "TemplateItems", true);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("ChestTier", (byte)this.chestTier);
        nbt.setLong("TemplateMask", this.templateMask);

        super.writeToNBT(nbt);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("tier", (byte)this.chestTier);

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        this.chestTier = nbt.getByte("tier");
        this.invSize = INV_SIZES[this.chestTier];
        this.templateStacks = new ItemStack[this.invSize];

        super.onDataPacket(net, packet);
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
        this.templateStacks = new ItemStack[this.invSize];
        this.initStorage();
    }

    public long getTemplateMask()
    {
        return this.templateMask;
    }

    public void toggleTemplateMask(int slotNum)
    {
        this.setTemplateMask(this.templateMask ^ (1L << slotNum));
    }

    public void setTemplateMask(long mask)
    {
        this.templateMask = mask;

        this.enabledTemplateSlots.clear();
        long bit = 0x1;
        for (int i = 0; i < this.invSize; i++, bit <<= 1)
        {
            if ((this.templateMask & bit) != 0)
            {
                this.enabledTemplateSlots.add(i);
            }
        }
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
        }
    }

    private class ItemHandlerWrapperMemoryChest implements IItemHandlerModifiable
    {
        private final ItemStackHandlerBasic baseHandler;
        private final TileEntityMemoryChest temc;

        public ItemHandlerWrapperMemoryChest(ItemStackHandlerBasic baseHandler, TileEntityMemoryChest te)
        {
            this.baseHandler = baseHandler;
            this.temc = te;
        }

        @Override
        public int getSlots()
        {
            return this.baseHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return this.baseHandler.getStackInSlot(slot);
        }

        private boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            // Simple cases for allowing items in: no templated slots, or matching item already in the slot 
            if (this.temc.templateMask == 0 || stack == null || this.baseHandler.getStackInSlot(slot) != null)
            {
                return true;
            }

            // If trying to add into an empty slot, first make sure that there aren't templated slots
            // for this item type, that still have free space
            int max = Math.min(this.baseHandler.getInventoryStackLimit(), stack.getMaxStackSize());

            for (int i : this.temc.enabledTemplateSlots)
            {
                if (slot == i)
                {
                    //System.out.println("isValid slot match - " + (this.worldObj.isRemote ? "client" : "server"));
                    return InventoryUtils.areItemStacksEqual(stack, this.temc.templateStacks[slot]) == true;
                }

                // Space in the inventory slot for this template slot, and the input item matches the template item
                // => disallow putting the input item in slotNum, unless slotNum was this slot (see above check)
                if ((this.baseHandler.getStackInSlot(i) == null || this.baseHandler.getStackInSlot(i).stackSize < max) &&
                     InventoryUtils.areItemStacksEqual(stack, this.temc.templateStacks[i]) == true)
                {
                    //System.out.println("isValid denied - " + (this.worldObj.isRemote ? "client" : "server"));
                    return false;
                }
            }

            return true;

            /*// This is the simple version with no templated slot prioritization
            if ((this.templateMask & (1L << slotNum)) == 0)
            {
                return true;
            }

            return InventoryUtils.areItemStacksEqual(stack, this.templateStacks[slotNum]) == true;
            */
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            if (this.isItemValidForSlot(slot, stack) == true)
            {
                this.baseHandler.setStackInSlot(slot, stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (this.isItemValidForSlot(slot, stack) == true)
            {
                return this.baseHandler.insertItem(slot, stack, simulate);
            }

            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return this.baseHandler.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public ContainerMemoryChest getContainer(EntityPlayer player)
    {
        return new ContainerMemoryChest(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiMemoryChest(this.getContainer(player), this);
    }
}
