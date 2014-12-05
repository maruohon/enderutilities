package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesSided
{
    private static final int[] SLOTS = new int[0];

    public TileEntityToolWorkstation()
    {
        super(ReferenceBlocksItems.NAME_TILEENTITY_TOOL_WORKSTATION);
        this.itemStacks = new ItemStack[20];
    }

    private void writeModulesToItem(int toolSlotNum, int slotStart, int numModuleSlots)
    {
        if (toolSlotNum >= this.itemStacks.length)
        {
            return;
        }

        if (this.itemStacks[toolSlotNum] != null && this.itemStacks[toolSlotNum].getItem() instanceof IModular)
        {
            NBTTagList nbtTagList = new NBTTagList();
            int invSlots = this.getSizeInventory();
            // Write all the modules into a TAG_List
            for (int slotNum = slotStart; slotNum < invSlots && slotNum < (slotStart + numModuleSlots); ++slotNum)
            {
                if (this.itemStacks[slotNum] != null)
                {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    nbtTagCompound.setByte("Slot", (byte)slotNum);
                    this.itemStacks[slotNum].writeToNBT(nbtTagCompound);
                    nbtTagList.appendTag(nbtTagCompound);
                }
            }

            // Write the module list to the tool
            NBTTagCompound nbt = this.itemStacks[toolSlotNum].getTagCompound();
            if (nbt == null) { nbt = new NBTTagCompound(); }

            nbt.setTag("Items", nbtTagList);
            this.itemStacks[toolSlotNum].setTagCompound(nbt);
        }
    }

    private void clearModuleSlots(int slotStart, int numModuleSlots)
    {
        // Clear all the module slots from the work station
        int invSlots = this.getSizeInventory();
        for (int slotNum = slotStart; slotNum < invSlots && slotNum < (slotStart + numModuleSlots); ++slotNum)
        {
            this.itemStacks[slotNum] = null;
        }
    }

    private void readModulesFromItem(int toolSlotNum, int slotStart, int numModuleSlots)
    {
        if (toolSlotNum >= this.itemStacks.length)
        {
            return;
        }

        if (this.itemStacks[toolSlotNum] != null && this.itemStacks[toolSlotNum].getItem() instanceof IModular)
        {
            NBTTagCompound nbt = this.itemStacks[toolSlotNum].getTagCompound();
            if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
            {
                return;
            }

            NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            int listNumStacks = nbtTagList.tagCount();
            // Read all the module ItemStacks from the tool, and write them to the workstation's module ItemStacks
            for (int i = 0; i < listNumStacks; ++i)
            {
                NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
                byte slotNum = nbtTagCompound.getByte("Slot");

                if (slotNum < this.itemStacks.length && slotNum >= slotStart && slotNum < (slotStart + numModuleSlots))
                {
                    this.itemStacks[slotNum] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                }
            }
        }
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack itemStack)
    {
        // Changing the item in the tool slot, write the current modules to the tool first
        if (slotNum == 0 && this.itemStacks[0] != null)
        {
            this.writeModulesToItem(0, 1, 10);
            this.clearModuleSlots(1, 10);
        }

        super.setInventorySlotContents(slotNum, itemStack);

        // Changing the item in the tool slot, read the modules from the new tool
        if (slotNum == 0 && this.itemStacks[0] != null)
        {
            // First clear the module slots, just in case
            this.clearModuleSlots(1, 10);
            this.readModulesFromItem(0, 1, 10);
        }
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        // Write the current modules to the tool every time the tool slot is accessed and has a tool
        if (slotNum == 0 && this.itemStacks[0] != null && this.worldObj.isRemote == false)
        {
            this.writeModulesToItem(0, 1, 10);
        }

        return super.getStackInSlot(slotNum);
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        if (this.itemStacks[slotNum] != null)
        {
            ItemStack itemstack;

            if (this.itemStacks[slotNum].stackSize <= maxAmount)
            {
                itemstack = this.getStackInSlot(slotNum);
                this.setInventorySlotContents(slotNum, null);

                return itemstack;
            }
            else
            {
                ItemStack newStack = this.getStackInSlot(slotNum).copy();
                itemstack = newStack.splitStack(maxAmount);
                this.setInventorySlotContents(slotNum, newStack);

                if (this.itemStacks[slotNum].stackSize == 0)
                {
                    this.setInventorySlotContents(slotNum, null);
                }

                return itemstack;
            }
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        if (this.itemStacks[slotNum] != null)
        {
            ItemStack itemstack = this.getStackInSlot(slotNum);
            this.setInventorySlotContents(slotNum, null);
            return itemstack;
        }

        return null;
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slotNum, ItemStack itemStack, int side)
    {
        return false;
    }

    @Override
    public boolean canExtractItem(int slotNum, ItemStack itemStack, int side)
    {
        return false;
    }

    @Override
    public ContainerToolWorkstation getContainer(InventoryPlayer inventory)
    {
        return new ContainerToolWorkstation(this, inventory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiToolWorkstation(getContainer(inventoryPlayer), this);
    }
}
