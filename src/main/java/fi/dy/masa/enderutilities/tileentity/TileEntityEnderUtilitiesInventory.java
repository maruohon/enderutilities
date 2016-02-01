package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;

public class TileEntityEnderUtilitiesInventory extends TileEntityEnderUtilities implements IInventory, ILockableContainer
{
    protected LockCode lockCode = LockCode.EMPTY_CODE;
    protected String customInventoryName;
    protected ItemStack[] itemStacks;
    protected int invSize;
    protected int invStackLimit;

    public TileEntityEnderUtilitiesInventory(String name, int invSize)
    {
        super(name);
        this.invSize = invSize;
        this.invStackLimit = 64;
        this.itemStacks = new ItemStack[this.invSize];
    }

    public void setInventoryName(String name)
    {
        this.customInventoryName = name;
    }

    @Override
    public boolean hasCustomName()
    {
        return this.customInventoryName != null && this.customInventoryName.length() > 0;
    }

    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.customInventoryName : Reference.MOD_ID + ".container." + this.tileEntityName;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return new ChatComponentTranslation(this.getName());
    }

    /**
     * Reads stored items from NBT, from a TagList by the name <b>tagName</b>.
     * The array of ItemStacks is initialized to the size <b>invSize</b>.
     * @param invSize
     * @param tagName
     * @return an array of ItemStacks read from NBT from a key tagName
     */
    public ItemStack[] readItemsFromNBT(NBTTagCompound nbt, int invSize, String tagName)
    {
        ItemStack[] stacks = new ItemStack[invSize];

        NBTTagList nbtTagList = nbt.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
        int numSlots = nbtTagList.tagCount();

        for (int i = 0; i < numSlots; i++)
        {
            NBTTagCompound tagItem = nbtTagList.getCompoundTagAt(i);
            byte slotNum = tagItem.getByte("Slot");

            if (slotNum >= 0 && slotNum < stacks.length)
            {
                stacks[slotNum] = ItemStack.loadItemStackFromNBT(tagItem);

                if (stacks[slotNum] != null && tagItem.hasKey("ActualCount", Constants.NBT.TAG_INT))
                {
                    stacks[slotNum].stackSize = tagItem.getInteger("ActualCount");
                }
            }
            else
            {
                BlockPos pos = this.getPos();
                String str = String.format("Invalid slot number while reading inventory from NBT; got: %d, max: %d (TE location: x: %d y: %d, z: %d)",
                        slotNum, (this.itemStacks.length - 1), pos.getX(), pos.getY(), pos.getZ());
                EnderUtilities.logger.warn(this.getClass().getSimpleName() + ": " + str);
            }
        }

        return stacks;
    }

    /**
     * Writes the items from the given array of ItemStacks into NBT to a TagList by the name <b>tagName</b>.
     * @param nbt
     * @param tagName
     */
    public void writeItemsToNBT(NBTTagCompound nbt, ItemStack[] stacks, String tagName)
    {
        if (stacks == null)
        {
            return;
        }

        NBTTagList nbtTagList = new NBTTagList();
        int numSlots = (stacks != null ? stacks.length : 0);

        for (int i = 0; i < numSlots; ++i)
        {
            if (stacks[i] != null)
            {
                NBTTagCompound tagItem = new NBTTagCompound();
                stacks[i].writeToNBT(tagItem);
                tagItem.setByte("Slot", (byte)i);
                tagItem.setInteger("ActualCount", stacks[i].stackSize);
                nbtTagList.appendTag(tagItem);
            }
        }

        nbt.setTag(tagName, nbtTagList);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        if (nbt.hasKey("CustomName", Constants.NBT.TAG_STRING) == true)
        {
            this.customInventoryName = nbt.getString("CustomName");
        }

        this.itemStacks = this.readItemsFromNBT(nbt, this.invSize, "Items");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (this.hasCustomName() == true)
        {
            nbt.setString("CustomName", this.customInventoryName);
        }

        this.writeItemsToNBT(nbt, this.itemStacks, "Items");
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
    public ItemStack getStackInSlot(int slotNum)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return null;
        }

        return this.itemStacks[slotNum];
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return null;
        }

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

            this.markDirty();
            return stack;
        }

        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int slotNum)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return null;
        }

        ItemStack stack = this.itemStacks[slotNum];
        this.itemStacks[slotNum] = null;
        this.markDirty();

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack stack)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return;
        }

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.itemStacks[slotNum] = stack;

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.invStackLimit;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack itemStack)
    {
        return true;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < this.itemStacks.length; ++i)
        {
            this.itemStacks[i] = null;
        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        if (this.worldObj.getTileEntity(this.getPos()) != this)
        {
            return false;
        }

        if (player.getDistanceSq(this.getPos()) >= 64.0d)
        {
            return false;
        }

        return true;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value) { }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return null;
    }

    public void performGuiAction(EntityPlayer player, int action, int element)
    {
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return null;
    }

    @Override
    public String getGuiID()
    {
        return Reference.MOD_ID + ":" + this.tileEntityName;
    }

    @Override
    public boolean isLocked()
    {
        return this.lockCode != null && this.lockCode.isEmpty() == false;
    }

    @Override
    public void setLockCode(LockCode code)
    {
    }

    @Override
    public LockCode getLockCode()
    {
        return this.lockCode;
    }
}
