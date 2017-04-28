package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.tileentity.TileEntityMSU;

public class ContainerMSU extends ContainerLargeStacksTile
{
    protected final TileEntityMSU temsu;

    public ContainerMSU(EntityPlayer player, TileEntityMSU te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);
        this.temsu = te;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 57);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int tier = MathHelper.clamp(this.temsu.getStorageTier(), 0, 1);

        int posX = tier == 1 ? 8 : 80;
        int posY = 23;
        int slots = tier == 1 ? 9 : 1;

        for (int slot = 0; slot < slots; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, posX + slot * 18, posY));
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, slots);
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        ItemStackHandlerLockable inv = this.temsu.getInventoryMSU();

        // Middle click or middle click drag
        if (((clickType == ClickType.CLONE && dragType == 2) ||
            (clickType == ClickType.QUICK_CRAFT && dragType == 9)) &&
            slotNum >= 0 && slotNum < inv.getSlots())
        {
            this.toggleSlotLocked(slotNum, inv, false);
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
                this.cycleStackSize(element, this.temsu.getInventoryMSU());
            }
        }
        // Alt + Middle click: Swap two stacks
        else if (EnumKey.MIDDLE_CLICK.matches(action, HotKeys.MOD_ALT))
        {
            this.swapSlots(element, player);
        }
    }
}
