package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.item.ItemQuickStacker;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ContainerQuickStacker extends ContainerCustomSlotClick implements IContainerItem
{
    public ContainerQuickStacker(EntityPlayer player, ItemStack containerStack)
    {
        super(player, null);

        this.addPlayerInventorySlots(23, 36);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        super.addPlayerInventorySlots(posX, posY);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        ItemStack stack = ItemQuickStacker.getEnabledItem(player);

        // Middle click
        if (stack != null && clickType == ClickType.CLONE && dragType == 2)
        {
            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;
            if (invSlotNum == -1)
            {
                return null;
            }

            byte selected = NBTUtils.getByte(stack, ItemQuickStacker.TAG_NAME_CONTAINER, ItemQuickStacker.TAG_NAME_PRESET_SELECTION);
            long mask = NBTUtils.getLong(stack, ItemQuickStacker.TAG_NAME_CONTAINER, ItemQuickStacker.TAG_NAME_PRESET + selected);
            mask ^= (0x1L << invSlotNum);
            NBTUtils.setLong(stack, ItemQuickStacker.TAG_NAME_CONTAINER, ItemQuickStacker.TAG_NAME_PRESET + selected, mask);

            return null;
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }

    @Override
    public ItemStack getContainerItem()
    {
        return ItemQuickStacker.getEnabledItem(this.player);
    }
}
