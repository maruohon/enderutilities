package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiTemplatedChest;
import fi.dy.masa.enderutilities.inventory.ContainerTemplatedChest;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityTemplatedChest extends TileEntityEnderUtilitiesInventory implements ITieredStorage
{
    public static final int[] INV_SIZES = new int[] { 9, 27, 54 };

    protected ItemStack[] templateStacks;
    protected int chestTier;
    protected long templateMask;

    public TileEntityTemplatedChest()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TEMPLATED_CHEST, 9);
        this.templateStacks = new ItemStack[54];
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.chestTier = MathHelper.clamp_int(nbt.getByte("ChestTier"), 0, 2);
        this.templateMask = nbt.getLong("TemplateMask");
        this.invSize = INV_SIZES[this.chestTier];

        super.readFromNBTCustom(nbt);

        this.templateStacks = this.readItemsFromNBT(nbt, this.invSize, "TemplateItems");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("ChestTier", (byte)this.chestTier);
        nbt.setLong("TemplateMask", this.templateMask);

        this.writeItemsToNBT(nbt, this.templateStacks, "TemplateItems");

        super.writeToNBT(nbt);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("tier", (byte)this.chestTier);
        //nbt.setLong("tmpl", this.templateMask); // TODO remove?

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();

        this.chestTier = nbt.getByte("tier");
        //this.templateMask = nbt.getLong("tmpl");
        this.invSize = INV_SIZES[this.chestTier];
        this.itemStacks = new ItemStack[this.invSize];
        this.templateStacks = new ItemStack[this.invSize];

        super.onDataPacket(net, packet);
    }

    @Override
    public int getSizeInventory()
    {
        return this.invSize;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if ((this.templateMask & (1L << slotNum)) == 0)
        {
            return true;
        }

        return InventoryUtils.areItemStacksEqual(stack, this.templateStacks[slotNum]) == true;
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
        this.itemStacks = new ItemStack[this.invSize];
        this.templateStacks = new ItemStack[this.invSize];
    }

    public long getTemplateMask()
    {
        return this.templateMask;
    }

    public void toggleTemplateMask(int slotNum)
    {
        this.templateMask ^= (1L << slotNum);
    }

    public void setTemplateMask(long mask)
    {
        this.templateMask = mask;
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

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public ContainerTemplatedChest getContainer(InventoryPlayer inventoryPlayer)
    {
        return new ContainerTemplatedChest(inventoryPlayer, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiTemplatedChest(this.getContainer(inventoryPlayer), this);
    }
}
