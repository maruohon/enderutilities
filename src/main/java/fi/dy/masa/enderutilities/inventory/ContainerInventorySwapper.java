package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class ContainerInventorySwapper extends ContainerEnderUtilities implements IContainerModularItem
{
    public InventoryItem inventoryItem;
    public EntityPlayer player;

    public ContainerInventorySwapper(EntityPlayer player, InventoryItem inventory)
    {
        super(player.inventory, inventory);
        this.player = player;
        this.inventoryItem = inventory;
        this.inventoryItem.readFromContainerItemStack();
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 84);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int xOff = 8;
        int yOff = 102;

        int moduleSlots = this.inventoryItem.getSizeInventory();
        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModule(this.inventoryItem, i, xOff + i * 18, yOff, ModuleType.TYPE_MEMORY_CARD, this));
        }
    }

    @Override
    public ItemStack getModularItem()
    {
        return this.inventoryItem.getContainerItemStack();
    }
}
