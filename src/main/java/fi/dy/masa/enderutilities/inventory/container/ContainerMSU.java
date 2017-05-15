package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.inventory.ICustomSlotSync;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityMSU;

public class ContainerMSU extends ContainerLargeStacksTile implements ICustomSlotSync
{
    protected final TileEntityMSU temsu;
    private final boolean[] lockedLast;
    private final NonNullList<ItemStack> templateStacksLast;

    public ContainerMSU(EntityPlayer player, TileEntityMSU te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);
        this.temsu = te;
        this.itemHandlerLargeStacks = te.getInventoryMSU();

        int numSlots = this.itemHandlerLargeStacks.getSlots();
        this.lockedLast = new boolean[numSlots];
        this.templateStacksLast = NonNullList.withSize(numSlots, ItemStack.EMPTY);

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
    public void detectAndSendChanges()
    {
        this.syncLockableSlots(this.temsu.getInventoryMSU(), 0, 0, this.lockedLast, this.templateStacksLast);

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);

        if (id == 0)
        {
            this.temsu.getInventoryMSU().setSlotLocked(data & 0xF, (data & 0x8000) != 0);
        }
    }

    @Override
    public void putCustomStack(int typeId, int slotNum, ItemStack stack)
    {
        this.temsu.getInventoryMSU().setTemplateStackInSlot(slotNum, stack);
    }
}
