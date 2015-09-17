package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ContainerTileEntityInventory extends ContainerEnderUtilities
{
    protected TileEntityEnderUtilitiesInventory te;

    public ContainerTileEntityInventory(InventoryPlayer inventoryPlayer, TileEntityEnderUtilitiesInventory te)
    {
        super(inventoryPlayer, te);
        this.te = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return super.canInteractWith(player) && this.te.isInvalid() == false;
    }
}
