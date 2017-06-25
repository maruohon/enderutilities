package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiMSU;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.container.ContainerMSU;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperCreative;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart.ItemPartType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityMSU extends TileEntityEnderUtilitiesInventory
{
    public static final int GUI_ACTION_TOGGLE_CREATIVE = 1;
    private ItemStackHandlerLockable itemHandlerLockable;
    private int tier;

    public TileEntityMSU()
    {
        super(ReferenceNames.NAME_TILE_MSU, true);
        this.initStorage();
    }

    private int getInvSize()
    {
        return this.tier == 1 ? 9 : 1;
    }

    private void initStorage()
    {
        this.itemHandlerLockable    = new ItemStackHandlerLockable(0, this.getInvSize(), Configs.msuMaxItems, true, "Items", this);
        this.itemHandlerBase        = this.itemHandlerLockable;
        this.itemHandlerExternal    = new ItemHandlerWrapperCreative(this.itemHandlerLockable, this);
    }

    public ItemStackHandlerLockable getInventoryMSU()
    {
        return this.itemHandlerLockable;
    }

    public void setStorageTier(int tier)
    {
        this.tier = MathHelper.clamp(tier, 0, 1);

        this.initStorage();
    }

    public int getStorageTier()
    {
        return this.tier;
    }

    @Override
    public boolean onRightClickBlock(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.isEmpty() == false && ItemEnderPart.itemMatches(stack, ItemPartType.CREATIVE_STORAGE_KEY))
        {
            this.setCreative(! this.isCreative());
            this.markDirty();
            this.notifyBlockUpdate(this.getPos());
            this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 0.7f, 1f);

            return true;
        }

        return super.onRightClickBlock(player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.tier = MathHelper.clamp(nbt.getByte("Tier"), 0, 1);
        this.setCreative(nbt.getBoolean("Creative"));

        super.readFromNBTCustom(nbt);
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        // This creates the inventories themselves...
        this.initStorage();

        // ... and this de-serializes the items from NBT into the inventory
        super.readItemsFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Tier", (byte) this.tier);
        nbt.setBoolean("Creative", this.isCreative());

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte) this.tier);
        nbt.setBoolean("cr", this.isCreative());

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.tier = tag.getByte("tier");
        this.setCreative(tag.getBoolean("cr"));

        this.initStorage();

        super.handleUpdateTag(tag);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_TOGGLE_CREATIVE)
        {
            if (player.capabilities.isCreativeMode)
            {
                this.setCreative(! this.isCreative());
                this.markDirty();
                this.notifyBlockUpdate(this.getPos());
            }
        }
    }

    @Override
    public ContainerMSU getContainer(EntityPlayer player)
    {
        return new ContainerMSU(player, this);
    }

    @Override
    public Object getGui(EntityPlayer player)
    {
        return new GuiMSU(this.getContainer(player), this);
    }
}
