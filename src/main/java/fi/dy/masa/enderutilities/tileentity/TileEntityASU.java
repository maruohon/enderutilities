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

public class TileEntityASU extends TileEntityEnderUtilitiesInventory implements ITieredStorage
{
    public static final int MAX_STACK_SIZE = 1024;
    private ItemStackHandlerLockable itemHandlerLockable;
    private int tier = 1;

    public TileEntityASU()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ASU);

        this.initStorage();
    }

    private int getInvSize()
    {
        return this.tier;
    }

    private void initStorage()
    {
        this.itemHandlerLockable    = new ItemStackHandlerLockable(0, this.getInvSize(), 0, true, "Items", this);
        this.itemHandlerBase        = this.itemHandlerLockable;
        this.itemHandlerExternal    = new ItemHandlerWrapperSelective(this.itemHandlerLockable);
    }

    public ItemStackHandlerLockable getInventoryASU()
    {
        return this.itemHandlerLockable;
    }

    @Override
    public void setStorageTier(int tier)
    {
        this.tier = MathHelper.clamp(tier, 1, 9);

        this.initStorage();
    }

    @Override
    public int getStorageTier()
    {
        return this.tier;
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

        this.markDirty();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.setStorageTier(nbt.getByte("Tier"));
        this.setStackLimit(nbt.getInteger("StackLimit"));

        super.readFromNBTCustom(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Tier", (byte) this.tier);
        nbt.setInteger("StackLimit", this.getBaseItemHandler().getInventoryStackLimit());

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte) this.tier);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.tier = tag.getByte("tier");

        this.initStorage();

        super.handleUpdateTag(tag);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 0)
        {
            this.setStackLimit(this.getBaseItemHandler().getInventoryStackLimit() + element);
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
