package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class InventoryItemModule extends InventoryItem
{
    protected InventoryItemModular inventoryItemModular;

    public InventoryItemModule(InventoryItemModular inv, int invSize, World world, EntityPlayer player)
    {
        super(inv.getModularItemStack(), invSize, world, player);
        this.inventoryItemModular = inv;
    }

    @Override
    protected ItemStack getContainerItemStack()
    {
        return this.inventoryItemModular.getModularItemStack();
    }

    @Override
    public void writeToItem()
    {
        super.writeToItem();

        if (this.isRemote == false)
        {
            this.inventoryItemModular.readFromItem();
        }
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }
}
