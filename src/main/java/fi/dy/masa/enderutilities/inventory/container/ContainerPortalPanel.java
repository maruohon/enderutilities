package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerTile;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.item.base.IStringInput;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

public class ContainerPortalPanel extends ContainerTile implements IStringInput
{
    private final TileEntityPortalPanel tepp;
    private int targetLast;
    private boolean portalOnlyLast;

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

        listener.sendWindowProperty(this, 0, this.tepp.getActiveTargetId());
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        boolean portalOnly = Configs.portalOnlyAllowsPortalTypeLinkCrystals;

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            if (this.targetLast != this.tepp.getActiveTargetId())
            {
                this.listeners.get(i).sendWindowProperty(this, 0, this.tepp.getActiveTargetId());
            }

            if (this.portalOnlyLast != portalOnly)
            {
                this.listeners.get(i).sendWindowProperty(this, 1, portalOnly ? 1 : 0);
            }
        }

        this.targetLast = this.tepp.getActiveTargetId();
        this.portalOnlyLast = portalOnly;
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);

        switch (id)
        {
            case 0:
                this.tepp.setActiveTargetId(data);
                break;
            case 1:
                this.portalOnlyLast = data == 1;
                break;
        }
    }

    public boolean portalOnly()
    {
        return this.portalOnlyLast;
    }

    @Override
    public void handleString(EntityPlayer player, ItemStack stack, String text)
    {
        this.tepp.setTargetName(text);
    }
}
