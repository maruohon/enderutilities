package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiASU;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.container.ContainerASU;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityASU extends TileEntityEnderUtilitiesInventory
{
    public static final int MAX_INV_SIZE = 27;
    public static final int MAX_STACK_SIZE = 1024;
    private ItemStackHandlerLockable itemHandlerLockable;
    private int inventorySize = 1;

    public TileEntityASU()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ASU);

        this.initStorage();
    }

    private void initStorage()
    {
        this.itemHandlerLockable    = new ItemStackHandlerASU(0, MAX_INV_SIZE, 1, true, "Items", this);
        this.itemHandlerBase        = this.itemHandlerLockable;
        this.itemHandlerExternal    = new ItemHandlerWrapperSelective(this.itemHandlerLockable);
    }

    public ItemStackHandlerLockable getInventoryASU()
    {
        return this.itemHandlerLockable;
    }

    public int getInvSize()
    {
        return this.inventorySize;
    }

    public void setInvSize(int size)
    {
        this.inventorySize = MathHelper.clamp(size, 1, MAX_INV_SIZE);
        this.markDirty();
    }

    public void setStackLimit(int limit)
    {
        this.getBaseItemHandler().setStackLimit(MathHelper.clamp(limit, 0, MAX_STACK_SIZE));
    }

    @Override
    public void setPlacementProperties(World world, BlockPos pos, ItemStack stack, NBTTagCompound tag)
    {
        if (tag.hasKey("asu.stack_limit", Constants.NBT.TAG_INT))
        {
            this.setStackLimit(tag.getInteger("asu.stack_limit"));
        }

        if (tag.hasKey("asu.slots", Constants.NBT.TAG_BYTE))
        {
            this.setInvSize(tag.getByte("asu.slots"));
        }

        this.markDirty();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.setInvSize(nbt.getByte("Tier"));
        this.setStackLimit(nbt.getInteger("StackLimit"));

        super.readFromNBTCustom(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Tier", (byte) this.inventorySize);
        nbt.setInteger("StackLimit", this.getBaseItemHandler().getInventoryStackLimit());

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte) this.inventorySize);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.setInvSize(tag.getByte("tier"));

        super.handleUpdateTag(tag);
    }

    private void changeInventorySize(int changeAmount)
    {
        int newSize = MathHelper.clamp(this.getInvSize() + changeAmount, 1, MAX_INV_SIZE);

        // Shrinking the inventory, only allowed if there are no items in the slots-to-be-removed
        if (changeAmount < 0)
        {
            int changeFinal = 0;

            for (int slot = this.getInvSize() - 1; slot >= newSize && slot >= 1; slot--)
            {
                if (this.itemHandlerLockable.getStackInSlot(slot) == null)
                {
                    changeFinal--;
                }
                else
                {
                    break;
                }
            }

            newSize = MathHelper.clamp(this.getInvSize() + changeFinal, 1, MAX_INV_SIZE);
        }

        if (newSize >= 1 && newSize <= MAX_INV_SIZE)
        {
            this.setInvSize(newSize);

            this.notifyBlockUpdate(this.getPos());
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 0)
        {
            this.setStackLimit(this.getBaseItemHandler().getInventoryStackLimit() + element);
        }
        else if (action == 1)
        {
            this.changeInventorySize(element);
        }
    }

    private class ItemStackHandlerASU extends ItemStackHandlerLockable
    {
        public ItemStackHandlerASU(int inventoryId, int invSize, int stackLimit, boolean allowCustomStackSizes,
                String tagName, TileEntityEnderUtilitiesInventory te)
        {
            super(inventoryId, invSize, stackLimit, allowCustomStackSizes, tagName, te);
        }

        @Override
        public int getSlots()
        {
            return TileEntityASU.this.getInvSize();
        }
    }

    @Override
    public ContainerASU getContainer(EntityPlayer player)
    {
        return new ContainerASU(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiASU(this.getContainer(player), this);
    }
}
