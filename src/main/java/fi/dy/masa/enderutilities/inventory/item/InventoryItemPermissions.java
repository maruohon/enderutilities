package fi.dy.masa.enderutilities.inventory.item;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class InventoryItemPermissions extends InventoryItem
{
    protected UUID accessorUUID;

    public InventoryItemPermissions(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, player, "Items");
    }

    public InventoryItemPermissions(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player, String tagName)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, player, tagName, null, null);
    }

    public InventoryItemPermissions(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player, String tagName, UUID containerUUID, IItemHandler hostInv)
    {
        super(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, tagName,containerUUID, hostInv);

        if (player != null)
        {
            this.accessorUUID = player.getUniqueID();
        }
    }

    @Override
    public boolean isCurrentlyAccessible()
    {
        //System.out.println("InventoryItemPermissions#isCurrentlyAccessible() - " + (this.isRemote ? "client" : "server"));
        return this.isAccessibleBy(this.accessorUUID);
    }

    @Override
    public boolean isAccessibleBy(Entity entity)
    {
        //System.out.println("InventoryItemPermissions#isAccessibleByPlayer() - " + (this.isRemote ? "client" : "server"));
        return OwnerData.canAccessItem(this.getContainerItemStack(), entity);
    }

    @Override
    public boolean isAccessibleBy(UUID uuid)
    {
        //System.out.println("InventoryItemPermissions#isAccessibleBy() - " + (this.isRemote ? "client" : "server"));
        return OwnerData.canAccessItem(this.getContainerItemStack(), uuid);
    }
}
