package fi.dy.masa.enderutilities.inventory.container.base;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ContainerTile extends ContainerCustomSlotClick
{
    protected final TileEntityEnderUtilitiesInventory te;

    public ContainerTile(EntityPlayer player, TileEntityEnderUtilitiesInventory te)
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
