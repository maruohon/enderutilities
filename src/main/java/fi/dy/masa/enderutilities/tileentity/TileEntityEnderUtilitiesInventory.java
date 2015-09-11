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
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.inventory.ContainerTileEntityInventory;
import fi.dy.masa.enderutilities.reference.Reference;

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

    @Override
    public boolean hasCustomInventoryName()
    {
        return this.customInventoryName != null && this.customInventoryName.length() > 0;
    }

    @Override
    public String getInventoryName()
    {
        return this.hasCustomInventoryName() ? this.customInventoryName : Reference.MOD_ID + ".container." + this.tileEntityName;
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
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        if (nbt.hasKey("CustomName", Constants.NBT.TAG_STRING) == true)
        {
            this.customInventoryName = nbt.getString("CustomName");
        }

        NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        int numSlots = nbtTagList.tagCount();
        this.itemStacks = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < numSlots; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            byte slotNum = tag.getByte("Slot");

            if (slotNum >= 0 && slotNum < this.itemStacks.length)
            {
                this.itemStacks[slotNum] = ItemStack.loadItemStackFromNBT(tag);
            }
            else
            {
                EnderUtilities.logger.warn("Invalid slot number when reading inventory from NBT: " + slotNum + " (max: " + (this.itemStacks.length - 1) + ")");
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
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                this.itemStacks[i].writeToNBT(tag);
                nbtTagList.appendTag(tag);
            }
        }

        nbt.setTag("Items", nbtTagList);
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        return itemStacks[slotNum];
    }

    /**
     * Removes from an inventory slot (slotNum) up to a specified number (maxAmount) of items and returns them in a new stack.
     */
    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        if (this.itemStacks[slotNum] != null)
        {
            ItemStack stack;

            if (this.itemStacks[slotNum].stackSize >= maxAmount)
            {
                stack = this.itemStacks[slotNum].splitStack(maxAmount);

                if (this.itemStacks[slotNum].stackSize <= 0)
                {
                    this.itemStacks[slotNum] = null;
                }
            }
            else
            {
                stack = this.itemStacks[slotNum];
                this.itemStacks[slotNum] = null;
            }

            return stack;
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        ItemStack stack = this.itemStacks[slotNum];
        this.setInventorySlotContents(slotNum, null);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack stack)
    {
        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.itemStacks[slotNum] = stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

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

    public ContainerTileEntityInventory getContainer(InventoryPlayer inventory)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return null;
    }

    public void performGuiAction(int action, int element)
    {
    }
}
