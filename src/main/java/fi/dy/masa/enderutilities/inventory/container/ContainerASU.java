package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.ICustomSlotSync;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncCustomSlot;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class ContainerASU extends ContainerLargeStacksTile implements ICustomSlotSync
{
    protected final TileEntityASU teasu;
    private final boolean[] lockedLast;
    private final ItemStack[] templateStacksLast;
    private int stackLimitLast;

    public ContainerASU(EntityPlayer player, TileEntityASU te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);
        this.teasu = te;

        int numSlots = te.getInventoryASU().getSlots();
        this.lockedLast = new boolean[numSlots];
        this.templateStacksLast = new ItemStack[numSlots];

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 57);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 8;
        int posY = 27;
        int slots = this.teasu.getStorageTier();

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), slots);

        for (int slot = 0; slot < slots; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, posX + slot * 18, posY));
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.isClient == false)
        {
            int stackLimit = this.teasu.getBaseItemHandler().getInventoryStackLimit();
 
            for (int i = 0; i < this.listeners.size(); i++)
            {
                if (stackLimit != this.stackLimitLast)
                {
                    this.listeners.get(i).sendProgressBarUpdate(this, 0, stackLimit & 0xFFFF);
                    this.listeners.get(i).sendProgressBarUpdate(this, 1, stackLimit >>> 16);
                }
            }

            ItemStackHandlerLockable inv = this.teasu.getInventoryASU();
            int numSlots = inv.getSlots();

            for (int slot = 0; slot < numSlots; slot++)
            {
                boolean locked = inv.isSlotLocked(slot);

                if (this.lockedLast[slot] != locked)
                {
                    for (int i = 0; i < this.listeners.size(); i++)
                    {
                        this.listeners.get(i).sendProgressBarUpdate(this, 2, slot | (locked ? 0x8000 : 0));
                    }

                    this.lockedLast[slot] = locked;
                }

                ItemStack templateStack = inv.getTemplateStackInSlot(slot);

                if (InventoryUtils.areItemStacksEqual(this.templateStacksLast[slot], templateStack) == false)
                {
                    for (int i = 0; i < this.listeners.size(); i++)
                    {
                        IContainerListener listener = this.listeners.get(i);

                        if (listener instanceof EntityPlayerMP)
                        {
                            PacketHandler.INSTANCE.sendTo(new MessageSyncCustomSlot(this.windowId, 0, slot, templateStack), (EntityPlayerMP) listener);
                        }
                    }

                    this.templateStacksLast[slot] = ItemStack.copyItemStack(templateStack);
                }
            }

            this.stackLimitLast = stackLimit;

            super.detectAndSendChanges();
        }
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);

        switch (id)
        {
            case 0:
                this.stackLimitLast = data;
                break;
            case 1:
                this.teasu.getBaseItemHandler().setStackLimit((data << 16) | this.stackLimitLast);
                break;
            case 2:
                this.teasu.getInventoryASU().setSlotLocked(data & 0xF, (data & 0x8000) != 0);
                break;
        }
    }

    @Override
    public void putCustomStack(int typeId, int slotNum, ItemStack stack)
    {
        this.teasu.getInventoryASU().setTemplateStackInSlot(slotNum, stack);
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        ItemStackHandlerLockable inv = this.teasu.getInventoryASU();

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
                this.cycleStackSize(element, this.teasu.getInventoryASU());
            }
        }
        // Alt + Middle click: Swap two stacks
        else if (EnumKey.MIDDLE_CLICK.matches(action, HotKeys.MOD_ALT))
        {
            this.swapSlots(element, player);
        }
    }
}
