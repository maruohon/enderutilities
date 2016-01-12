package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ContainerInventorySwapper extends ContainerEnderUtilitiesCustomSlotClick implements IContainerModularItem
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
        this.addPlayerInventorySlots(36, 163);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        super.addPlayerInventorySlots(posX, posY);

        int playerArmorStart = this.inventorySlots.size();

        // Player armor slots
        posX = 13;
        posY = 53;
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

        this.playerArmorSlots = new SlotRange(playerArmorStart, 4);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 36;
        int posY = 53;

        // Inventory Swapper's player inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventory, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        // Inventory Swapper's player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new SlotGeneric(this.inventory, i, posX + i * 18, posY + 58));
        }

        // Add the armor slots inside the Inventory Swapper as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posY = 33;
        // Inventory Swapper's armor slots
        for (int i = 0; i < 4; i++)
        {
            final int slotNum = i;
            this.addSlotToContainer(new SlotGeneric(this.inventory, 39 - i, posX + i * 18, posY)
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

        posX = 126;
        posY = 18;
        int moduleSlots = this.inventoryItemModular.getModuleInventory().getSizeInventory();
        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModule(this.inventoryItemModular.getModuleInventory(), i, posX + i * 18, posY, ModuleType.TYPE_MEMORY_CARD, this));
        }

        this.customInventorySlots = new SlotRange(customInvStart, this.inventorySlots.size() - customInvStart);
    }

    @Override
    public ItemStack getModularItem()
    {
        return this.inventoryItemModular.getModularItemStack();
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
        if (button == 2 && type == 3 && stack != null && slotNum >= 44 && slotNum < (44 + 40))
        {
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

        ItemStack modularStackPre = this.inventoryItemModular.getModularItemStack();

        stack = super.slotClick(slotNum, button, type, player);

        ItemStack modularStackPost = this.inventoryItemModular.getModularItemStack();

        // The Bag's stack changed after the click, re-read the inventory contents.
        if (modularStackPre != modularStackPost)
        {
            //System.out.println("slotClick() - updating container");
            this.inventoryItemModular.readFromContainerItemStack();
        }

        this.detectAndSendChanges();

        return stack;
    }
}
