package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.tileentity.TileEntityTemplatedChest;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerTemplatedChest extends ContainerTileEntityInventory
{
    TileEntityTemplatedChest tetc;

    public ContainerTemplatedChest(InventoryPlayer inventoryPlayer, TileEntityTemplatedChest te)
    {
        super(inventoryPlayer, te);
        this.tetc = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 58);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 8;
        int posY = 26;

        int tier = this.tetc.getStorageTier();

        // Regular or Deep Templated Chest - 27 slots
        if (tier == 1 || tier == 2)
        {
            if (tier == 2)
            {
                posY = 57;
            }

            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 9; j++)
                {
                    this.addSlotToContainer(new SlotGeneric(this.inventory, i * 9 + j, posX + j * 18, posY + i * 18));
                }
            }
        }
        // Small Templated Chest - 9 slots
        else
        {
            for (int i = 0; i < 9; i++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventory, i, posX + i * 18, posY));
            }
        }

        this.customInventorySlots = new SlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        if (tier == 2)
        {
            // Add the module slots as a priority slot range for shift+click merging
            this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

            posX = 98;
            posY = 26;

            int min = ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B;
            int max = ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;
            // The Storage Module slots
            for (int i = 0; i < 4; i++)
            {
                this.addSlotToContainer(new SlotModule(this.tetc.getModuleInventory(), i, posX + i * 18, posY, ModuleType.TYPE_MEMORY_CARD).setMinAndMaxModuleTier(min, max));
            }
        }
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        int tier = this.tetc.getStorageTier();
        if (tier == 0) { posY = 58; }
        else if (tier == 1) { posY = 94; }
        else if (tier == 2) { posY = 125; }

        super.addPlayerInventorySlots(posX, posY);
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        // Middle click
        if (button == 2 && type == 3 && slotNum >= 0 && slotNum < (this.tetc.getSizeInventory()))
        {
            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;
            if (invSlotNum == -1)
            {
                return null;
            }

            this.tetc.setTemplateStack(invSlotNum, this.tetc.getStackInSlot(invSlotNum));

            return null;
        }

        ItemStack stack = super.slotClick(slotNum, button, type, player);

        this.detectAndSendChanges();

        return stack;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
    }
}
