package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.items.CapabilityItemHandler;

import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ContainerTileEntityInventory extends ContainerEnderUtilities
{
    protected TileEntityEnderUtilitiesInventory te;

    public ContainerTileEntityInventory(EntityPlayer player, TileEntityEnderUtilitiesInventory te)
    {
        super(player, te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP));
        this.te = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return super.canInteractWith(player) && this.te.isInvalid() == false;
    }
}
