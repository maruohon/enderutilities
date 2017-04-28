package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;

public class ContainerBarrel extends ContainerLargeStacksTile
{
    private final ItemStackHandlerLockable lockableInv;
    private final IItemHandler upgradeInv;

    public ContainerBarrel(EntityPlayer player, TileEntityBarrel te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);

        this.lockableInv = te.getInventoryBarrel();
        this.upgradeInv = te.getUpgradeInventory();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 93);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 1);

        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 80, 23));

        // Upgrade slots
        for (int slot = 0; slot < 3; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.upgradeInv, slot, 62 + slot * 18, 59));
        }
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        // Middle click or middle click drag
        if (((clickType == ClickType.CLONE && dragType == 2) ||
            (clickType == ClickType.QUICK_CRAFT && dragType == 9)) &&
            slotNum >= 0 && slotNum < this.lockableInv.getSlots())
        {
            this.toggleSlotLocked(slotNum, this.lockableInv, false);
            return null;
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        // Shift + Middle click: Cycle the stack size in creative mode
        if (EnumKey.MIDDLE_CLICK.matches(action, HotKeys.MOD_SHIFT))
        {
            if (player.capabilities.isCreativeMode)
            {
                this.cycleStackSize(element, this.lockableInv);
            }
        }
    }
}
