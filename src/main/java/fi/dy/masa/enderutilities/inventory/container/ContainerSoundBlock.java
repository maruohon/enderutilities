package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntitySoundBlock;

public class ContainerSoundBlock extends ContainerEnderUtilities
{
    public ContainerSoundBlock(EntityPlayer player, TileEntitySoundBlock te)
    {
        super(player, null);

        this.addPlayerInventorySlots(8, 174);
    }
}
