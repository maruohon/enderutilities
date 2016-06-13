package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;

public class ContainerHandyChest extends ContainerLargeStacks
{
    protected static final int[] PLAYER_INV_Y = new int[] { 95, 131, 167, 174 };
    protected TileEntityHandyChest tehc;
    public int selectedModule;
    public int actionMode;

    public ContainerHandyChest(EntityPlayer player, TileEntityHandyChest te)
    {
        super(player, te.getWrappedInventoryForContainer(player));
        this.tehc = te;

        this.addCustomInventorySlots();

        int tier = te.getStorageTier();
        int y = tier >= 0 && tier <= 3 ? PLAYER_INV_Y[tier] : 95;
        this.addPlayerInventorySlots(tier == 3 ? 44 : 8, y);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int tier = MathHelper.clamp_int(this.tehc.getStorageTier(), 0, 3);

        int posX = 8;
        int posY = tier <= 2 ? 41 : 13;

        int rows = tier <= 2 ? (tier + 1) * 2 : 8;
        int columns = tier == 3 ? 13 : 9;

        // Item inventory slots
        for (int row = 0; row < rows; row++)
        {
            for (int col = 0; col < columns; col++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, row * columns + col, posX + col * 18, posY + row * 18));
            }
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        // Add the module slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posX = tier <= 2 ? 98 : 224;
        posY = tier <= 2 ? 8 : 174;
        int modX = tier == 3 ? 0 : 18;
        int modY = tier == 3 ? 18 : 0;

        // The Storage Module slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerModule(this.tehc.getModuleInventory(), i,
                    posX + i * modX, posY + i * modY, ModuleType.TYPE_MEMORY_CARD_ITEMS));
        }
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our main item inventory
        if (slot instanceof SlotItemHandler && ((SlotItemHandler)slot).getItemHandler() == this.inventory)
        {
            return slot.getItemStackLimit(stack);
        }

        // Player inventory or module slots
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);

        listener.sendProgressBarUpdate(this, 0, this.tehc.getSelectedModule());
        listener.sendProgressBarUpdate(this, 1, this.tehc.getQuickMode());
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.tehc.getWorld().isRemote == true)
        {
            return;
        }

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener listener = this.listeners.get(i);

            if (this.selectedModule != this.tehc.getSelectedModule())
            {
                listener.sendProgressBarUpdate(this, 0, this.tehc.getSelectedModule());
            }

            if (this.actionMode != this.tehc.getQuickMode())
            {
                listener.sendProgressBarUpdate(this, 1, this.tehc.getQuickMode());
            }
        }

        this.selectedModule = this.tehc.getSelectedModule();
        this.actionMode = this.tehc.getQuickMode();

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int var, int val)
    {
        super.updateProgressBar(var, val);

        switch (var)
        {
            case 0:
                this.tehc.setSelectedModule(val);
                break;
            case 1:
                this.tehc.setQuickMode(val);
                break;
            default:
        }
    }
}
