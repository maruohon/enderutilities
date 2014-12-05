package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilitiesInventory;

public class TileEntityEnderUtilitiesInventory extends TileEntityEnderUtilities implements IInventory
{
    protected String customInventoryName;
    protected ItemStack[] itemStacks;


    public TileEntityEnderUtilitiesInventory(String name)
    {
        super(name);
    }

    public void setInventoryName(String name)
    {
        this.customInventoryName = name;
    }

    /* Returns if the inventory is named */
    @Override
    public boolean hasCustomInventoryName()
    {
        return this.customInventoryName != null && this.customInventoryName.length() > 0;
    }

    /* Returns the name of the inventory */
    @Override
    public String getInventoryName()
    {
        return this.hasCustomInventoryName() ? this.customInventoryName : "container." + this.tileEntityName;
    }

    @Override
    public int getSizeInventory()
    {
        if (this.itemStacks != null)
        {
            return this.itemStacks.length;
        }

        return 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        if (nbt.hasKey("CustomName", Constants.NBT.TAG_STRING) == true)
        {
            this.customInventoryName = nbt.getString("CustomName");
        }

        NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        //this.itemStacks = new ItemStack[this.getSizeInventory()]; // Done in the sub class constructor for each TE
        int numSlots = nbtTagList.tagCount();

        for (int i = 0; i < numSlots; ++i)
        {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
            byte slotNum = nbtTagCompound.getByte("Slot");

            if (slotNum >= 0 && slotNum < this.itemStacks.length)
            {
                this.itemStacks[slotNum] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (this.hasCustomInventoryName())
        {
            nbt.setString("CustomName", this.customInventoryName);
        }

        NBTTagList nbtTagList = new NBTTagList();
        int numSlots = (this.itemStacks != null ? this.itemStacks.length : 0);

        for (int i = 0; i < numSlots; ++i)
        {
            if (this.itemStacks[i] != null)
            {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setByte("Slot", (byte)i);
                this.itemStacks[i].writeToNBT(nbtTagCompound);
                nbtTagList.appendTag(nbtTagCompound);
            }
        }

        nbt.setTag("Items", nbtTagList);
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        return itemStacks[slotNum];
    }

    /* Removes from an inventory slot (slotNum) up to a specified number (maxAmount) of items and returns them in a new stack. */
    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        if (this.itemStacks[slotNum] != null)
        {
            ItemStack itemstack;

            if (this.itemStacks[slotNum].stackSize <= maxAmount)
            {
                itemstack = this.itemStacks[slotNum];
                this.itemStacks[slotNum] = null;

                return itemstack;
            }
            else
            {
                itemstack = this.itemStacks[slotNum].splitStack(maxAmount);

                if (this.itemStacks[slotNum].stackSize == 0)
                {
                    this.itemStacks[slotNum] = null;
                }

                return itemstack;
            }
        }

        return null;
    }

    /* When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     */
    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        if (this.itemStacks[slotNum] != null)
        {
            ItemStack itemstack = this.itemStacks[slotNum];
            this.itemStacks[slotNum] = null;
            return itemstack;
        }

        return null;
    }

    /* Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections). */
    @Override
    public void setInventorySlotContents(int slotNum, ItemStack itemStack)
    {
        this.itemStacks[slotNum] = itemStack;

        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit())
        {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
    }

    /* Returns the maximum stack size for a inventory slot. */
    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    /* Do not make give this method the name canInteractWith because it clashes with Container */
    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this)
        {
            return false;
        }

        if (player.getDistanceSq((double)this.xCoord + 0.5d, (double)this.yCoord + 0.5d, (double)this.zCoord + 0.5d) >= 64.0d)
        {
            return false;
        }

        return true;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack itemStack)
    {
        return true;
    }

    public ContainerEnderUtilitiesInventory getContainer(InventoryPlayer inventory)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return null;
    }

    public void performGuiAction(int element, short action)
    {
    }
}
