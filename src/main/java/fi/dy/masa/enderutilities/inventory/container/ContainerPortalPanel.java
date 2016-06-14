package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.item.base.IStringInput;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

public class ContainerPortalPanel extends ContainerTileEntityInventory implements IStringInput
{
    private final TileEntityPortalPanel tepp;
    private int targetLast;

    public ContainerPortalPanel(EntityPlayer player, TileEntityPortalPanel te)
    {
        super(player, te);

        this.tepp = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 168);
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

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);

        listener.sendProgressBarUpdate(this, 0, this.tepp.getActiveTargetId());
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            if (this.targetLast != this.tepp.getActiveTargetId())
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 0, this.tepp.getActiveTargetId());
            }
        }

        this.targetLast = this.tepp.getActiveTargetId();
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);

        if (id == 0)
        {
            this.tepp.setActiveTargetId(data);
        }
    }

    @Override
    public void handleString(EntityPlayer player, ItemStack stack, String text)
    {
        this.tepp.setTargetName(text);
    }
}
