package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ContainerPortalPanel extends ContainerTileEntityInventory
{
    public ContainerPortalPanel(EntityPlayer player, TileEntityEnderUtilitiesInventory te)
    {
        super(player, te);

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 121);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 53;
        int posY = 19;

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 16);

        // Add Link Crystal slots
        for (int r = 0; r < 2; r++)
        {
            for (int c = 0; c < 4; c++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, r * 4 + c, posX + c * 18, posY + r * 50));
            }
        }

        posY = 37;
        // Add dye slots
        for (int r = 0; r < 2; r++)
        {
            for (int c = 0; c < 4; c++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, r * 4 + c + 8, posX + c * 18, posY + r * 50));
            }
        }
    }
}
