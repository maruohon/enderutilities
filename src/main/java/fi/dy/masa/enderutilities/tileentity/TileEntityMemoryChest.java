package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.gui.client.GuiMemoryChest;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.container.ContainerMemoryChest;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class TileEntityMemoryChest extends TileEntityEnderUtilitiesInventory
{
    public static final int GUI_ACTION_TOGGLE_LOCKED = 1;
    private static final int[] INV_SIZES = new int[] { 9, 27, 54 };

    private ItemStackHandlerLockable itemHandlerLockable;
    private int chestTier;
    private int invSize;

    public TileEntityMemoryChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST);

        this.initStorage(0);
    }

    private void initStorage(int invSize)
    {
        this.itemHandlerLockable    = new ItemStackHandlerLockable(0, invSize, 64, false, "Items", this);
        this.itemHandlerBase        = this.itemHandlerLockable;
        this.itemHandlerExternal    = new ItemHandlerWrapperMemoryChestExternal(this.itemHandlerLockable, this);
    }

    public ItemStackHandlerLockable getInventory()
    {
        return this.itemHandlerLockable;
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperContainerMemoryChest(this.itemHandlerLockable, this.itemHandlerExternal);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.setStorageTier(nbt.getByte("ChestTier"));

        super.readFromNBTCustom(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("ChestTier", (byte) this.chestTier);

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte) this.chestTier);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.setStorageTier(tag.getByte("tier"));

        super.handleUpdateTag(tag);
    }

    public int getStorageTier()
    {
        return this.chestTier;
    }

    public void setStorageTier(int tier)
    {
        this.chestTier = MathHelper.clamp(tier, 0, 2);
        this.invSize = INV_SIZES[this.chestTier];

        this.initStorage(this.invSize);
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
            return this.temc.isPublic() ? super.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            return this.temc.isPublic() ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return this.temc.isPublic() ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
    }

    private class ItemHandlerWrapperContainerMemoryChest extends ItemHandlerWrapperContainer
    {
        public ItemHandlerWrapperContainerMemoryChest(IItemHandlerModifiable baseHandler, IItemHandler wrapperHandler)
        {
            super(baseHandler, wrapperHandler);
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return this.baseHandlerModifiable.getStackInSlot(slot);
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
