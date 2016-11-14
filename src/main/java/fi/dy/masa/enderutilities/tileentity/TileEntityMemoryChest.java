package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiMemoryChest;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerMemoryChest;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class TileEntityMemoryChest extends TileEntityEnderUtilitiesInventory implements ITieredStorage
{
    public static final int GUI_ACTION_TOGGLE_LOCKED = 1;
    public static final int[] INV_SIZES = new int[] { 9, 27, 54 };

    protected ItemStack[] templateStacks;
    protected List<Integer> enabledTemplateSlots;
    protected int chestTier;
    protected long templateMask;
    protected int invSize;

    public TileEntityMemoryChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST);
        this.enabledTemplateSlots = new ArrayList<Integer>();
        this.initStorage(54);
    }

    private void initStorage(int invSize)
    {
        this.templateStacks = new ItemStack[invSize];
        this.itemHandlerBase = new ItemStackHandlerTileEntity(invSize, this);
        this.itemHandlerExternal = new ItemHandlerWrapperMemoryChestExternal(this.getBaseItemHandler(), this);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperContainer(this.getBaseItemHandler(),
                new ItemHandlerWrapperMemoryChestContainer(this.getBaseItemHandler(), this));
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
        // This creates the inventories themselves...
        this.initStorage(this.invSize);
        NBTUtils.readStoredItemsFromTag(nbt, this.templateStacks, "TemplateItems");

        // ... and this de-serializes the items from NBT into the inventory
        super.readItemsFromNBT(nbt);
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        super.writeItemsToNBT(nbt);

        NBTUtils.writeItemsToTag(nbt, this.templateStacks, "TemplateItems", true);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("ChestTier", (byte)this.chestTier);
        nbt.setLong("TemplateMask", this.templateMask);

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte)this.chestTier);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.chestTier = tag.getByte("tier");
        this.invSize = INV_SIZES[this.chestTier];
        this.initStorage(this.invSize);

        super.handleUpdateTag(tag);
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

        this.initStorage(this.invSize);
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

    private class ItemHandlerWrapperMemoryChestExternal extends ItemHandlerWrapperSelective
    {
        private final TileEntityMemoryChest temc;

        public ItemHandlerWrapperMemoryChestExternal(IItemHandler baseHandler, TileEntityMemoryChest te)
        {
            super(baseHandler);
            this.temc = te;
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if (this.temc.isPublic() == false)
            {
                return null;
            }

            return super.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (this.temc.isPublic() == false)
            {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (this.temc.isPublic() == false)
            {
                return null;
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return false;
            }

            // Simple cases for allowing items in: no templated slots, or matching item already in the slot
            if (this.temc.templateMask == 0 || InventoryUtils.areItemStacksEqual(stack, this.getStackInSlot(slot)) == true)
            {
                return true;
            }

            // If trying to add into an empty slot, first make sure that there aren't templated slots
            // for this item type, that still have free space
            //int max = Math.min(((ItemStackHandlerBasic)this.getBaseHandler()).getInventoryStackLimit(), stack.getMaxStackSize());
            int max = stack.getMaxStackSize();

            for (int i : this.temc.enabledTemplateSlots)
            {
                if (slot == i)
                {
                    //System.out.println("isValid slot match - " + (this.worldObj.isRemote ? "client" : "server"));
                    return InventoryUtils.areItemStacksEqual(stack, this.temc.templateStacks[slot]) == true;
                }

                ItemStack stackTmp = this.getStackInSlot(i);
                // Space in the inventory slot for this template slot, and the input item matches the template item
                // => disallow putting the input item in slotNum, unless slotNum was this slot (see above check)
                if ((stackTmp == null || stackTmp.stackSize < max) && InventoryUtils.areItemStacksEqual(stack, this.temc.templateStacks[i]) == true)
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
    }

    private class ItemHandlerWrapperMemoryChestContainer extends ItemHandlerWrapperSelective
    {
        private final TileEntityMemoryChest temc;

        public ItemHandlerWrapperMemoryChestContainer(IItemHandler baseHandler, TileEntityMemoryChest te)
        {
            super(baseHandler);
            this.temc = te;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return false;
            }

            // Simple cases for allowing items in: no templated slots, or matching item already in the slot
            if (this.temc.templateMask == 0 || InventoryUtils.areItemStacksEqual(stack, this.getStackInSlot(slot)) == true)
            {
                return true;
            }

            // No template locked for this slot, or the item matches with the template item
            return (this.temc.templateMask & (1L << slot)) == 0 || InventoryUtils.areItemStacksEqual(stack, this.temc.templateStacks[slot]);
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_TOGGLE_LOCKED)
        {
            if (this.ownerData == null)
            {
                this.ownerData = new OwnerData(player);
                this.ownerData.setIsPublic(false);
            }
            else if (player.capabilities.isCreativeMode || this.ownerData.isOwner(player))
            {
                this.setIsPublic(! this.isPublic());
            }
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
