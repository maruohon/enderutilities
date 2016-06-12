package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;

import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ContainerTileEntityInventory extends ContainerCustomSlotClick
{
    public final TileEntityEnderUtilitiesInventory te;

    public ContainerTileEntityInventory(EntityPlayer player, TileEntityEnderUtilitiesInventory te)
    {
        super(player, te.getWrappedInventoryForContainer(player));
        this.te = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return super.canInteractWith(player) && this.te.isInvalid() == false;
    }
}
