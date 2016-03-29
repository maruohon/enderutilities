package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ContainerInventorySwapper extends ContainerCustomSlotClick implements IContainerModularItem
{
    public final InventoryItemModular inventoryItemModular;

    public ContainerInventorySwapper(EntityPlayer player, ItemStack containerStack)
    {
        super(player, new InventoryItemModular(containerStack, player, false, ModuleType.TYPE_MEMORY_CARD_ITEMS));
        this.inventoryItemModular = (InventoryItemModular)this.inventory;
        this.inventoryItemModular.setHostInventory(new PlayerMainInvWrapper(player.inventory));

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(36, 163);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        super.addPlayerInventorySlots(posX, posY);

        int playerArmorStart = this.inventorySlots.size();

        IItemHandlerModifiable inv = new PlayerArmorInvWrapper(this.inventoryPlayer);
        // Player armor slots
        posX = 13;
        posY = 53;
        for (int i = 0; i < 4; i++)
        {
            // FIXME 1.9
            final EntityEquipmentSlot entityequipmentslot = ContainerHandyBag.VALID_EQUIPMENT_SLOTS[i];
            this.addSlotToContainer(new SlotItemHandlerGeneric(inv, 3 - i, posX, posY + i * 18)
            {
                public int getSlotStackLimit()
                {
                    return 1;
                }

                public boolean isItemValid(ItemStack stack)
                {
                    if (stack == null) return false;

                    EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
                    return stack.getItem().isValidArmor(stack, slot, ContainerInventorySwapper.this.player);
                }

                @SideOnly(Side.CLIENT)
                @Override
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        this.playerArmorSlots = new MergeSlotRange(playerArmorStart, 4);
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
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        // Inventory Swapper's player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i, posX + i * 18, posY + 58));
        }

        // Add the armor slots inside the Inventory Swapper as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        posY = 33;
        // Inventory Swapper's armor slots
        for (int i = 0; i < 4; i++)
        {
            final EntityEquipmentSlot entityequipmentslot = ContainerHandyBag.VALID_EQUIPMENT_SLOTS[i];
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 39 - i, posX + i * 18, posY)
            {
                public int getSlotStackLimit()
                {
                    return 1;
                }

                public boolean isItemValid(ItemStack stack)
                {
                    if (stack == null) return false;

                    EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
                    return stack.getItem().isValidArmor(stack, slot, ContainerInventorySwapper.this.player);
                }

                @SideOnly(Side.CLIENT)
                @Override
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        posX = 126;
        posY = 18;
        int moduleSlots = this.inventoryItemModular.getModuleInventory().getSlots();
        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModuleModularItem(this.inventoryItemModular.getModuleInventory(), i, posX + i * 18, posY, ModuleType.TYPE_MEMORY_CARD_ITEMS, this));
        }

        // Add Memory Card slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size() - moduleSlots, moduleSlots);

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);
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
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        ItemStack stack = this.getModularItem();

        // FIXME 1.9: check the click type
        // Middle click
        if (dragType == 2 && clickType == ClickType.CLONE && stack != null && slotNum >= 44 && slotNum < (44 + 40))
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

        stack = super.slotClick(slotNum, dragType, clickType, player);

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
