package fi.dy.masa.enderutilities.inventory.container.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class ContainerLargeStacksTile extends ContainerLargeStacks
{
    protected final TileEntityEnderUtilities te;

    public ContainerLargeStacksTile(EntityPlayer player, IItemHandler inventory, TileEntityEnderUtilities te)
    {
        super(player, inventory);

        this.te = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return super.canInteractWith(player) && this.te.isInvalid() == false;
    }
}
