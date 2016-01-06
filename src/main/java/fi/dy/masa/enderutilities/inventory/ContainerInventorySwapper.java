package fi.dy.masa.enderutilities.inventory;

import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerInventorySwapper extends ContainerEnderUtilities implements IContainerModularItem
{
    public InventoryItemModular inventoryItemModular;
    public EntityPlayer player;

    public ContainerInventorySwapper(EntityPlayer player, InventoryItemModular inventory)
    {
        super(player.inventory, inventory);
        this.player = player;
        this.inventoryItemModular = inventory;
        this.inventoryItemModular.setHostInventory(player.inventory);

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(24, 117);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        super.addPlayerInventorySlots(posX, posY);

        // Player armor slots
        posY = 10;
        for (int i = 0; i < 4; i++)
        {
            final int slotNum = i;
            this.addSlotToContainer(new Slot(this.inventoryPlayer, 39 - i, posX, posY + i * 18)
            {
                public int getSlotStackLimit()
                {
                    return 1;
                }

                public boolean isItemValid(ItemStack stack)
                {
                    if (stack == null) return false;
                    return stack.getItem().isValidArmor(stack, slotNum, ContainerInventorySwapper.this.player);
                }

                /* TODO: Enable this in 1.8; in 1.7.10, there is a Forge bug that causes
                 * the Slot background icons to render incorrectly if there is an item with the glint effect
                 * before the Slot in question in the Container.
                @SideOnly(Side.CLIENT)
                public IIcon getBackgroundIconIndex()
                {
                    return ItemArmor.func_94602_b(slotNum);
                }
                */
            });
        }
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int xOff = 114;
        int yOff = 46;

        int moduleSlots = this.inventoryItemModular.getModuleInventory().getSizeInventory();
        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModule(this.inventoryItemModular.getModuleInventory(), i, xOff + i * 18, yOff, ModuleType.TYPE_MEMORY_CARD, this));
        }
    }

    @Override
    public ItemStack getModularItem()
    {
        return this.inventoryItemModular.getModularItemStack();
    }

    @Override
    protected int getNumMergableSlots(int invSize)
    {
        // Our inventory, player item inventory and armor slots
        return invSize + this.inventoryPlayer.getSizeInventory();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        this.inventoryItemModular.closeInventory();
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        ItemStack stack = this.getModularItem();

        // Middle click
        if (button == 2 && type == 3 && stack != null && slotNum >= 4 && slotNum < 44)
        {
            // Damn armor slots being reversed ;_;
            if (slotNum >= 40)
            {
                slotNum = 43 - slotNum + 40;
            }

            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;
            if (invSlotNum == -1)
            {
                return null;
            }

            byte selected = NBTUtils.getByte(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET_SELECTION);
            long mask = NBTUtils.getLong(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET + selected);
            mask ^= (0x1L << invSlotNum);
            NBTUtils.setLong(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET + selected, mask);
            return null;
        }

        return super.slotClick(slotNum, button, type, player);
    }
}
