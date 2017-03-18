package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;

public class ContainerASU extends ContainerLargeStacks
{
    protected TileEntityASU teasu;

    public ContainerASU(EntityPlayer player, TileEntityASU te)
    {
        super(player, te.getWrappedInventoryForContainer(player));
        this.teasu = te;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 57);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 8;
        int posY = 23;
        int slots = this.teasu.getStorageTier();

        for (int slot = 0; slot < slots; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, posX + slot * 18, posY));
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, slots);

        // Add the "reference inventory" slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.teasu.getReferenceInventory(), 0, 175, posY));
    }
}
